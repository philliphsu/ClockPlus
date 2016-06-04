package com.philliphsu.clock2.editalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.UpcomingAlarmReceiver;
import com.philliphsu.clock2.ringtone.RingtoneActivity;
import com.philliphsu.clock2.ringtone.RingtoneService;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.app.PendingIntent.FLAG_NO_CREATE;
import static android.app.PendingIntent.getActivity;
import static com.philliphsu.clock2.util.Preconditions.checkNotNull;

/**
 * Created by Phillip Hsu on 6/3/2016.
 *
 * Utilities for scheduling and unscheduling alarms with the {@link AlarmManager}, as well as
 * managing the upcoming alarm notification.
 *
 * TODO: Adapt this to Timers too...
 */
public final class AlarmUtils {

    private AlarmUtils() {}

    public static void scheduleAlarm(Context context, Alarm alarm) {
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
        // todo: read shared prefs for number of hours to be notified in advance
        long ringAt = alarm.isSnoozed() ? alarm.snoozingUntil() : alarm.ringsAt();
        // If snoozed, upcoming note posted immediately.
        am.set(AlarmManager.RTC_WAKEUP, ringAt - 2*3600000, notifyUpcomingAlarmIntent(context, alarm, false));
        am.setExact(AlarmManager.RTC_WAKEUP, ringAt, alarmIntent(context, alarm, false));
    }

    public static void cancelAlarm(Context c, Alarm a) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pi = alarmIntent(c, a, true);
        am.cancel(pi);
        pi.cancel();

        pi = notifyUpcomingAlarmIntent(c, a, true);
        am.cancel(pi);
        pi.cancel();

        removeUpcomingAlarmNotification(c, a);

        // If service is not running, nothing happens
        c.stopService(new Intent(c, RingtoneService.class));
    }

    public static void removeUpcomingAlarmNotification(Context c, Alarm a) {
        Intent intent = new Intent(c, UpcomingAlarmReceiver.class)
                .setAction(UpcomingAlarmReceiver.ACTION_CANCEL_NOTIFICATION)
                .putExtra(UpcomingAlarmReceiver.EXTRA_ALARM_ID, a.id());
        c.sendBroadcast(intent);
    }

    private static PendingIntent alarmIntent(Context context, Alarm alarm, boolean retrievePrevious) {
        // TODO: Use appropriate subclass instead
        Intent intent = new Intent(context, RingtoneActivity.class)
                .putExtra(RingtoneActivity.EXTRA_ITEM_ID, alarm.id());
        int flag = retrievePrevious ? FLAG_NO_CREATE : FLAG_CANCEL_CURRENT;
        PendingIntent pi = getActivity(context, alarm.intId(), intent, flag);
        if (retrievePrevious) {
            checkNotNull(pi);
        }
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
        if (retrievePrevious) {
            checkNotNull(pi);
        }
        return pi;
    }
}
