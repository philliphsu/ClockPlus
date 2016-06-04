package com.philliphsu.clock2;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.philliphsu.clock2.model.AlarmsRepository;

import static com.philliphsu.clock2.util.DateFormatUtils.formatTime;
import static com.philliphsu.clock2.util.Preconditions.checkNotNull;

public class UpcomingAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "UpcomingAlarmReceiver";

    public static final String ACTION_CANCEL_NOTIFICATION
            = "com.philliphsu.clock2.action.CANCEL_NOTIFICATION";
    public static final String ACTION_SHOW_SNOOZING
            = "com.philliphsu.clock2.action.SHOW_SNOOZING";
    public static final String EXTRA_ALARM_ID
            = "com.philliphsu.clock2.extra.ALARM_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(EXTRA_ALARM_ID, -1);
        if (id < 0) {
            throw new IllegalStateException("No alarm id received");
        }

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (ACTION_CANCEL_NOTIFICATION.equals(intent.getAction())) {
            nm.cancel(getClass().getName(), (int) id);
        } else {
            Alarm alarm = checkNotNull(AlarmsRepository.getInstance(context).getItem(id));
            if (ACTION_SHOW_SNOOZING.equals(intent.getAction())) {
                if (!alarm.isSnoozed()) {
                    throw new IllegalStateException("Can't show snoozing notif. if alarm not snoozed!");
                }
                String title = alarm.label().isEmpty()
                        ? context.getString(R.string.alarm)
                        : alarm.label();
                String text = context.getString(R.string.title_snoozing_until,
                        formatTime(context, alarm.snoozingUntil()));
                Notification note = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher) // TODO: alarm icon
                        .setContentTitle(title)
                        .setContentText(text)
                        .setOngoing(true)
                        .build();
                // todo actions
                nm.notify(getClass().getName(), alarm.intId(), note);
            } else {
                // No intent action required for default behavior
                String text = formatTime(context, alarm.ringsAt());
                if (!alarm.label().isEmpty()) {
                    text = alarm.label() + ", " + text;
                }
                Notification note = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(context.getString(R.string.upcoming_alarm))
                        .setContentText(text)
                        .setOngoing(true)
                        .build();
                // todo actions
                nm.notify(getClass().getName(), alarm.intId(), note);
            }
        }
    }
}
