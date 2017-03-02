/*
 * Copyright 2017 Phillip Hsu
 *
 * This file is part of ClockPlus.
 *
 * ClockPlus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ClockPlus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ClockPlus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.philliphsu.clock2.ringtone;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.view.ViewGroup;

import com.philliphsu.clock2.ringtone.playback.RingtoneService;
import com.philliphsu.clock2.ringtone.playback.TimerRingtoneService;
import com.philliphsu.clock2.timers.ui.CountdownChronometer;
import com.philliphsu.clock2.timers.TimerController;
import com.philliphsu.clock2.timers.TimerNotificationService;
import com.philliphsu.clock2.timers.data.AsyncTimersTableUpdateHandler;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.timers.Timer;

public class TimesUpActivity extends RingtoneActivity<Timer> {
    private static final String TAG = "TimesUpActivity";

    private TimerController mController;
    private NotificationManager mNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TimerNotificationService.cancelNotification(this, getRingingObject().getId());
        mController = new TimerController(getRingingObject(),
                new AsyncTimersTableUpdateHandler(this, null));
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void finish() {
        super.finish();
        mNotificationManager.cancel(TAG, getRingingObject().getIntId());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        postExpiredTimerNote();
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
    protected int getAutoSilencedText() {
        return R.string.timer_auto_silenced_text;
    }

    @Override
    protected int getLeftButtonText() {
        return R.string.add_one_minute;
    }

    @Override
    protected int getRightButtonText() {
        return R.string.stop;
    }

    @Override
    protected int getLeftButtonDrawable() {
        return R.drawable.ic_add_48dp;
    }

    @Override
    protected int getRightButtonDrawable() {
        return R.drawable.ic_stop_48dp;
    }

    @Override
    protected void onLeftButtonClick() {
        mController.addOneMinute();
        stopAndFinish();
    }

    @Override
    protected void onRightButtonClick() {
        mController.stop();
        stopAndFinish();
    }

    // TODO: Consider changing the return type to Notification, and move the actual
    // task of notifying to the base class.
    @Override
    protected void showAutoSilenced() {
        super.showAutoSilenced();
        postExpiredTimerNote();
    }

    @Override
    protected Parcelable.Creator<Timer> getParcelableCreator() {
        return Timer.CREATOR;
    }

    private void postExpiredTimerNote() {
        Notification note = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.timer_expired))
                .setContentText(getRingingObject().label())
                .setSmallIcon(R.drawable.ic_timer_24dp)
                .build();
        mNotificationManager.notify(TAG, getRingingObject().getIntId(), note);
    }
}
