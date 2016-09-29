package com.philliphsu.clock2.timers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;

import com.philliphsu.clock2.MainActivity;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.chronometer.ChronometerNotificationService;
import com.philliphsu.clock2.timers.data.AsyncTimersTableUpdateHandler;
import com.philliphsu.clock2.timers.data.TimerCursor;
import com.philliphsu.clock2.util.ContentIntentUtils;

/**
 * Handles the notification for an active Timer.
 * TOneverDO: extend IntentService, it is ill-suited for our requirement that
 * this remains alive until we explicitly stop it. Otherwise, it would finish
 * a single task and immediately destroy itself, which means we lose all of
 * our instance state.
 */
public class TimerNotificationService extends ChronometerNotificationService {
    private static final String TAG = "TimerNotifService";

    private static final String ACTION_CANCEL_NOTIFICATION = "com.philliphsu.clock2.timers.action.CANCEL_NOTIFICATION";
    public static final String ACTION_ADD_ONE_MINUTE = "com.philliphsu.clock2.timers.action.ADD_ONE_MINUTE";

    public static final String EXTRA_TIMER = "com.philliphsu.clock2.timers.extra.TIMER";
    private static final String EXTRA_CANCEL_TIMER_ID = "com.philliphsu.clock2.timers.extra.CANCEL_TIMER_ID";

    private AsyncTimersTableUpdateHandler mUpdateHandler;
    private final SimpleArrayMap<Long, Timer> mTimers = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, TimerController> mControllers = new SimpleArrayMap<>();

    private long mMostRecentTimerId;

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
    public static void cancelNotification(Context context, long timerId) {
        Intent intent = new Intent(context, TimerNotificationService.class)
                .setAction(ACTION_CANCEL_NOTIFICATION)
                .putExtra(EXTRA_CANCEL_TIMER_ID, timerId);
        context.startService(intent);
    }

    @Override
    protected int getSmallIcon() {
        return R.drawable.ic_timer_24dp;
    }

    @Nullable
    @Override
    protected PendingIntent getContentIntent() {
        // The base class won't call this for us because this is not a foreground service,
        // as we require multiple notifications created as needed. Instead, this is called after
        // we call registerNewNoteBuilder() in handleDefaultAction().
        // Before we called registerNewNoteBuilder(), we saved a reference to the most recent timer id.
        return ContentIntentUtils.create(this, MainActivity.PAGE_TIMERS, mMostRecentTimerId);
    }

    @Override
    protected boolean isCountDown() {
        return true;
    }

    @Override
    protected int getNoteId() {
        // Since isForeground() returns false, this won't be called by the base class.
        return 0;
    }

    @Override
    protected String getNoteTag() {
        // This is so we can cancel notifications in our static helper method
        // cancelNotification(Context, long) with the static TAG constant
        return TAG;
    }

