package com.philliphsu.clock2.timers;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.ViewGroup;

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
    protected Class<? extends RingtoneService> getRingtoneServiceClass() {
        return TimerRingtoneService.class;
    }

    @Override
    protected CharSequence getHeaderTitle() {
        return getRingingObject().label();
    }

    @Override
    protected void getHeaderContent(ViewGroup parent) {
        // Inflate the content and apply the parent's layout params, but don't
        // attach it to the parent yet. This is so the return value can be
        // the root of the inflated content, and not the parent. Alternatively,
        // we could set an id on the root of the content's layout and find it
        // from the returned parent.
        CountdownChronometer countdown = (CountdownChronometer) getLayoutInflater()
                .inflate(R.layout.content_header_timesup_activity, parent, false);
        countdown.setBase(SystemClock.elapsedRealtime());
        countdown.start();
        parent.addView(countdown);
    }

    @Override
    protected int getAutoSilencedDrawable() {
        // TODO: correct icon
        return R.drawable.ic_half_day_1_black_24dp;
    }

    @Override
    protected int getAutoSilencedText() {
        return R.string.timer_auto_silenced_text;
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

    }

    @Override
    protected void onRightButtonClick() {

    }
}
