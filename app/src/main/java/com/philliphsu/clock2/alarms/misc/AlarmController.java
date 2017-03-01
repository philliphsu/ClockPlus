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

package com.philliphsu.clock2.alarms.misc;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.philliphsu.clock2.MainActivity;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.alarms.Alarm;
import com.philliphsu.clock2.alarms.background.PendingAlarmScheduler;
import com.philliphsu.clock2.alarms.background.UpcomingAlarmReceiver;
import com.philliphsu.clock2.alarms.data.AlarmsTableManager;
import com.philliphsu.clock2.ringtone.AlarmActivity;
import com.philliphsu.clock2.ringtone.playback.AlarmRingtoneService;
import com.philliphsu.clock2.util.ContentIntentUtils;
import com.philliphsu.clock2.util.DelayedSnackbarHandler;
import com.philliphsu.clock2.util.DurationUtils;
import com.philliphsu.clock2.util.ParcelableUtil;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.app.PendingIntent.FLAG_NO_CREATE;
import static android.app.PendingIntent.getActivity;
import static com.philliphsu.clock2.util.TimeFormatUtils.formatTime;
import static java.util.concurrent.TimeUnit.HOURS;

/**
 * Created by Phillip Hsu on 7/10/2016.
 *
 * API to control alarm states and update the UI.
 * TODO: Rename to AlarmStateHandler? AlarmStateController?
 */
public final class AlarmController {
    private static final String TAG = "AlarmController";

    private final Context mAppContext;
    private final View mSnackbarAnchor;
    // TODO: Why aren't we using AsyncAlarmsTableUpdateHandler?
    private final AlarmsTableManager mTableManager;

    /**
     *
     * @param context the Context from which the application context will be requested
     * @param snackbarAnchor an optional anchor for a Snackbar to anchor to
     */
    public AlarmController(Context context, View snackbarAnchor) {
        mAppContext = context.getApplicationContext();
        mSnackbarAnchor = snackbarAnchor;
        mTableManager = new AlarmsTableManager(context);
    }

    /**
     * Schedules the alarm with the {@link AlarmManager}.
     * If {@code alarm.}{@link Alarm#isEnabled() isEnabled()}
     * returns false, this does nothing and returns immediately.
     * 
     * If there is already an alarm for this Intent scheduled (with the equality of two
     * intents being defined by filterEquals(Intent)), then it will be removed and replaced
     * by this one. For most of our uses, the relevant criteria for equality will be the
     * action, the data, and the class (component). Although not documented, the request code
     * of a PendingIntent is also considered to determine equality of two intents.
     */
    public void scheduleAlarm(Alarm alarm, boolean showSnackbar) {
        if (!alarm.isEnabled()) {
            return;
        }
        // Does nothing if it's not posted. This is primarily here for when alarms
        // are updated, instead of newly created, so that we don't leave behind
        // stray upcoming alarm notifications. This occurs e.g. when a single-use
        // alarm is updated to recur on a weekday later than the current day.
        removeUpcomingAlarmNotification(alarm);

        AlarmManager am = (AlarmManager) mAppContext.getSystemService(Context.ALARM_SERVICE);

        final long ringAt = alarm.isSnoozed() ? alarm.snoozingUntil() : alarm.ringsAt();
        final PendingIntent alarmIntent = alarmIntent(alarm, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            PendingIntent showIntent = ContentIntentUtils.create(mAppContext, MainActivity.PAGE_ALARMS, alarm.getId());
            AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(ringAt, showIntent);
            am.setAlarmClock(info, alarmIntent);
        } else {
            // WAKEUP alarm types wake the CPU up, but NOT the screen;
            // you would handle that yourself by using a wakelock, etc..
            am.setExact(AlarmManager.RTC_WAKEUP, ringAt, alarmIntent);
            // Show alarm in the status bar
            Intent alarmChanged = new Intent("android.intent.action.ALARM_CHANGED");
            alarmChanged.putExtra("alarmSet", true/*enabled*/);
            mAppContext.sendBroadcast(alarmChanged);
        }

        final int hoursToNotifyInAdvance = AlarmPreferences.hoursBeforeUpcoming(mAppContext);
        if (hoursToNotifyInAdvance > 0 || alarm.isSnoozed()) {
            // If snoozed, upcoming note posted immediately.
            long upcomingAt = ringAt - HOURS.toMillis(hoursToNotifyInAdvance);
            // We use a WAKEUP alarm to send the upcoming alarm notification so it goes off even if the
            // device is asleep. Otherwise, it will not go off until the device is turned back on.
            am.set(AlarmManager.RTC_WAKEUP, upcomingAt, notifyUpcomingAlarmIntent(alarm, false));
        }

        if (showSnackbar) {
            String message = mAppContext.getString(R.string.alarm_set_for,
                    DurationUtils.toString(mAppContext, alarm.ringsIn(), false/*abbreviate*/));
            showSnackbar(message);
        }
    }

