package com.philliphsu.clock2.timers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.philliphsu.clock2.AsyncTimersTableUpdateHandler;
import com.philliphsu.clock2.ChronometerNotificationService;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.Timer;

/**
 * Handles the notification for an active Timer.
 * TOneverDO: extend IntentService, it is ill-suited for our requirement that
 * this remains alive until we explicitly stop it. Otherwise, it would finish
 * a single task and immediately destroy itself, which means we lose all of
 * our instance state.
 */
public class TimerNotificationService extends ChronometerNotificationService {
    private static final String TAG = "TimerNotifService";

    public static final String ACTION_ADD_ONE_MINUTE = "com.philliphsu.clock2.timers.action.ADD_ONE_MINUTE";

    public static final String EXTRA_TIMER = "com.philliphsu.clock2.timers.extra.TIMER";

    // TODO: I think we may need a list of timers.
    private Timer mTimer;
    private TimerController mController;

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
        // TODO: We do this in onDestroy() for a single notification.
        // Multiples will probably need something like this.
//        NotificationManager nm = (NotificationManager)
//                context.getSystemService(Context.NOTIFICATION_SERVICE);
//        nm.cancel(getNoteTag(), (int) timerId);
        // TODO: We only do this for a single notification. Remove this for multiples.
        context.stopService(new Intent(context, TimerNotificationService.class));
    }

    @Override
    protected int getSmallIcon() {
        return R.drawable.ic_timer_24dp;
    }

    @Nullable
    @Override
    protected PendingIntent getContentIntent() {
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
        return null;
    }

    @Override
    protected boolean isCountDown() {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // After being cancelled due to time being up, sometimes the active timer notification posts again
        // with a static 00:00 text, along with the Time's up notification. My theory is
        // our thread has enough leeway to sneak in a final call to post the notification before it
        // is actually quit().
        // As such, try cancelling the notification with this (tag, id) pair again.
        cancelNotification(mTimer.getIntId());
    }

    @Override
    protected void handleDefaultAction(Intent intent, int flags, long startId) {
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
        setContentTitle(title);
        syncNotificationWithTimerState(mTimer.isRunning());
    }

    @Override
    protected void handleStartPauseAction(Intent intent, int flags, long startId) {
        mController.startPause();
        syncNotificationWithTimerState(mTimer.isRunning());
    }

    @Override
    protected void handleStopAction(Intent intent, int flags, long startId) {
        mController.stop();
        stopSelf();
        // We leave removing the notification up to AsyncTimersTableUpdateHandler
        // when it calls cancelAlarm() from onPostAsyncUpdate().
    }

    @Override
    protected void handleAction(@NonNull String action, Intent intent, int flags, long startId) {
        if (ACTION_ADD_ONE_MINUTE.equals(action)) {
            // While the notification's countdown would automatically be extended by one minute,
            // there is a noticeable delay before the minute gets added on.
            // Update the text immediately, because there's no harm in doing so.
            setBase(getBase() + 60000);
            updateNotification();
            mController.addOneMinute();
        } else {
            throw new IllegalArgumentException("TimerNotificationService cannot handle action " + action);
        }
    }

    private void syncNotificationWithTimerState(boolean running) {
        // The actions from the last time we configured the Builder are still here.
        // We have to retain the relative ordering of the actions while updating
        // just the start/pause action, so clear them and set them again.
        clearActions();
        final int timerId = mTimer.getIntId();
        addAction(ACTION_ADD_ONE_MINUTE,
                R.drawable.ic_add_24dp,
                getString(R.string.minute),
                timerId);
        addStartPauseAction(running, timerId);
        addStopAction(timerId);

        quitCurrentThread();
        if (running) {
            startNewThread(timerId, SystemClock.elapsedRealtime() + mTimer.timeRemaining());
        }
    }
}