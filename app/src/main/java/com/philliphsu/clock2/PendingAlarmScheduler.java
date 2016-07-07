package com.philliphsu.clock2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.philliphsu.clock2.model.DatabaseManager;
import com.philliphsu.clock2.util.AlarmUtils;

import static com.philliphsu.clock2.util.Preconditions.checkNotNull;

/**
 * Used to reschedule recurring alarms that were dismissed in their upcoming state, so {@link Alarm#ringsAt()}
 * still refers to the time it rings today. This class receives
 * your intent at the Alarm instance's normal ring time, so by the time you make a subsequent call
 * to {@link Alarm#ringsAt()}, the value returned refers to the next time the alarm will recur.
 */
public class PendingAlarmScheduler extends BroadcastReceiver {
    // We include the class name in the string to distinguish this constant from the one defined
    // in UpcomingAlarmReceiver.
    public static final String EXTRA_ALARM_ID = "com.philliphsu.clock2.PendingAlarmScheduler.extra.ALARM_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(EXTRA_ALARM_ID, -1);
        if (id < 0) {
            throw new IllegalStateException("No alarm id received");
        }
        // TODO: Do this in the background. AsyncTask?
        Alarm alarm = checkNotNull(DatabaseManager.getInstance(context).getAlarm(id));
        if (!alarm.isEnabled()) {
            throw new IllegalStateException("Alarm must be enabled!");
        }
        AlarmUtils.scheduleAlarm(context, alarm, false);
    }
}