    /**
     * Cancel the alarm. This does NOT check if you previously scheduled the alarm.
     * @param rescheduleIfRecurring True if the alarm should be rescheduled after cancelling.
     *                              This param will only be considered if the alarm has recurrence
     *                              and is enabled.
     */
    public void cancelAlarm(Alarm alarm, boolean showSnackbar, boolean rescheduleIfRecurring) {
        Log.d(TAG, "Cancelling alarm " + alarm);
        AlarmManager am = (AlarmManager) mAppContext.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pi = alarmIntent(alarm, true);
        if (pi != null) {
            am.cancel(pi);
            pi.cancel();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                // Remove alarm in the status bar
                Intent alarmChanged = new Intent("android.intent.action.ALARM_CHANGED");
                alarmChanged.putExtra("alarmSet", false/*enabled*/);
                mAppContext.sendBroadcast(alarmChanged);
            }
        }

        pi = notifyUpcomingAlarmIntent(alarm, true);
        if (pi != null) {
            am.cancel(pi);
            pi.cancel();
        }

        // Does nothing if it's not posted.
        removeUpcomingAlarmNotification(alarm);

        final int hoursToNotifyInAdvance = AlarmPreferences.hoursBeforeUpcoming(mAppContext);
        // ------------------------------------------------------------------------------------
        // TOneverDO: Place block after making value changes to the alarm.
        if ((hoursToNotifyInAdvance > 0 && showSnackbar
                // TODO: Consider showing the snackbar for non-upcoming alarms too;
                // then, we can remove these checks.
                && alarm.ringsWithinHours(hoursToNotifyInAdvance)) || alarm.isSnoozed()) {
            long time = alarm.isSnoozed() ? alarm.snoozingUntil() : alarm.ringsAt();
            String msg = mAppContext.getString(R.string.upcoming_alarm_dismissed,
                    formatTime(mAppContext, time));
            showSnackbar(msg);
        }
        // ------------------------------------------------------------------------------------

        if (alarm.isSnoozed()) {
            alarm.stopSnoozing();
        }

        if (!alarm.hasRecurrence()) {
            alarm.setEnabled(false);
        } else if (alarm.isEnabled() && rescheduleIfRecurring) {
            if (alarm.ringsWithinHours(hoursToNotifyInAdvance)) {
                // Still upcoming today, so wait until the normal ring time
                // passes before rescheduling the alarm.
                alarm.ignoreUpcomingRingTime(true); // Useful only for VH binding
                Intent intent = new Intent(mAppContext, PendingAlarmScheduler.class)
                        .putExtra(PendingAlarmScheduler.EXTRA_ALARM_ID, alarm.getId());
                pi = PendingIntent.getBroadcast(mAppContext, alarm.getIntId(),
                        intent, PendingIntent.FLAG_ONE_SHOT);
                am.set(AlarmManager.RTC_WAKEUP, alarm.ringsAt(), pi);
            } else {
                scheduleAlarm(alarm, false);
            }
        }

