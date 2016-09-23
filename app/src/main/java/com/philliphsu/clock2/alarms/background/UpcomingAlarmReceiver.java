package com.philliphsu.clock2.alarms.background;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.philliphsu.clock2.MainActivity;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.alarms.Alarm;
import com.philliphsu.clock2.alarms.misc.AlarmController;
import com.philliphsu.clock2.alarms.ui.AlarmsFragment;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static com.philliphsu.clock2.util.TimeFormatUtils.formatTime;

// TODO: Consider registering this locally instead of in the manifest.
public class UpcomingAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "UpcomingAlarmReceiver";
    /*TOneverDO: not private*/
    private static final String ACTION_DISMISS_NOW = "com.philliphsu.clock2.action.DISMISS_NOW";

    public static final String ACTION_CANCEL_NOTIFICATION = "com.philliphsu.clock2.action.CANCEL_NOTIFICATION";
    public static final String ACTION_SHOW_SNOOZING = "com.philliphsu.clock2.action.SHOW_SNOOZING";
    public static final String EXTRA_ALARM = "com.philliphsu.clock2.extra.ALARM";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final Alarm alarm = intent.getParcelableExtra(EXTRA_ALARM);
        if (alarm == null) {
            throw new IllegalStateException("No alarm received");
        }

        final long id = alarm.getId();
        final NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (ACTION_CANCEL_NOTIFICATION.equals(intent.getAction())) {
            nm.cancel(TAG, (int) id);
        } else {
            if (ACTION_DISMISS_NOW.equals(intent.getAction())) {
                new AlarmController(context, null).cancelAlarm(alarm, false, true);
            } else {
                // Prepare notification
                // http://stackoverflow.com/a/15803726/5055032
                // Notifications aren't updated on the UI thread, so we could have
                // done this in the background. However, no lengthy operations are
                // done here, so doing so is a premature optimization.
                String title;
                String text;
                if (ACTION_SHOW_SNOOZING.equals(intent.getAction())) {
                    if (!alarm.isSnoozed())
                        throw new IllegalStateException("Can't show snoozing notif. if alarm not snoozed!");
                    title = alarm.label().isEmpty() ? context.getString(R.string.alarm) : alarm.label();
                    text = context.getString(R.string.title_snoozing_until,
                            formatTime(context, alarm.snoozingUntil()));
                } else {
                    // No intent action required for default behavior
                    title = context.getString(R.string.upcoming_alarm);
                    text = formatTime(context, alarm.ringsAt());
                    if (!alarm.label().isEmpty()) {
                        text = alarm.label() + ", " + text;
                    }
                }

                Intent dismissIntent = new Intent(context, UpcomingAlarmReceiver.class)
                        .setAction(ACTION_DISMISS_NOW)
                        .putExtra(EXTRA_ALARM, alarm);
                PendingIntent piDismiss = PendingIntent.getBroadcast(context, (int) id, dismissIntent, FLAG_ONE_SHOT);
                Intent contentIntent = new Intent(context, MainActivity.class)
                        // http://stackoverflow.com/a/3128418/5055032
                        // "For some unspecified reason, extras will be delivered only if you've set some action"
                        // This ONLY applies to PendingIntents...
                        // And for another unspecified reason, this dummy action must NOT be the same value
                        // as another PendingIntent's dummy action.
                        .setAction("abc")
                        .putExtra(MainActivity.EXTRA_SHOW_PAGE, MainActivity.PAGE_ALARMS)
                        .putExtra(AlarmsFragment.EXTRA_SCROLL_TO_ALARM_ID, id);
                PendingIntent piContent = PendingIntent.getActivity(context, (int) id, contentIntent, FLAG_CANCEL_CURRENT);
                Notification note = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_alarm_24dp)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setContentIntent(piContent)
                        .setOngoing(true)
                        .addAction(R.drawable.ic_dismiss_alarm_24dp, context.getString(R.string.dismiss_now), piDismiss)
                        .build();
                nm.notify(TAG, (int) id, note);
            }
        }
    }
}
