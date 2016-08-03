package com.philliphsu.clock2.timers;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.support.v4.app.NotificationCompat;

import com.philliphsu.clock2.MainActivity;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.Timer;
import com.philliphsu.clock2.model.TimersTableManager;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class TimerNotificationService extends IntentService {
    private static final String TAG = "TimerNotificationService";

    public static final String ACTION_ADD_ONE_MINUTE = "com.philliphsu.clock2.timers.action.ADD_ONE_MINUTE";
    public static final String ACTION_START_PAUSE = "com.philliphsu.clock2.timers.action.START_PAUSE";
    public static final String ACTION_STOP = "com.philliphsu.clock2.timers.action.STOP";

    public static final String EXTRA_TIMER_ID = "com.philliphsu.clock2.timers.extra.TIMER_ID";

    private TimersTableManager mTableManager;

    public TimerNotificationService() {
        super("TimerNotificationService");
    }

    /**
     * Helper method to start this Service for its default action: to show
     * the notification for the Timer with the given id.
     */
    public static void showNotification(Context context, long timerId) {
        Intent intent = new Intent(context, TimerNotificationService.class);
        intent.putExtra(EXTRA_TIMER_ID, timerId);
        context.startService(intent);
    }

    /**
     * Helper method to cancel the notification previously shown from calling
     * {@link #showNotification(Context, long)}. This does NOT start the Service
     * and call through to {@link #onHandleIntent(Intent)}.
     * @param timerId the id of the Timer associated with the notification
     *                you want to cancel
     */
    public static void cancelNotification(Context context, long timerId) {
        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(TAG, (int) timerId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTableManager = new TimersTableManager(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final long timerId = intent.getLongExtra(EXTRA_TIMER_ID, -1);
            if (timerId == -1) {
                throw new IllegalStateException("Did not pass in timer id");
            }
            final String action = intent.getAction();
            if (action == null) {
                showNotification(timerId);
            } else if (ACTION_ADD_ONE_MINUTE.equals(action)) {
                handleAddOneMinute(timerId);
            } else if (ACTION_START_PAUSE.equals(action)) {
                handleStartPause(timerId);
            } else if (ACTION_STOP.equals(action)) {
                handleStop(timerId);
            }
        }
    }

    private void showNotification(long timerId) {
        Timer timer = getTimer(timerId);

        // Base note
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                // TODO: correct icon
                .setSmallIcon(R.drawable.ic_half_day_1_black_24dp)
                .setShowWhen(false)
                .setOngoing(true);
        // TODO: Set content intent so that when clicked, we launch
        // TimersFragment and scroll to the given timer id. The following
        // is merely pseudocode.
        Intent contentIntent = new Intent(this, MainActivity.class);
        contentIntent.putExtra(null/*TODO:MainActivity.EXTRA_SHOW_PAGE*/,
                1/*TODO:The tab index of the timers page*/);
        contentIntent.putExtra(null/*TODO:MainActivity.EXTRA_SCROLL_TO_ID*/,
                timerId);
        builder.setContentIntent(PendingIntent.getActivity(
                this,
                0, // TODO: Request code not needed? Since any multiple notifications
                   // should be able to use the same PendingIntent for this action....
                   // unless the underlying *Intent* and its id extra are overwritten
                   // per notification when retrieving the PendingIntent..
                contentIntent,
                0/*Shouldn't need a flag..*/));
        // TODO: Use a handler to continually update the countdown text

        String title = timer.label();
        if (title.isEmpty()) {
            title = getString(R.string.timer);
        }
        builder.setContentTitle(title);

        addAction(builder, ACTION_ADD_ONE_MINUTE,
                timer.getId(), R.drawable.ic_add_circle_24dp/*TODO: correct icon*/);
        addAction(builder, ACTION_START_PAUSE,
                timer.getId(), R.drawable.ic_add_circle_24dp/*TODO: correct icon*/);
        addAction(builder, ACTION_STOP,
                timer.getId(), R.drawable.ic_add_circle_24dp/*TODO: correct icon*/);

        NotificationManager nm = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(TAG, timer.getIntId(), builder.build());
    }

    /**
     * Builds and adds the specified action to the notification's builder.
     */
    private void addAction(NotificationCompat.Builder noteBuilder, String action,
                           long timerId, @DrawableRes int icon) {
        Intent intent = new Intent(this, TimerNotificationService.class)
                .setAction(action)
                .putExtra(EXTRA_TIMER_ID, timerId);
        PendingIntent pi = PendingIntent.getService(this,
                (int) timerId, intent, 0/*no flags*/);
        noteBuilder.addAction(icon, ""/*no action title*/, pi);
    }

    private void handleAddOneMinute(long timerId) {
        Timer timer = getTimer(timerId);
        timer.addOneMinute();
        updateTimer(timer);
        // TODO: Verify the notification countdown is extended by one minute.
    }

    private void handleStartPause(long timerId) {
        Timer t = getTimer(timerId);
        TimerController.startPause(t);
        updateTimer(t);
    }

    private void handleStop(long timerId) {
        Timer t = getTimer(timerId);
        t.stop();
        updateTimer(t);
    }

    private void updateTimer(Timer timer) {
        mTableManager.updateItem(timer.getId(), timer);
    }

    private Timer getTimer(long timerId) {
        return mTableManager.queryItem(timerId).getItem();
    }
}