        save(alarm);

        // If service is not running, nothing happens
        mAppContext.stopService(new Intent(mAppContext, AlarmRingtoneService.class));
    }

    public void snoozeAlarm(Alarm alarm) {
        int minutesToSnooze = AlarmPreferences.snoozeDuration(mAppContext);
        alarm.snooze(minutesToSnooze);
        scheduleAlarm(alarm, false);
        String message = mAppContext.getString(R.string.title_snoozing_until,
                formatTime(mAppContext, alarm.snoozingUntil()));
        // Since snoozing is always done by an app component away from
        // the list screen, the Snackbar will never be shown. In fact, this
        // controller has a null mSnackbarAnchor if we're using it for snoozing
        // an alarm. We solve this by preparing the message, and waiting until
        // the list screen is resumed so that it can display the Snackbar for us.
        DelayedSnackbarHandler.prepareMessage(message);
        save(alarm);
    }

    public void removeUpcomingAlarmNotification(Alarm a) {
        Intent intent = new Intent(mAppContext, UpcomingAlarmReceiver.class)
                .setAction(UpcomingAlarmReceiver.ACTION_CANCEL_NOTIFICATION)
                .putExtra(UpcomingAlarmReceiver.EXTRA_ALARM, ParcelableUtil.marshall(a));
        mAppContext.sendBroadcast(intent);
    }

    public void save(final Alarm alarm) {
        // TODO: Will using the Runnable like this cause a memory leak?
        new Thread(new Runnable() {
            @Override
            public void run() {
                mTableManager.updateItem(alarm.getId(), alarm);
            }
        }).start();
    }

    private PendingIntent alarmIntent(Alarm alarm, boolean retrievePrevious) {
        Intent intent = new Intent(mAppContext, AlarmActivity.class)
                .putExtra(AlarmActivity.EXTRA_RINGING_OBJECT, ParcelableUtil.marshall(alarm));
        int flag = retrievePrevious ? FLAG_NO_CREATE : FLAG_CANCEL_CURRENT;
        // Even when we try to retrieve a previous instance that actually did exist,
        // null can be returned for some reason. Thus, we don't checkNotNull().
        return getActivity(mAppContext, alarm.getIntId(), intent, flag);
    }

    private PendingIntent notifyUpcomingAlarmIntent(Alarm alarm, boolean retrievePrevious) {
        Intent intent = new Intent(mAppContext, UpcomingAlarmReceiver.class)
                .putExtra(UpcomingAlarmReceiver.EXTRA_ALARM, ParcelableUtil.marshall(alarm));
        if (alarm.isSnoozed()) {
            intent.setAction(UpcomingAlarmReceiver.ACTION_SHOW_SNOOZING);
        }
        int flag = retrievePrevious ? FLAG_NO_CREATE : FLAG_CANCEL_CURRENT;
        // Even when we try to retrieve a previous instance that actually did exist,
        // null can be returned for some reason. Thus, we don't checkNotNull().
        return PendingIntent.getBroadcast(mAppContext, alarm.getIntId(), intent, flag);
    }

    private void showSnackbar(final String message) {
        // Is the window containing this anchor currently focused?
//        Log.d(TAG, "Anchor has window focus? " + mSnackbarAnchor.hasWindowFocus());
        if (mSnackbarAnchor != null /*&& mSnackbarAnchor.hasWindowFocus()*/) {
            // Queue the message on the view's message loop, so the message
            // gets processed once the view gets attached to the window.
            // This executes on the UI thread, just like not queueing it will,
            // but the difference here is we wait for the view to be attached
            // to the window (if not already) before executing the runnable code.
            mSnackbarAnchor.post(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(mSnackbarAnchor, message, Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }
}
