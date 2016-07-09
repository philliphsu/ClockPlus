package com.philliphsu.clock2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import com.philliphsu.clock2.model.DatabaseManager;
import com.philliphsu.clock2.util.AlarmUtils;

import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static com.philliphsu.clock2.util.DateFormatUtils.formatTime;
import static com.philliphsu.clock2.util.Preconditions.checkNotNull;

// TODO: Consider registering this locally instead of in the manifest.
public class UpcomingAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "UpcomingAlarmReceiver";
    /*TOneverDO: not private*/
    private static final String ACTION_DISMISS_NOW = "com.philliphsu.clock2.action.DISMISS_NOW";

    public static final String ACTION_CANCEL_NOTIFICATION = "com.philliphsu.clock2.action.CANCEL_NOTIFICATION";
    public static final String ACTION_SHOW_SNOOZING = "com.philliphsu.clock2.action.SHOW_SNOOZING";
    public static final String EXTRA_ALARM_ID = "com.philliphsu.clock2.extra.ALARM_ID";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final long id = intent.getLongExtra(EXTRA_ALARM_ID, -1);
        if (id < 0) {
            throw new IllegalStateException("No alarm id received");
        }

        final NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (ACTION_CANCEL_NOTIFICATION.equals(intent.getAction())) {
            nm.cancel(TAG, (int) id);
        } else {
            new AsyncTask<Void, Void, Alarm>() {
                @Override
                protected Alarm doInBackground(Void... params) {
                    return checkNotNull(DatabaseManager.getInstance(context).getAlarm(id));
                }

                @Override
                protected void onPostExecute(Alarm alarm) {
                    if (ACTION_DISMISS_NOW.equals(intent.getAction())) {
                        // This MUST be done on the UI thread.
                        AlarmUtils.cancelAlarm(context, alarm, true);
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

                        Intent in = new Intent(context, UpcomingAlarmReceiver.class)
                                .putExtra(EXTRA_ALARM_ID, id) // TOneverDO: cast to int
                                .setAction(ACTION_DISMISS_NOW);
                        PendingIntent pi = PendingIntent.getBroadcast(context, (int) id, in, FLAG_ONE_SHOT);
                        Notification note = new NotificationCompat.Builder(context)
                                .setSmallIcon(R.mipmap.ic_launcher) // TODO: alarm icon
                                .setContentTitle(title)
                                .setContentText(text)
                                .setOngoing(true)
                                .addAction(R.mipmap.ic_launcher, context.getString(R.string.dismiss_now), pi)
                                .build();
                        nm.notify(TAG, (int) id, note);
                    }
                }
            }.execute();
        }
    }
}
