package com.philliphsu.clock2.alarms;

import android.app.Notification;
import android.app.NotificationManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.ViewGroup;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.ringtone.RingtoneActivity;
import com.philliphsu.clock2.ringtone.RingtoneService;
import com.philliphsu.clock2.util.AlarmController;
import com.philliphsu.clock2.util.DateFormatUtils;

public class AlarmActivity extends RingtoneActivity<Alarm> {
    private static final String TAG = "TimesUpActivity";

    private AlarmController mAlarmController;
    private NotificationManager mNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAlarmController = new AlarmController(this, null);
        // TODO: If the upcoming alarm notification isn't present, verify other notifications aren't affected.
        // This could be the case if we're starting a new instance of this activity after leaving the first launch.
        mAlarmController.removeUpcomingAlarmNotification(getRingingObject());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void finish() {
        super.finish();
        mNotificationManager.cancel(TAG, getRingingObject().getIntId());
    }

    @Override
    protected Class<? extends RingtoneService> getRingtoneServiceClass() {
        return AlarmRingtoneService.class;
    }

    @Override
    protected CharSequence getHeaderTitle() {
        return getRingingObject().label();
    }

    @Override
    protected void getHeaderContent(ViewGroup parent) {
        // TODO: Consider applying size span on the am/pm label
        getLayoutInflater().inflate(R.layout.content_header_alarm_activity, parent, true);
    }

    @Override
    protected int getAutoSilencedDrawable() {
        // TODO: correct icon
        return R.drawable.ic_half_day_1_black_24dp;
    }

    @Override
    protected int getAutoSilencedText() {
        return R.string.alarm_auto_silenced_text;
    }

    @Override
    protected int getLeftButtonText() {
        return R.string.snooze;
    }

    @Override
    protected int getRightButtonText() {
        return R.string.dismiss;
    }

    @Override
    protected int getLeftButtonDrawable() {
        // TODO: correct icon
        return R.drawable.ic_half_day_1_black_24dp;
    }

    @Override
    protected int getRightButtonDrawable() {
        // TODO: correct icon
        return R.drawable.ic_half_day_1_black_24dp;
    }

    @Override
    protected void onLeftButtonClick() {
        mAlarmController.snoozeAlarm(getRingingObject());
        // Can't call dismiss() because we don't want to also call cancelAlarm()! Why? For example,
        // we don't want the alarm, if it has no recurrence, to be turned off right now.
        stopAndFinish();
    }

    @Override
    protected void onRightButtonClick() {
        // TODO do we really need to cancel the intent and alarm?
        mAlarmController.cancelAlarm(getRingingObject(), false);
        stopAndFinish();
    }

    // TODO: Consider changing the return type to Notification, and move the actual
    // task of notifying to the base class.
    @Override
    protected void showAutoSilenced() {
        super.showAutoSilenced();
        // Post notification that alarm was missed
        String alarmTime = DateFormatUtils.formatTime(this,
                getRingingObject().hour(), getRingingObject().minutes());
        Notification note = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.missed_alarm))
                .setContentText(alarmTime)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        mNotificationManager.notify(TAG, getRingingObject().getIntId(), note);
    }
}
