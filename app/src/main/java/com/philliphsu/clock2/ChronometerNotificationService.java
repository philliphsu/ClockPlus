package com.philliphsu.clock2;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.CallSuper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.SimpleArrayMap;

import com.philliphsu.clock2.timers.ChronometerDelegate;

/**
 * Created by Phillip Hsu on 9/10/2016.
 */
public abstract class ChronometerNotificationService extends Service {
    public static final String ACTION_START_PAUSE = "com.philliphsu.clock2.timers.action.START_PAUSE";
    public static final String ACTION_STOP = "com.philliphsu.clock2.timers.action.STOP";

    // TODO: I think we'll need a collection of builders too. However, we can have a common immutable
    // builder instance with attributes that all timer notifications will have.
    private NotificationCompat.Builder mNoteBuilder;
    private NotificationManager mNotificationManager;
    @Deprecated
    private ChronometerNotificationThread mThread;
    @Deprecated
    private final ChronometerDelegate mDelegate = new ChronometerDelegate();

    /**
     * The default capacity of an array map is 0.
     * The minimum amount by which the capacity of a ArrayMap will increase
     * is currently {@link SimpleArrayMap#BASE_SIZE 4}.
     */
    private final SimpleArrayMap<Long, ChronometerNotificationThread> mThreads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, ChronometerDelegate> mDelegates = new SimpleArrayMap<>();

    /**
     * @return the icon for the notification
     */
    @DrawableRes
    protected abstract int getSmallIcon();

    /**
     * @return an optional content intent that is fired when the notification is clicked
     */
    @Nullable
    protected abstract PendingIntent getContentIntent();

    /**
     * @return whether the chronometer should be counting down
     */
    protected abstract boolean isCountDown();

    /**
     * @return the id for the foreground notification, if {@link #isForeground()} returns true.
     * Otherwise, this value will not be considered for anything.
     */
    protected abstract int getNoteId();

    /**
     * @return whether this service should run in the foreground. The default is true.
     */
    protected boolean isForeground() {
        return true;
    }

    /**
     * The intent received in {@link #onStartCommand(Intent, int, int)}
     * has no {@link Intent#getAction() action} set. At this point, you
     * should configure the notification to be displayed.
     * @param intent
     * @param flags
     * @param startId
     */
    protected abstract void handleDefaultAction(Intent intent, int flags, long startId);

    protected abstract void handleStartPauseAction(Intent intent, int flags, long startId);

    protected abstract void handleStopAction(Intent intent, int flags, long startId);

    /**
     * This will be called if the command in {@link #onStartCommand(Intent, int, int)}
     * has an action that your subclass defines.
     * @param action Your custom action.
     */
    protected abstract void handleAction(@NonNull String action, Intent intent, int flags, long startId);

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Create base note
        mNoteBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(getSmallIcon())
                .setShowWhen(false)
                .setOngoing(true)
                .setContentIntent(getContentIntent());
        if (isForeground()) {
            registerNewChronometer(getNoteId());
            startForeground(getNoteId(), mNoteBuilder.build());
        }
    }

    private void registerNewChronometer(long id) {
        ChronometerDelegate delegate = new ChronometerDelegate();
        delegate.init();
        delegate.setCountDown(isCountDown());
        mDelegates.put(id, delegate);
    }

    // Didn't work!
