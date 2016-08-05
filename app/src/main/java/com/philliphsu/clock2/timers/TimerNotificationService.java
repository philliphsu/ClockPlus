package com.philliphsu.clock2.timers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.philliphsu.clock2.AsyncTimersTableUpdateHandler;
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
public class TimerNotificationService extends Service {
    private static final String TAG = "TimerNotificationService";

    public static final String ACTION_ADD_ONE_MINUTE = "com.philliphsu.clock2.timers.action.ADD_ONE_MINUTE";
    public static final String ACTION_START_PAUSE = "com.philliphsu.clock2.timers.action.START_PAUSE";
    public static final String ACTION_STOP = "com.philliphsu.clock2.timers.action.STOP";

    public static final String EXTRA_TIMER = "com.philliphsu.clock2.timers.extra.TIMER";

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
    public static void cancelNotification(Context context, long timerId) {
        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(TAG, (int) timerId);
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
                // TODO: Spawn your own thread to update the countdown text
                showNotification();
            } else if (ACTION_ADD_ONE_MINUTE.equals(action)) {
                mController.addOneMinute();
                // TODO: Verify the notification countdown is extended by one minute.
            } else if (ACTION_START_PAUSE.equals(action)) {
                mController.startPause();
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

    private void showNotification() {
        // Base note
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_half_day_1_black_24dp) // TODO: correct icon
                .setShowWhen(false)
                .setOngoing(true);
        // TODO: Set content intent so that when clicked, we launch
        // TimersFragment and scroll to the given timer id. The following
        // is merely pseudocode.
        Intent contentIntent = new Intent(this, MainActivity.class);
        contentIntent.putExtra(null/*TODO:MainActivity.EXTRA_SHOW_PAGE*/, 1/*TODO:The tab index of the timers page*/);
        contentIntent.putExtra(null/*TODO:MainActivity.EXTRA_SCROLL_TO_ID*/, mTimer.getId());
        builder.setContentIntent(PendingIntent.getActivity(
                this,
                0, // TODO: Request code not needed? Since any multiple notifications
                // should be able to use the same PendingIntent for this action....
                // unless the underlying *Intent* and its id extra are overwritten
                // per notification when retrieving the PendingIntent..
                contentIntent,
                0/*Shouldn't need a flag..*/));
        // TODO: Use a handler to continually update the countdown text

        String title = mTimer.label();
        if (title.isEmpty()) {
            title = getString(R.string.timer);
        }
        builder.setContentTitle(title);

        addAction(builder, ACTION_ADD_ONE_MINUTE,
                R.drawable.ic_add_circle_24dp/*TODO: correct icon*/);
        addAction(builder, ACTION_START_PAUSE,
                R.drawable.ic_add_circle_24dp/*TODO: correct icon*/);
        addAction(builder, ACTION_STOP,
                R.drawable.ic_add_circle_24dp/*TODO: correct icon*/);

        NotificationManager nm = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(TAG, mTimer.getIntId(), builder.build());
    }

    /**
     * Builds and adds the specified action to the notification's builder.
     */
    private void addAction(NotificationCompat.Builder noteBuilder, String action, @DrawableRes int icon) {
        Intent intent = new Intent(this, TimerNotificationService.class)
                .setAction(action);
//                .putExtra(EXTRA_TIMER, mTimer);
        PendingIntent pi = PendingIntent.getService(this,
                mTimer.getIntId(), intent, 0/*no flags*/);
        noteBuilder.addAction(icon, ""/*no action title*/, pi);
    }
}