package com.philliphsu.clock2.timers;

import android.content.Intent;
import android.os.Bundle;

import com.philliphsu.clock2.R;
import com.philliphsu.clock2.Timer;
import com.philliphsu.clock2.ringtone.RingtoneActivity;
import com.philliphsu.clock2.ringtone.RingtoneService;

public class TimesUpActivity extends RingtoneActivity<Timer> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stopService(new Intent(this, TimerNotificationService.class));
        TimerNotificationService.cancelNotification(this, getRingingObject().getId());
    }

    @Override
    public int layoutResource() {
        return R.layout.activity_ringtone;
    }

    @Override
    protected Class<? extends RingtoneService> getRingtoneServiceClass() {
        return TimerRingtoneService.class;
    }
}