    @Override
    protected boolean isForeground() {
        // We're going to post a separate notification for each Timer.
        // Foreground services are limited to one notification.
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mUpdateHandler = new AsyncTimersTableUpdateHandler(this, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // After being cancelled due to time being up, sometimes the active timer notification posts again
        // with a static 00:00 text, along with the Time's up notification. My theory is
        // our thread has enough leeway to sneak in a final call to post the notification before it
        // is actually quit().
        // As such, try cancelling the notification with this (tag, id) pair again.
        for (int i = 0; i < mTimers.size(); i++) {
            cancelNotification(mTimers.keyAt(i));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.d(TAG, "Recreated service, starting chronometer again.");
            // Restore all running timers. This restores all of the base
            // class's member state as well, due to the various API
            // calls required.
            new Thread(new Runnable() {
                @Override
                public void run() {
                    TimerCursor cursor = mUpdateHandler.getTableManager().queryStartedTimers();
                    while (cursor.moveToNext()) {
                        // We actually don't need any args since this will be
                        // passed directly to our handler method. If we were going
                        // to startService() with this, then we would need to
                        // specify them.
                        Intent intent = new Intent(
                                /*TimerNotificationService.this,
                                TimerNotificationService.class*/);
                        intent.putExtra(EXTRA_TIMER, cursor.getItem());
                        // TODO: Should we startService() instead?
                        handleDefaultAction(intent, 0, 0);
                    }
                }
            }).start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void handleDefaultAction(Intent intent, int flags, int startId) {
        final Timer timer = intent.getParcelableExtra(EXTRA_TIMER);
        if (timer == null) {
            throw new IllegalStateException("Cannot start TimerNotificationService without a Timer");
        }
        final long id = timer.getId();
        boolean updateChronometer = false;
        Timer oldTimer = mTimers.put(id, timer);
        if (oldTimer != null) {
            updateChronometer = oldTimer.endTime() != timer.endTime();
        }
        mControllers.put(id, new TimerController(timer, mUpdateHandler));
        mMostRecentTimerId = id;
        // If isUpdate == true, this won't call through because the id already exists in the
        // internal mappings as well.
        registerNewNoteBuilder(id);
        // The note's title should change here every time, especially if the Timer's label was updated.
        String title = timer.label();
        if (title.isEmpty()) {
            title = getString(R.string.timer);
        }
        setContentTitle(id, title);
        if (updateChronometer) {
            // Immediately push any duration updates, or else there will be a noticeable delay.
            setBase(id, timer.endTime());
            updateNotification(id, true);
        }
        // This handles any other notification updates like the title or actions, even if
        // the timer is not running because the current thread will update the notification
        // (besides the content text) before quitting.
        syncNotificationWithTimerState(id, timer.isRunning());
    }

    @Override
    protected void handleStartPauseAction(Intent intent, int flags, int startId) {
        long id = getActionId(intent);
        mControllers.get(id).startPause();
        syncNotificationWithTimerState(id, mTimers.get(id).isRunning());
    }

    @Override
    protected void handleStopAction(Intent intent, int flags, int startId) {
        long id = getActionId(intent);
        mControllers.get(id).stop();
        // We leave removing the notification up to AsyncTimersTableUpdateHandler
        // when it calls cancelAlarm() from onPostAsyncUpdate().
        // This calls the static helper cancelNotification(), which
        // starts this service to handle ACTION_CANCEL_NOTIFICATION.
    }

    @Override
    protected void handleAction(@NonNull String action, Intent intent, int flags, int startId) {
        if (ACTION_ADD_ONE_MINUTE.equals(action)) {
            // While the notification's countdown would automatically be extended by one minute,
            // there is a noticeable delay before the minute gets added on.
            // Update the text immediately, because there's no harm in doing so.
            long id = getActionId(intent);
            final Timer orig = mTimers.get(id);
            // We have to add the minute to a copy Timer. If we had modified the original Timer,
            // then the controller would also add another minute to the same instance, and what
            // would happen after the DB update is the Timer instance with this ID ends up
            // being extended by 2 minutes.
            //
            // Why not just do something like: `t.endTime() + 60000`? Because extending timers that
            // are paused is a special case, and Timer.addOneMinute() already has the logic to handle
            // that.
            Timer copy = Timer.create(orig.hour(), orig.minute(), orig.second(), orig.group(), orig.label());
            orig.copyMutableFieldsTo(copy);
            copy.addOneMinute();
            setBase(id, copy.endTime());
            updateNotification(id, true);
            mControllers.get(id).addOneMinute();
        } else if (ACTION_CANCEL_NOTIFICATION.equals(action)) {
            long id = intent.getLongExtra(EXTRA_CANCEL_TIMER_ID, -1);
            cancelNotification(id);
            // TODO: SHould this be before cancelNotification()? I'm worried
            // that the thread's handler will have enough leeway to sneak
            // in a notification update before it is quit. If it did,
            // then at least cancelNotification should theoretically
            // remove it...
            releaseResources(id);
        } else {
            throw new IllegalArgumentException("TimerNotificationService cannot handle action " + action);
        }
    }

    @Override
    protected void releaseResources(long id) {
        super.releaseResources(id);
        mTimers.remove(id);
        mControllers.remove(id);
        // TODO: Should we make a private method?
        // This private method would first call releaseResources(),
        // and then this block.
        if (mTimers.isEmpty()) { // We could check any map, since they should all have the same sizes
            stopSelf();
        }
    }

    private void syncNotificationWithTimerState(long id, boolean running) {
        // The actions from the last time we configured the Builder are still here.
        // We have to retain the relative ordering of the actions while updating
        // just the start/pause action, so clear them and set them again.
        clearActions(id);
        addAction(ACTION_ADD_ONE_MINUTE, R.drawable.ic_add_24dp, getString(R.string.minute), id);
        addStartPauseAction(running, id);
        addStopAction(id);

        quitCurrentThread(id);
        if (running) {
            startNewThread(id, mTimers.get(id).endTime());
        }
    }
    
    private long getActionId(Intent intent) {
        return intent.getLongExtra(EXTRA_ACTION_ID, -1);
    }
}