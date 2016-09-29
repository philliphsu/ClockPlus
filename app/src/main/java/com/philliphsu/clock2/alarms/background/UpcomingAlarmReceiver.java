/*
 * Copyright (C) 2016 Phillip Hsu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import com.philliphsu.clock2.util.ContentIntentUtils;

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
                Notification note = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_alarm_24dp)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setContentIntent(ContentIntentUtils.create(context, MainActivity.PAGE_ALARMS, id))
                        .addAction(R.drawable.ic_dismiss_alarm_24dp, context.getString(R.string.dismiss_now), piDismiss)
                        .build();
                nm.notify(TAG, (int) id, note);
            }
        }
    }
}
