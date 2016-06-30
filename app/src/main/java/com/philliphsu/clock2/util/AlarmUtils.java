package com.philliphsu.clock2.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.util.Log;
import android.widget.Toast;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.PendingAlarmScheduler;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.UpcomingAlarmReceiver;
import com.philliphsu.clock2.model.DatabaseManager;
import com.philliphsu.clock2.ringtone.RingtoneActivity;
import com.philliphsu.clock2.ringtone.RingtoneService;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.app.PendingIntent.FLAG_NO_CREATE;
import static android.app.PendingIntent.getActivity;
import static com.philliphsu.clock2.util.DateFormatUtils.formatTime;
import static java.util.concurrent.TimeUnit.HOURS;

/**
 * Created by Phillip Hsu on 6/3/2016.
 *
 * Utilities for scheduling and unscheduling alarms with the {@link AlarmManager}, as well as
 * managing the upcoming alarm notification.
 *
 * TODO: Adapt this to Timers too...
 */
public final class AlarmUtils {
    private static final String TAG = "AlarmUtils";

    private AlarmUtils() {}

    public static void scheduleAlarm(Context context, Alarm alarm, boolean showToast) {
        Log.d(TAG, "Scheduling alarm " + alarm);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // If there is already an alarm for this Intent scheduled (with the equality of two
        // intents being defined by filterEquals(Intent)), then it will be removed and replaced
        // by this one. For most of our uses, the relevant criteria for equality will be the
        // action, the data, and the class (component). Although not documented, the request code
        // of a PendingIntent is also considered to determine equality of two intents.

        // WAKEUP alarm types wake the CPU up, but NOT the screen. If that is what you want, you need
        // to handle that yourself by using a wakelock, etc..
        // We use a WAKEUP alarm to send the upcoming alarm notification so it goes off even if the
        // device is asleep. Otherwise, it will not go off until the device is turned back on.
        long ringAt = alarm.isSnoozed() ? alarm.snoozingUntil() : alarm.ringsAt();
        // If snoozed, upcoming note posted immediately.
        am.set(AlarmManager.RTC_WAKEUP, ringAt - HOURS.toMillis(hoursBeforeUpcoming(context)),
                notifyUpcomingAlarmIntent(context, alarm, false));
        am.setExact(AlarmManager.RTC_WAKEUP, ringAt, alarmIntent(context, alarm, false));

        if (showToast) {
            String message;
            if (alarm.isSnoozed()) {
                message = context.getString(R.string.title_snoozing_until,
                        formatTime(context, alarm.snoozingUntil()));
            } else {
                message = context.getString(R.string.alarm_set_for,
                        DurationUtils.toString(context, alarm.ringsIn(), false /*abbreviate?*/));
            }
            // TODO: Will toasts show for any Context? e.g. IntentService can't do anything on UI thread.
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    public static void cancelAlarm(Context c, Alarm a, boolean showToast) {
        Log.d(TAG, "Cancelling alarm " + a);
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pi = alarmIntent(c, a, true);
        if (pi != null) {
            am.cancel(pi);
            pi.cancel();
        }

        pi = notifyUpcomingAlarmIntent(c, a, true);
        if (pi != null) {
            am.cancel(pi);
            pi.cancel();
        }

        removeUpcomingAlarmNotification(c, a);

        // TOneverDO: Place block after making value changes to the alarm.
        if (showToast && (a.ringsWithinHours(hoursBeforeUpcoming(c)) || a.isSnoozed())) {
            String time = formatTime(c, a.isSnoozed() ? a.snoozingUntil() : a.ringsAt());
            String text = c.getString(R.string.upcoming_alarm_dismissed, time);
            Toast.makeText(c, text, Toast.LENGTH_LONG).show();
        }

        if (a.isSnoozed()) {
            a.stopSnoozing();
        }

        if (!a.hasRecurrence()) {
            a.setEnabled(false);
        } else {
            if (a.isEnabled()) {
                if (a.ringsWithinHours(hoursBeforeUpcoming(c))) {
                    // Still upcoming today, so wait until the normal ring time passes before
                    // rescheduling the alarm.
                    Intent intent = new Intent(c, PendingAlarmScheduler.class)
                            .putExtra(PendingAlarmScheduler.EXTRA_ALARM_ID, a.id());
                    pi = PendingIntent.getBroadcast(c, a.intId(), intent, PendingIntent.FLAG_ONE_SHOT);
                    am.set(AlarmManager.RTC_WAKEUP, a.ringsAt(), pi);
                } else {
                    scheduleAlarm(c, a, false);
                }
            }
        }

        save(c, a);

        // If service is not running, nothing happens
        c.stopService(new Intent(c, RingtoneService.class));
    }

    public static void snoozeAlarm(Context c, Alarm a) {
        a.snooze(snoozeDuration(c));
        scheduleAlarm(c, a, true);
        save(c, a);
    }

    public static void removeUpcomingAlarmNotification(Context c, Alarm a) {
        Intent intent = new Intent(c, UpcomingAlarmReceiver.class)
                .setAction(UpcomingAlarmReceiver.ACTION_CANCEL_NOTIFICATION)
                .putExtra(UpcomingAlarmReceiver.EXTRA_ALARM_ID, a.id());
        c.sendBroadcast(intent);
    }

    public static int snoozeDuration(Context c) {
        return readPreference(c, R.string.key_snooze_duration, 10);
    }

    // TODO: Consider renaming to hoursToNotifyInAdvance()
    public static int hoursBeforeUpcoming(Context c) {
        return readPreference(c, R.string.key_notify_me_of_upcoming_alarms, 2);
    }

    public static int minutesToSilenceAfter(Context c) {
        return readPreference(c, R.string.key_silence_after, 15);
    }

    public static int firstDayOfWeek(Context c) {
        return readPreference(c, R.string.key_first_day_of_week, 0 /* Sunday */);
    }

    public static int readPreference(Context c, @StringRes int key, int defaultValue) {
        String value = PreferenceManager.getDefaultSharedPreferences(c).getString(c.getString(key), null);
        return null == value ? defaultValue : Integer.parseInt(value);
    }

    private static PendingIntent alarmIntent(Context context, Alarm alarm, boolean retrievePrevious) {
        // TODO: Use appropriate subclass instead
        Intent intent = new Intent(context, RingtoneActivity.class)
                .putExtra(RingtoneActivity.EXTRA_ITEM_ID, alarm.id());
        int flag = retrievePrevious ? FLAG_NO_CREATE : FLAG_CANCEL_CURRENT;
        PendingIntent pi = getActivity(context, alarm.intId(), intent, flag);
        // Even when we try to retrieve a previous instance that actually did exist,
        // null can be returned for some reason.
/*
        if (retrievePrevious) {
            checkNotNull(pi);
        }
*/
        return pi;
    }

    private static PendingIntent notifyUpcomingAlarmIntent(Context context, Alarm alarm, boolean retrievePrevious) {
        Intent intent = new Intent(context, UpcomingAlarmReceiver.class)
                .putExtra(UpcomingAlarmReceiver.EXTRA_ALARM_ID, alarm.id());
        if (alarm.isSnoozed()) {
            // TODO: Will this affect retrieving a previous instance? Say if the previous instance
            // didn't have this action set initially, but at a later time we made a new instance
            // with it set.
            intent.setAction(UpcomingAlarmReceiver.ACTION_SHOW_SNOOZING);
        }
        int flag = retrievePrevious ? FLAG_NO_CREATE : FLAG_CANCEL_CURRENT;
        PendingIntent pi = PendingIntent.getBroadcast(context, alarm.intId(), intent, flag);
        // Even when we try to retrieve a previous instance that actually did exist,
        // null can be returned for some reason.
/*
        if (retrievePrevious) {
            checkNotNull(pi);
        }
*/
        return pi;
    }

    private static void save(Context c, Alarm alarm) {
//        AlarmsRepository.getInstance(c).saveItems();
        // Update the same alarm
        DatabaseManager.getInstance(c).updateAlarm(alarm.id(), alarm);
    }
}