//    @Override
//    public void onTrimMemory(int level) {
//        if (level >= TRIM_MEMORY_BACKGROUND) {
//            Log.d("ChronomNotifService", "Stopping foreground");
//            // The penultimate trim level, indicates the process is around the
//            // middle of the background LRU list.
//            // If we didn't call this, we would continue to run in the foreground
//            // until we get killed, and the notification would be removed with it.
//            // We want to keep the notification alive even if the process is killed,
//            // so the user can still be aware of the stopwatch.
//            stopForeground(true);
//            // Post it again, but outside of the foreground state.
//            updateNotification(true);
//        }
//    }

    @Override
    public void onDestroy() {
        // TODO: Quit all threads by iterating through the collection
        quitThread(); // TOneverDO: quitCurrentThread() because that posts the notification again
    }

    @CallSuper
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_START_PAUSE:
                        handleStartPauseAction(intent, flags, startId);
                        break;
                    case ACTION_STOP:
                        handleStopAction(intent, flags, startId);
                        break;
                    default:
                        // Defer to the subclass
                        handleAction(action, intent, flags, startId);
                        break;
                }
            } else {
                handleDefaultAction(intent, flags, startId);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public final IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * If there is a thread currently running, then this will push any notification updates
     * you might have configured in the Builder and then call the thread's {@link
     * ChronometerNotificationThread#quit() quit()}.
     */
    // TODO: rename method to quitThread(long id)
    public void quitCurrentThread() {
        if (mThread != null) {
            // Display any notification updates associated with the current state
            // of the chronometer. If we relied on the HandlerThread to do this for us,
            // the message delivery would be delayed.
            mThread.updateNotification(false/*updateText*/);
            // If the chronometer has been set to not run, the effect is obvious.
            // Otherwise, we're preparing for the start of a new thread.
            quitThread();
        }
    }

    /**
     * Instantiates a new HandlerThread and calls its {@link Thread#start() start()}.
     * The calling thread will be blocked until the HandlerThread created here finishes
     * initializing its looper.
     * @param base the new base time of the chronometer
     */
    // TODO: Change sig to (long id, long base)
    public void startNewThread(long base) {
        // An instance of Thread cannot be started more than once. You must create
        // a new instance if you want to start the Thread's work again.
        mThread = new ChronometerNotificationThread(
                mDelegate,
                mNotificationManager,
                mNoteBuilder,
                getResources(),
                getNoteId());
        // Initializes this thread as a looper. HandlerThread.run() will be executed
        // in this thread.
        // This gives you a chance to create handlers that then reference this looper,
        // before actually starting the loop.
        mThread.start();
        // If this thread has been started, this method will block *the calling thread*
        // until the looper has been initialized. This ensures the handler thread is
        // fully initialized before we proceed.
        mThread.getLooper();
        // -------------------------------------------------------------------------------
        // TOneverDO: Set base BEFORE the thread is ready to begin working, or else when
        // the thread actually begins working, it will initially show that some time has
        // passed.
        mDelegate.setBase(base);
        // -------------------------------------------------------------------------------
    }

    /**
     * Helper method to add the start/pause action to the notification's builder.
     * @param running whether the chronometer is running
     * @param requestCode Used to create the PendingIntent that is fired when this action is clicked.
     */
    protected final void addStartPauseAction(boolean running, int requestCode/*TODO: long id. as a request code, cast down.*/) {
        // TODO: Add this to the correct Builder, associated with the provided long id.
        addAction(ACTION_START_PAUSE,
                running ? R.drawable.ic_pause_24dp : R.drawable.ic_start_24dp,
                getString(running ? R.string.pause : R.string.resume),
                requestCode);
    }

    /**
     * Helper method to add the stop action to the notification's builder.
     * @param requestCode Used to create the PendingIntent that is fired when this action is clicked.
     */
    protected final void addStopAction(int requestCode/*TODO: long id. as a request code, cast down.*/) {
        addAction(ACTION_STOP, R.drawable.ic_stop_24dp, getString(R.string.stop), requestCode);
    }

    /**
     * Clear the notification builder's set actions.
     */
    protected final void clearActions(/*TODO: long id*/) {
        // TODO: Clear the actions from the correct builder.
        // TODO: The source indicates mActions is hidden, so how are we able to access it?
        // Will it remain accessible for all SDK versions? If not, we would have to rebuild
        // the entire notification with a new local Builder instance.
        mNoteBuilder.mActions.clear();
    }

    // TODO: We'll need to change the signatures of all these to have a long id param.
    protected final void setBase(long base) {
        mDelegate.setBase(base);
    }

    protected final long getBase() {
        return mDelegate.getBase();
    }

    protected final void updateNotification(boolean updateText) {
        mThread.updateNotification(updateText);
    }

    protected final void setContentTitle(CharSequence title) {
        mNoteBuilder.setContentTitle(title);
    }

    //////////////////////////////////////////////////////////////////////////////////////

    /**
     * Adds the specified action to the notification's Builder.
     */
    protected final void addAction(String action, @DrawableRes int icon, String actionTitle, int requestCode/*TODO: long id. as a request code, cast down.*/) {
        Intent intent = new Intent(this, getClass())
                .setAction(action);
        // TODO: We can put the requestCode as an extra to this intent, and then retrieve that extra
        // in onStartCommand() to figure out which of the multiple timers should we apply this action to.
//                .putExtra(EXTRA_TIMER, mTimer);
        PendingIntent pi = PendingIntent.getService(
                this, requestCode, intent, 0/*no flags*/);
        mNoteBuilder.addAction(icon, actionTitle, pi);
    }

    /**
     * Cancels the foreground notification.
     */
    // TODO: change sig to long id
    protected final void cancelNotification() {
        mNotificationManager.cancel(getNoteId());
    }

    /**
     * Causes the handler thread's looper to terminate without processing
     * any more messages in the message queue.
     */
    // TODO: change sig to long id
    private void quitThread() {
        if (mThread != null && mThread.isAlive()) {
            mThread.quit();
        }
    }
}
