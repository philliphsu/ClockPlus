package com.philliphsu.clock2.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.PendingAlarmScheduler;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.UpcomingAlarmReceiver;
import com.philliphsu.clock2.alarms.AlarmActivity;
import com.philliphsu.clock2.alarms.AlarmRingtoneService;
import com.philliphsu.clock2.model.AlarmsTableManager;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.app.PendingIntent.FLAG_NO_CREATE;
import static android.app.PendingIntent.getActivity;
import static com.philliphsu.clock2.util.DateFormatUtils.formatTime;
import static java.util.concurrent.TimeUnit.HOURS;

/**
 * Created by Phillip Hsu on 7/10/2016.
 *
 * API to control alarm states and update the UI.
 * TODO: Move this out of the .utils package when done.
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
     */
    public void scheduleAlarm(Alarm alarm, boolean showSnackbar) {
        if (!alarm.isEnabled()) {
            Log.i(TAG, "Skipped scheduling an alarm because it was not enabled");
            return;
        }

        // TODO: Consider doing this in a new thread.
        Log.d(TAG, "Scheduling alarm " + alarm);
        AlarmManager am = (AlarmManager) mAppContext.getSystemService(Context.ALARM_SERVICE);
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
        int hoursToNotifyInAdvance = AlarmUtils.hoursBeforeUpcoming(mAppContext);
        long upcomingAt = ringAt - HOURS.toMillis(hoursToNotifyInAdvance);
        // If snoozed, upcoming note posted immediately.
        am.set(AlarmManager.RTC_WAKEUP, upcomingAt, notifyUpcomingAlarmIntent(alarm, false));
        am.setExact(AlarmManager.RTC_WAKEUP, ringAt, alarmIntent(alarm, false));

        if (showSnackbar) {
            String message = mAppContext.getString(R.string.alarm_set_for,
                    DurationUtils.toString(mAppContext, alarm.ringsIn(), false /*abbreviate?*/));
            // TODO: Consider adding delay to allow the alarm item animation
            // to finish first before we show the snackbar. Inbox app does this.
            showSnackbar(message);
        }
    }

    /**
     * Cancel the alarm. This does NOT check if you previously scheduled the alarm.
     */
    public void cancelAlarm(Alarm alarm, boolean showSnackbar) {
        // TODO: Consider doing this in a new thread.
        Log.d(TAG, "Cancelling alarm " + alarm);
        AlarmManager am = (AlarmManager) mAppContext.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pi = alarmIntent(alarm, true);
        if (pi != null) {
            am.cancel(pi);
            pi.cancel();
        }

        pi = notifyUpcomingAlarmIntent(alarm, true);
        if (pi != null) {
            am.cancel(pi);
            pi.cancel();
        }

        // Does nothing if it's not posted.
        removeUpcomingAlarmNotification(alarm);

        int hoursToNotifyInAdvance = AlarmUtils.hoursBeforeUpcoming(mAppContext);
        // TOneverDO: Place block after making value changes to the alarm.
        if (showSnackbar
                // TODO: Consider showing the snackbar for non-upcoming alarms too;
                // then, we can remove these checks.
                && alarm.ringsWithinHours(hoursToNotifyInAdvance) || alarm.isSnoozed()) {
            long time = alarm.isSnoozed() ? alarm.snoozingUntil() : alarm.ringsAt();
            String msg = mAppContext.getString(R.string.upcoming_alarm_dismissed,
                    formatTime(mAppContext, time));
            showSnackbar(msg);
        }

        if (alarm.isSnoozed()) {
            alarm.stopSnoozing();
        }

        if (!alarm.hasRecurrence()) {
            alarm.setEnabled(false);
        } else if (alarm.isEnabled()) {
            if (alarm.ringsWithinHours(hoursToNotifyInAdvance)) {
                // Still upcoming today, so wait until the normal ring time
                // passes before rescheduling the alarm.
                alarm.ignoreUpcomingRingTime(true); // Useful only for VH binding
                Intent intent = new Intent(mAppContext, PendingAlarmScheduler.class)
                        .putExtra(PendingAlarmScheduler.EXTRA_ALARM_ID, alarm.id());
                pi = PendingIntent.getBroadcast(mAppContext, alarm.intId(),
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
        int minutesToSnooze = AlarmUtils.snoozeDuration(mAppContext);
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
                .putExtra(UpcomingAlarmReceiver.EXTRA_ALARM, a);
        mAppContext.sendBroadcast(intent);
    }

    public void save(final Alarm alarm) {
        // TODO: Will using the Runnable like this cause a memory leak?
        new Thread(new Runnable() {
            @Override
            public void run() {
                mTableManager.updateItem(alarm.id(), alarm);
            }
        }).start();
    }

    private PendingIntent alarmIntent(Alarm alarm, boolean retrievePrevious) {
        // TODO: Use appropriate subclass instead
        Intent intent = new Intent(mAppContext, AlarmActivity.class)
                .putExtra(AlarmActivity.EXTRA_RINGING_OBJECT, alarm);
        int flag = retrievePrevious ? FLAG_NO_CREATE : FLAG_CANCEL_CURRENT;
        PendingIntent pi = getActivity(mAppContext, alarm.intId(), intent, flag);
        // Even when we try to retrieve a previous instance that actually did exist,
        // null can be returned for some reason.
/*
        if (retrievePrevious) {
            checkNotNull(pi);
        }
*/
        return pi;
    }

    private PendingIntent notifyUpcomingAlarmIntent(Alarm alarm, boolean retrievePrevious) {
        Intent intent = new Intent(mAppContext, UpcomingAlarmReceiver.class)
                .putExtra(UpcomingAlarmReceiver.EXTRA_ALARM, alarm);
        if (alarm.isSnoozed()) {
            // TODO: Will this affect retrieving a previous instance? Say if the previous instance
            // didn't have this action set initially, but at a later time we made a new instance
            // with it set.
            intent.setAction(UpcomingAlarmReceiver.ACTION_SHOW_SNOOZING);
        }
        int flag = retrievePrevious ? FLAG_NO_CREATE : FLAG_CANCEL_CURRENT;
        PendingIntent pi = PendingIntent.getBroadcast(mAppContext, alarm.intId(), intent, flag);
        // Even when we try to retrieve a previous instance that actually did exist,
        // null can be returned for some reason.
/*
        if (retrievePrevious) {
            checkNotNull(pi);
        }
*/
        return pi;
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
