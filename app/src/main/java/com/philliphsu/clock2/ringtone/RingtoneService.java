package com.philliphsu.clock2.ringtone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.philliphsu.clock2.R;

import static com.philliphsu.clock2.util.Preconditions.checkNotNull;

/**
 * Runs in the foreground. While it can still be killed by the system, it stays alive significantly
 * longer than if it does not run in the foreground. The longevity should be sufficient for practical
 * use. In fact, if the app is used properly, longevity should be a non-issue; realistically, the lifetime
 * of the RingtoneService will be tied to that of its RingtoneActivity because users are not likely to
 * navigate away from the Activity without making an action. But if they do accidentally navigate away,
 * they have plenty of time to make the desired action via the notification.
 */
public class RingtoneService extends Service {
    private static final String TAG = "RingtoneService";

    private AudioManager mAudioManager;
    private Ringtone mRingtone;
    private boolean mAutoSilenced = false;
    private final Handler mSilenceHandler = new Handler();
    private final Runnable mSilenceRunnable = new Runnable() {
        @Override
        public void run() {
            mAutoSilenced = true;
            stopSelf();
        }
    };

    // TODO: Apply the setting for "Silence after" here by using an AlarmManager to
    // schedule an alarm in the future to stop this service, and also update the foreground
    // notification to say "alarm missed" in the case of Alarms or "timer expired" for Timers.
    // If Alarms and Timers will have distinct settings for this, then consider doing this
    // operation in the respective subclass of this service.

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mAudioManager == null && mRingtone == null) {
            Uri ringtone = checkNotNull(intent.getData());
            // TODO: The below call requires a notification, and there is no way to provide one suitable
            // for both Alarms and Timers. Consider making this class abstract, and have subclasses
            // implement an abstract method that calls startForeground(). You would then call that
            // method here instead.
            Notification note = new NotificationCompat.Builder(this)
                    // Required contents
                    .setSmallIcon(R.mipmap.ic_launcher) // TODO: alarm icon
                    .setContentTitle("Foreground RingtoneService")
                    .setContentText("Ringtone is playing in the foreground.")
                    .build();
            startForeground(R.id.ringtone_service_notification, note); // TOneverDO: Pass 0 as the first argument

            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            // Request audio focus first, so we don't play our ringtone on top of any
            // other apps that currently have playback.
            int result = mAudioManager.requestAudioFocus(
                    null, // Playback will likely be short, so don't worry about listening for focus changes
                    AudioManager.STREAM_ALARM,
                    // Request permanent focus, as ringing could last several minutes
                    AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mRingtone = RingtoneManager.getRingtone(this, ringtone);
                // Deprecated, but the alternative AudioAttributes requires API 21
                mRingtone.setStreamType(AudioManager.STREAM_ALARM);
                mRingtone.play();
                scheduleAutoSilence();
            }
        }
        // If killed while started, don't recreate
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        mRingtone.stop();
        mAudioManager.abandonAudioFocus(null); // no listener was set
        mSilenceHandler.removeCallbacks(mSilenceRunnable);
        if (mAutoSilenced) {
            // Post notification that alarm was missed, or timer expired.
            // TODO: You should probably do this in the appropriate subclass.
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Notification note = new NotificationCompat.Builder(this)
                    .setContentTitle("Missed alarm")
                    .setContentText("Regular alarm time here")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build();
            nm.notify("tag", 0, note);
        }
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Binding to this service is not supported
    }

    private void scheduleAutoSilence() {
        // TODO: Read prefs
        //SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        /*int minutes = Integer.parseInt(pref.getString(
                getString(R.string.key_silence_after),
                "15"));*/
        mSilenceHandler.postDelayed(mSilenceRunnable, 10000);
    }
}
