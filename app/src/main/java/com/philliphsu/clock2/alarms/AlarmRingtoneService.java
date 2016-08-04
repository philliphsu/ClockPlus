package com.philliphsu.clock2.alarms;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.ringtone.RingtoneService;
import com.philliphsu.clock2.util.AlarmController;
import com.philliphsu.clock2.util.AlarmUtils;

import static com.philliphsu.clock2.util.DateFormatUtils.formatTime;

public class AlarmRingtoneService extends RingtoneService<Alarm> {
    private static final String TAG = "AlarmRingtoneService";
    /* TOneverDO: not private */
    private static final String ACTION_SNOOZE = "com.philliphsu.clock2.ringtone.action.SNOOZE";
    private static final String ACTION_DISMISS = "com.philliphsu.clock2.ringtone.action.DISMISS";

    private String mNormalRingTime;
    private AlarmController mAlarmController;
    private Alarm mAlarm;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TOneverDO: Call super before our custom logic
        if (intent.getAction() == null) {
//            final long id = intent.getLongExtra(EXTRA_ITEM_ID, -1);
//            if (id < 0)
//                throw new IllegalStateException("No item id set");
            // http://stackoverflow.com/q/8696146/5055032
            // Start our own thread to load the alarm instead of using a loader,
            // because Services do not have a built-in LoaderManager (because they have no need for one since
            // their lifecycle is not complex like in Activities/Fragments) and our
            // work is simple enough that getting loaders to work here is not
            // worth the effort.
//            // TODO: Will using the Runnable like this cause a memory leak?
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    // TODO: We don't actually need the exact same Alarm instance as the
//                    // one from our calling component, because we won't mutate any of its
//                    // fields. Since we only read values, we could just pass in the Alarm
//                    // to the intent as a Parcelable.
//                    AlarmCursor cursor = new AlarmsTableManager(AlarmRingtoneService.this).queryItem(id);
//                    mAlarm = checkNotNull(cursor.getItem());
//                }
//            }).start();
            if ((mAlarm = intent.getParcelableExtra(EXTRA_ITEM)) == null) {
                throw new IllegalStateException("Cannot start AlarmRingtoneService without an Alarm");
            }
        } else {
            if (ACTION_SNOOZE.equals(intent.getAction())) {
                mAlarmController.snoozeAlarm(mAlarm);
            } else if (ACTION_DISMISS.equals(intent.getAction())) {
                mAlarmController.cancelAlarm(mAlarm, false); // TODO do we really need to cancel the intent and alarm?
            } else {
                throw new UnsupportedOperationException();
            }
            // ==========================================================================
            stopSelf(startId);
            finishActivity();
        }
        return super.onStartCommand(intent, flags, startId);
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        mAlarmController = new AlarmController(this, null);
    }

    @Override
    protected void onAutoSilenced() {
        // TODO do we really need to cancel the alarm and intent?
        mAlarmController.cancelAlarm(mAlarm, false);
        // Post notification that alarm was missed
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification note = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.missed_alarm))
                .setContentText(mNormalRingTime)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        nm.notify(TAG, mAlarm.intId(), note);
    }

    @Override
    protected Ringtone getRingtone() {
        Uri ringtone = Uri.parse(mAlarm.ringtone());
        return RingtoneManager.getRingtone(this, ringtone);
    }

    @Override
    protected Notification getForegroundNotification() {
        String title = mAlarm.label().isEmpty()
                ? getString(R.string.alarm)
                : mAlarm.label();
        mNormalRingTime = formatTime(this, System.currentTimeMillis()); // now
        return new NotificationCompat.Builder(this)
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
    }

    @Override
    protected boolean doesVibrate() {
        return mAlarm.vibrates();
    }

    @Override
    protected int minutesToAutoSilence() {
        return AlarmUtils.minutesToSilenceAfter(this);
    }
}
