package com.philliphsu.clock2.timers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.philliphsu.clock2.AsyncTimersTableUpdateHandler;
import com.philliphsu.clock2.ChronometerNotificationService;
import com.philliphsu.clock2.MainActivity;
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
    private Intent mIntent;

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
        mIntent = new Intent(this, MainActivity.class);
        // http://stackoverflow.com/a/3128418/5055032
        // "For some unspecified reason, extras will be delivered only if you've set some action"
        // This ONLY applies to PendingIntents...
        // And for another unspecified reason, this dummy action must NOT be the same value
        // as another PendingIntent's dummy action. For example, StopwatchNotificationService
        // uses the dummy action "foo"; we previously used "foo" here as well, and firing this
        // intent scrolled us to MainActivity.PAGE_STOPWATCH...
        mIntent.setAction("bar");
        mIntent.putExtra(MainActivity.EXTRA_SHOW_PAGE, MainActivity.PAGE_TIMERS);
        // Request code not needed because we're only going to have one foreground notification.
        return PendingIntent.getActivity(this, 0, mIntent, 0);
    }

    @Override
    protected boolean isCountDown() {
        return true;
    }

    @Override
    protected int getNoteId() {
        return R.id.timer_notification_service;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // After being cancelled due to time being up, sometimes the active timer notification posts again
        // with a static 00:00 text, along with the Time's up notification. My theory is
        // our thread has enough leeway to sneak in a final call to post the notification before it
        // is actually quit().
        // As such, try cancelling the notification with this (tag, id) pair again.
        cancelNotification();
    }

    @Override
    protected void handleDefaultAction(Intent intent, int flags, long startId) {
        if ((mTimer = intent.getParcelableExtra(EXTRA_TIMER)) == null) {
            throw new IllegalStateException("Cannot start TimerNotificationService without a Timer");
        }
        // TODO: Wrap this around an `if (only one timer running)` statement.
        // TODO: We have to update the PendingIntent.. so write an API in the base class to do so.
        // TODO: Not implemented for simplicity. Future release??
//        mIntent.putExtra(TimersFragment.EXTRA_SCROLL_TO_TIMER_ID, mTimer.getId());
        mController = new TimerController(mTimer, new AsyncTimersTableUpdateHandler(this, null));
        // The note's title should change here every time, especially if the Timer's label was updated.
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
            updateNotification(true);
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
            startNewThread(SystemClock.elapsedRealtime() + mTimer.timeRemaining());
        }
    }
}