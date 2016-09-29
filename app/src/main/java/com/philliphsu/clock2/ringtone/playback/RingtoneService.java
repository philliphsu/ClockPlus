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

package com.philliphsu.clock2.ringtone.playback;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.util.Log;

import com.philliphsu.clock2.R;
import com.philliphsu.clock2.ringtone.RingtoneActivity;
import com.philliphsu.clock2.util.LocalBroadcastHelper;

import java.util.concurrent.TimeUnit;

/**
 * Runs in the foreground. While it can still be killed by the system, it stays alive significantly
 * longer than if it does not run in the foreground. The longevity should be sufficient for practical
 * use. In fact, if the app is used properly, longevity should be a non-issue; realistically, the lifetime
 * of the RingtoneService will be tied to that of its RingtoneActivity because users are not likely to
 * navigate away from the Activity without making an action. But if they do accidentally navigate away,
 * they have plenty of time to make the desired action via the notification.
 *
 * TOneverDO: Change this to not be a started service!
 */
public abstract class RingtoneService<T extends Parcelable> extends Service {
    private static final String TAG = "RingtoneService";

    // public okay
    public static final String ACTION_NOTIFY_MISSED = "com.philliphsu.clock2.ringtone.action.NOTIFY_MISSED";
//    public static final String EXTRA_ITEM_ID = RingtoneActivity.EXTRA_ITEM_ID;
    public static final String EXTRA_RINGING_OBJECT = RingtoneActivity.EXTRA_RINGING_OBJECT;

    private AudioManager mAudioManager;
    private RingtoneLoop mRingtone;
    private Vibrator mVibrator;
    private T mRingingObject;

    // TODO: Using Handler for this is ill-suited? Alarm ringing could outlast the
    // application's life. Use AlarmManager API instead.
    private final Handler mSilenceHandler = new Handler();

    private final Runnable mSilenceRunnable = new Runnable() {
        @Override
        public void run() {
            onAutoSilenced();
            LocalBroadcastHelper.sendBroadcast(RingtoneService.this, RingtoneActivity.ACTION_SHOW_SILENCED);
            stopSelf();
        }
    };

//    // Pretty sure this won't ever get called anymore... b/c EditAlarmActivity, the only component
//    // that sends such a broadcast, is deprecated.
    private final BroadcastReceiver mNotifyMissedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onAutoSilenced();
            stopSelf();
            // Activity finishes itself
        }
    };

    /**
     * Callback invoked when this Service is stopping and the corresponding
     * {@link RingtoneActivity} is finishing.
     */
    protected abstract void onAutoSilenced();

    protected abstract Uri getRingtoneUri();

    /**
     * @return the notification to show when this Service starts in the foreground
     */
    protected abstract Notification getForegroundNotification();

    protected abstract boolean doesVibrate();

    /**
     * @return the number of minutes to keep ringing before auto silence
     */
    protected abstract int minutesToAutoSilence();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mRingingObject == null) {
            if ((mRingingObject = intent.getParcelableExtra(EXTRA_RINGING_OBJECT)) == null) {
                throw new IllegalStateException("Cannot start RingtoneService without a ringing object");
            }
        }
        // Play ringtone, if not already playing
        if (mAudioManager == null && mRingtone == null) {
            // TOneverDO: Pass 0 as the first argument
            startForeground(R.id.ringtone_service_notification, getForegroundNotification());

            mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            // Request audio focus first, so we don't play our ringtone on top of any
            // other apps that currently have playback.
            int result = mAudioManager.requestAudioFocus(
                    null, // Playback will likely be short, so don't worry about listening for focus changes
                    AudioManager.STREAM_ALARM,
                    // Request permanent focus, as ringing could last several minutes
                    AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mRingtone = new RingtoneLoop(this, getRingtoneUri());
                mRingtone.play();
                if (doesVibrate()) {
                    mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    mVibrator.vibrate(new long[] { // apply pattern
                            0, // millis to wait before turning vibrator on
                            500, // millis to keep vibrator on before turning off
                            500, // millis to wait before turning back on
                            500 // millis to keep on before turning off
                    }, 2 /* start repeating at this index of the array, after one cycle */);
                }
                // Schedule auto silence
                mSilenceHandler.postDelayed(mSilenceRunnable,
                        TimeUnit.MINUTES.toMillis(minutesToAutoSilence()));
            }
        }
        // If killed while started, don't recreate. Should be sufficient.
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Pretty sure this won't ever get called anymore... b/c EditAlarmActivity, the only component
        // that sends such a broadcast, is deprecated.
        LocalBroadcastHelper.registerReceiver(this, mNotifyMissedReceiver, ACTION_NOTIFY_MISSED);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        mRingtone.stop();
        mAudioManager.abandonAudioFocus(null); // no listener was set
        if (mVibrator != null) {
            mVibrator.cancel();
        }
        mSilenceHandler.removeCallbacks(mSilenceRunnable);
        stopForeground(true);
        // Pretty sure this won't ever get called anymore... b/c EditAlarmActivity, the only component
        // that sends such a broadcast, is deprecated.
        LocalBroadcastHelper.unregisterReceiver(this, mNotifyMissedReceiver);
    }

    @Override
    public final IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Exposed to let subclasses finish their designated activity from, e.g. a
     * notification action.
     */
    protected void finishActivity() {
        // I think this will be received by all instances of RingtoneActivity
        // subclasses in memory.. but since we realistically expect only one
        // instance alive at any given time, we don't need to worry about having
        // to restrict the broadcast to only the subclass that's alive.
        // TODO: If we cared, we could write an abstract method called getFinishAction()
        // that subclasses implement, and call that here instead. The subclass of
        // RingtoneActivity would define their own ACTION_FINISH constants, and
        // the RingtoneService subclass retrieves that constant and returns it to us.
        LocalBroadcastHelper.sendBroadcast(this, RingtoneActivity.ACTION_FINISH);
    }

    /**
     * Exposed so subclasses can create their notification actions.
     */
    protected final PendingIntent getPendingIntent(@NonNull String action, int requestCode) {
        Intent intent = new Intent(this, getClass())
                .setAction(action);
        return PendingIntent.getService(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_ONE_SHOT);
    }

    protected final T getRingingObject() {
        return mRingingObject;
    }
}
