package com.philliphsu.clock2.timers;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.philliphsu.clock2.AsyncTimersTableUpdateHandler;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.Timer;

/**
 * Handles the notification for an active Timer.
 * TOneverDO: extend IntentService, it is ill-suited for our requirement that
 * this remains alive until we explicitly stop it. Otherwise, it would finish
 * a single task and immediately destroy itself, which means we lose all of
 * our instance state.
 */
public class TimerNotificationService extends Service {
    private static final String TAG = "TimerNotifService";

    public static final String ACTION_ADD_ONE_MINUTE = "com.philliphsu.clock2.timers.action.ADD_ONE_MINUTE";
    public static final String ACTION_START_PAUSE = "com.philliphsu.clock2.timers.action.START_PAUSE";
    public static final String ACTION_STOP = "com.philliphsu.clock2.timers.action.STOP";

    public static final String EXTRA_TIMER = "com.philliphsu.clock2.timers.extra.TIMER";

    private Timer mTimer; // TODO: I think we may need a list of timers.
    private TimerController mController;
    private NotificationCompat.Builder mNoteBuilder;
    private NotificationManager mNotificationManager;
    private final ChronometerDelegate mCountdownDelegate = new ChronometerDelegate();
    private MyHandlerThread mThread; // TODO: I think we may need a list of threads.

    /**
     * Helper method to start this Service for its default action: to show
     * the notification for the Timer with the given id.
     */
    public static void showNotification(Context context, Timer timer) {
        Intent intent = new Intent(context, TimerNotificationService.class);
        intent.putExtra(EXTRA_TIMER, timer);
        context.startService(intent);
    }

