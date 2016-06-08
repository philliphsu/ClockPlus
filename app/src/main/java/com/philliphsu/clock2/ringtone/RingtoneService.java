package com.philliphsu.clock2.ringtone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.model.AlarmsRepository;
import com.philliphsu.clock2.util.AlarmUtils;

import static com.philliphsu.clock2.util.DateFormatUtils.formatTime;
import static com.philliphsu.clock2.util.Preconditions.checkNotNull;

/**
 * Runs in the foreground. While it can still be killed by the system, it stays alive significantly
 * longer than if it does not run in the foreground. The longevity should be sufficient for practical
 * use. In fact, if the app is used properly, longevity should be a non-issue; realistically, the lifetime
 * of the RingtoneService will be tied to that of its RingtoneActivity because users are not likely to
 * navigate away from the Activity without making an action. But if they do accidentally navigate away,
 * they have plenty of time to make the desired action via the notification.
 */
public class RingtoneService extends Service { // TODO: abstract this, make subclasses
    private static final String TAG = "RingtoneService";

    /* TOneverDO: not private */
    private static final String ACTION_SNOOZE = "com.philliphsu.clock2.ringtone.action.SNOOZE";
    private static final String ACTION_DISMISS = "com.philliphsu.clock2.ringtone.action.DISMISS";
    // TODO: Same value as RingtoneActivity.EXTRA_ITEM_ID. Is it important enough to define a different constant?
    private static final String EXTRA_ITEM_ID = "com.philliphsu.clock2.ringtone.extra.ITEM_ID";

    private AudioManager mAudioManager;
    private Ringtone mRingtone;
    private Alarm mAlarm;
    private String mNormalRingTime;
    private boolean mAutoSilenced = false;
    private RingtoneCallback mRingtoneCallback;
    // TODO: Using Handler for this is ill-suited? Alarm ringing could outlast the
    // application's life. Use AlarmManager API instead.
    private final Handler mSilenceHandler = new Handler();
    private final Runnable mSilenceRunnable = new Runnable() {
        @Override
        public void run() {
            mAutoSilenced = true;
            mRingtone.stop(); // don't wait for activity to finish and unbind
            if (mRingtoneCallback != null) {
                // Finish the activity, which fires onDestroy() and then unbinds itself from this service.
                // All clients must be unbound before stopSelf() (and stopService()?) will succeed.
                // See https://developer.android.com/guide/components/bound-services.html#Lifecycle
                // Figure 1 regarding the lifecycle of started and bound services.
                mRingtoneCallback.onServiceFinish();
            }
        }
    };
    private final IBinder mBinder = new RingtoneBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Although this is a bound service, we override this method because this class is reused for
        // handling the notification actions for the presently ringing alarm.
        // Although the docs of Context#startService() says this:
        // "Using startService() overrides the default service lifetime that is managed by
        // bindService(Intent, ServiceConnection, int): it requires the service to remain running until
        // stopService(Intent) [or stopSelf()] is called, regardless of whether any clients are connected to it."
        // I have found the regardless part does not apply here. You MUST also unbind any clients from this service
        // at the same time you stop this service!
        String action = intent.getAction();
        if (!action.equals(ACTION_SNOOZE) && !action.equals(ACTION_DISMISS))
            throw new UnsupportedOperationException();

        if (ACTION_SNOOZE.equals(intent.getAction())) {
            long id = intent.getLongExtra(EXTRA_ITEM_ID, -1);
            if (id < 0)
                throw new IllegalStateException("No item id set");
            Alarm alarm = checkNotNull(AlarmsRepository.getInstance(this).getItem(id));
            AlarmUtils.snoozeAlarm(this, alarm);
        }
        // ============================== WARNING ===================================
        // DO NOT DO ANYTHING FOR ACTION_DISMISS. RingtoneActivity's current implementation of
        // onServiceFinish() calls cancelAlarm for you!
        // /*else if (ACTION_DISMISS.equals(intent.getAction())) {
        //    AlarmUtils.cancelAlarm(this, alarm);
        // }*/
        // ==========================================================================
        stopSelf(startId);
        if (mRingtoneCallback != null) {
            mRingtoneCallback.onServiceFinish(); // tell client to unbind from this service
        }

        return START_NOT_STICKY; // If killed while started, don't recreate. Should be sufficient.
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
                    .setContentTitle(getString(R.string.missed_alarm))
                    .setContentText(mNormalRingTime)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build();
            // A tag with the name of the subclass is used in addition to the item's id to prevent
            // conflicting notifications for items of different class types. Items of any class type
            // have ids starting from 0.
            nm.notify(getClass().getName(), mAlarm.intId(), note);
        }
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void playRingtone(@NonNull Alarm alarm) {
        if (mAudioManager == null && mRingtone == null) {
            mAlarm = checkNotNull(alarm);
            // TODO: The below call requires a notification, and there is no way to provide one suitable
            // for both Alarms and Timers. Consider making this class abstract, and have subclasses
            // implement an abstract method that calls startForeground(). You would then call that
            // method here instead.
            String title = mAlarm.label().isEmpty()
                    ? getString(R.string.alarm)
                    : mAlarm.label();
            mNormalRingTime = formatTime(this, System.currentTimeMillis()); // now
            Notification note = new NotificationCompat.Builder(this)
                    // Required contents
                    .setSmallIcon(R.mipmap.ic_launcher) // TODO: alarm icon
                    .setContentTitle(title)
                    .setContentText(mNormalRingTime)
                    .addAction(R.mipmap.ic_launcher,
                            getString(R.string.snooze),
                            getPendingIntent(ACTION_SNOOZE, mAlarm))
                    .addAction(R.mipmap.ic_launcher,
                            getString(R.string.dismiss),
                            getPendingIntent(ACTION_DISMISS, mAlarm))
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
                Uri ringtone = Uri.parse(mAlarm.ringtone());
                mRingtone = RingtoneManager.getRingtone(this, ringtone);
                // Deprecated, but the alternative AudioAttributes requires API 21
                mRingtone.setStreamType(AudioManager.STREAM_ALARM);
                mRingtone.play();
                scheduleAutoSilence();
            }
        }
    }

    public void setRingtoneCallback(RingtoneCallback callback) {
        mRingtoneCallback = callback;
    }

    /**
     * A way for clients to interrupt the currently running instance of this service. Interrupting
     * the service is akin to prematurely auto silencing the ringtone right now. <b>Clients MUST
     * unbind from this service immediately after interrupting.</b>
     */
    public void interrupt() {
        mAutoSilenced = true;
    }

    // Needed so clients can get the Service instance and e.g. call setRingtoneCallback().
    public class RingtoneBinder extends Binder {
        RingtoneService getService() {
            return RingtoneService.this;
        }
    }

    public interface RingtoneCallback {
        void onServiceFinish();
    }

    // TODO: For Timers, update the foreground notification to say "timer expired". Also,
    // if Alarms and Timers will have distinct settings for the minutes to silence after, then consider
    // doing this in the respective subclass of this service.
    private void scheduleAutoSilence() {
        int minutes = AlarmUtils.minutesToSilenceAfter(this);
        mSilenceHandler.postDelayed(mSilenceRunnable, minutes * 60000);
    }

    private PendingIntent getPendingIntent(@NonNull String action, Alarm alarm) {
        Intent intent = new Intent(this, getClass())
                .setAction(action)
                .putExtra(EXTRA_ITEM_ID, alarm.id());
        return PendingIntent.getService(
                this,
                alarm.intId(),
                intent,
                PendingIntent.FLAG_ONE_SHOT);
    }
}