    /**
     * Helper method to cancel the notification previously shown from calling
     * {@link #showNotification(Context, Timer)}. This does NOT start the Service
     * and call through to {@link #onStartCommand(Intent, int, int)}, because
     * the work does not require so.
     * @param timerId the id of the Timer associated with the notification
     *                you want to cancel
     */
    public static void cancelNotification(Context context, long timerId) { // TODO: remove long param
        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(TAG, (int) timerId);
        context.stopService(new Intent(context, TimerNotificationService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create base note
        mNoteBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_timer_24dp)
                .setShowWhen(false)
                .setOngoing(true);
        // TODO: Set content intent so that when clicked, we launch
        // TimersFragment and scroll to the given timer id. The following
        // is merely pseudocode.
//        Intent contentIntent = new Intent(this, MainActivity.class);
//        contentIntent.putExtra(null/*TODO:MainActivity.EXTRA_SHOW_PAGE*/, 1/*TODO:The tab index of the timers page*/);
//        contentIntent.putExtra(null/*TODO:MainActivity.EXTRA_SCROLL_TO_ID*/, mTimer.getId());
//        mNoteBuilder.setContentIntent(PendingIntent.getActivity(
//                this,
//                0, // TODO: Request code not needed? Since any multiple notifications
//                // should be able to use the same PendingIntent for this action....
//                // unless the underlying *Intent* and its id extra are overwritten
//                // per notification when retrieving the PendingIntent..
//                contentIntent,
//                0/*Shouldn't need a flag..*/));

        mCountdownDelegate.init();
        mCountdownDelegate.setCountDown(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        quitThread();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action == null) {
                if ((mTimer = intent.getParcelableExtra(EXTRA_TIMER)) == null) {
                    throw new IllegalStateException("Cannot start TimerNotificationService without a Timer");
                }
                mController = new TimerController(mTimer, new AsyncTimersTableUpdateHandler(this, null));
                // The note's title should change here every time,
                // especially if the Timer's label was updated.
                String title = mTimer.label();
                if (title.isEmpty()) {
                    title = getString(R.string.timer);
                }
                mNoteBuilder.setContentTitle(title);
                syncNotificationWithTimerState(mTimer.isRunning());
            } else if (ACTION_ADD_ONE_MINUTE.equals(action)) {
                // While the notification's countdown would automatically be extended by one minute,
                // there is a noticeable delay before the minute gets added on.
                // Update the text immediately, because there's no harm in doing so.
                mCountdownDelegate.setBase(mCountdownDelegate.getBase() + 60000);
                // Dispatch a one-time (non-looping) message so as not to conflate
                // with the current set of looping messages.
                mThread.sendMessage(MSG_DISPATCH_TICK);
                mController.addOneMinute();
            } else if (ACTION_START_PAUSE.equals(action)) {
                mController.startPause();
                syncNotificationWithTimerState(mTimer.isRunning());
            } else if (ACTION_STOP.equals(action)) {
                mController.stop();
                stopSelf();
                // We leave removing the notification up to AsyncTimersTableUpdateHandler
                // when it calls cancelAlarm() from onPostAsyncUpdate().
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void syncNotificationWithTimerState(boolean running) {
        // The actions from the last time we configured the Builder are still here.
        // We have to retain the relative ordering of the actions while updating
        // just the start/pause action, so clear them and set them again.
        // TODO: The source indicates mActions is hidden, so how are we able to access it?
        // Will it remain accessible for all SDK versions? If not, we would have to rebuild
        // the entire notification with a new local Builder instance.
        mNoteBuilder.mActions.clear();
        addAction(ACTION_ADD_ONE_MINUTE, R.drawable.ic_add_24dp, getString(R.string.minute));
        addAction(ACTION_START_PAUSE,
                running ? R.drawable.ic_pause_24dp : R.drawable.ic_start_24dp,
                getString(running ? R.string.pause : R.string.resume));
        addAction(ACTION_STOP, R.drawable.ic_stop_24dp, getString(R.string.stop));

        // Post the notification immediately, as the HandlerThread will delay its first
        // message delivery.
        updateNotification();
        // Quit any previously executed thread. If running == false, the effect is obvious;
        // otherwise, we're preparing for the start of a new thread.
        quitThread();

        if (running) {
            // An instance of Thread cannot be started more than once. You must create
            // a new instance if you want to start the Thread's work again.
            mThread = new MyHandlerThread();
            // Initializes this thread as a looper. HandlerThread.run() will be executed
            // in this thread.
            // This gives you a chance to create handlers that then reference this looper,
            // before actually starting the loop.
            mThread.start();
            // If this thread has been started, this method will block *the calling thread*
            // until the looper has been initialized. This ensures the handler thread is
            // fully initialized before we proceed.
            mThread.getLooper();
            Log.d(TAG, "Looper initialized");
            mCountdownDelegate.setBase(SystemClock.elapsedRealtime() + mTimer.timeRemaining());
            mThread.sendMessage(MSG_WHAT);
        }
    }

    /**
     * Builds and adds the specified action to the notification's mNoteBuilder.
     */
    private void addAction(String action, @DrawableRes int icon, String actionTitle) {
        Intent intent = new Intent(this, TimerNotificationService.class)
                .setAction(action);
//                .putExtra(EXTRA_TIMER, mTimer);
        PendingIntent pi = PendingIntent.getService(this,
                mTimer.getIntId(), intent, 0/*no flags*/);
        mNoteBuilder.addAction(icon, actionTitle, pi);
    }

    /**
     * Causes the handler thread's looper to terminate without processing
     * any more messages in the message queue.
     */
    private void quitThread() {
        if (mThread != null && mThread.isAlive()) {
            mThread.quit();
        }
    }

    private void updateNotification() {
        CharSequence text = mCountdownDelegate.formatElapsedTime(SystemClock.elapsedRealtime());
        mNoteBuilder.setContentText(text);
        mNotificationManager.notify(TAG, mTimer.getIntId(), mNoteBuilder.build());
    }

    private static final int MSG_WHAT = 2;
    private static final int MSG_DISPATCH_TICK = 3;

    private class MyHandlerThread extends HandlerThread {
        private Handler mHandler;

        public MyHandlerThread() {
            super("MyHandlerThread");
        }

        // There won't be a memory leak since our handler is using a looper that is not
        // associated with the main thread. The full Lint warning confirmed this.
        @SuppressLint("HandlerLeak")
        @Override
        protected void onLooperPrepared() {
            Log.d(TAG, "Looper fully prepared");
            // This is called after the looper has completed initializing, but before
            // it starts looping through its message queue. Right now, there is no
            // message queue, so this is the place to create it.
            // By default, the constructor associates this handler with this thread's looper.
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message m) {
                    updateNotification();
                    if (m.what != MSG_DISPATCH_TICK) {
                        sendMessageDelayed(Message.obtain(this, MSG_WHAT), 1000);
                    }
                }
            };
        }

        public void sendMessage(int what) {
            // We've encountered NPEs because the handler was still
            // uninitialized even at this point. I assume we cannot rely on any
            // defined order in which different threads execute their code.
            // Block the calling thread from proceeding until the handler thread
            // completes the handler's initialization.
            while (mHandler == null);

            Log.d(TAG, "Sending message");
            Message msg = Message.obtain(mHandler, what);
            if (what == MSG_DISPATCH_TICK) {
                mHandler.sendMessage(msg);
            } else if (what == MSG_WHAT) {
                mHandler.sendMessageDelayed(msg, 1000);
            }
        }
    }
}