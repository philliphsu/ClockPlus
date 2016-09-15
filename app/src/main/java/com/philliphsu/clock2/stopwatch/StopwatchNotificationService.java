package com.philliphsu.clock2.stopwatch;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.philliphsu.clock2.ChronometerNotificationService;
import com.philliphsu.clock2.MainActivity;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.timers.ChronometerDelegate;

public class StopwatchNotificationService extends ChronometerNotificationService {
    private static final String TAG = "StopwatchNotifService";

    public static final String ACTION_ADD_LAP = "com.philliphsu.clock2.stopwatch.action.ADD_LAP";
    public static final String ACTION_UPDATE_LAP_TITLE = "com.philliphsu.clock2.stopwatch.action.UPDATE_LAP_TITLE";

    public static final String EXTRA_LAP_NUMBER = "com.philliphsu.clock2.stopwatch.extra.LAP_NUMBER";

    private AsyncLapsTableUpdateHandler mUpdateHandler;
    private SharedPreferences mPrefs;
    private final ChronometerDelegate mDelegate = new ChronometerDelegate();
    private Lap mCurrentLap;

    @Override
    public void onCreate() {
        super.onCreate();
        mUpdateHandler = new AsyncLapsTableUpdateHandler(this, null);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        // TODO: I'm afraid the base time here will be off by a considerable amount from the base time
        // set in StopwatchFragment.
        mDelegate.init();
        mDelegate.setShowCentiseconds(true, false);
        // TODO: I think we can make this a foreground service so even
        // if the process is killed, this service remains alive.
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The base implementation returns START_STICKY, so this null intent
        // signifies that the service is being recreated after its process
        // had ended previously.
        if (intent == null) {
            // Start the ticking again, leaving everything else in the notification
            // as it was.
            Log.d(TAG, "Recreated service, starting chronometer again.");
            startChronometer();
        }
        // If this service is being recreated and the above if-block called through,
        // then the call to super won't run any commands, because it will see
        // that the intent is null.
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // After being cancelled due to time being up, sometimes the active timer notification posts again
        // with a static 00:00 text, along with the Time's up notification. My theory is
        // our thread has enough leeway to sneak in a final call to post the notification before it
        // is actually quit().
        // As such, try cancelling the notification with this (tag, id) pair again.
        cancelNotification(0);
    }

    @Override
    protected int getSmallIcon() {
        return R.drawable.ic_stopwatch_24dp;
    }

    @Nullable
    @Override
    protected PendingIntent getContentIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(null/*TODO:MainActivity.EXTRA_SHOW_PAGE*/, 2/*TODO:MainActivity.INDEX_STOPWATCH*/);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }

    @Override
    protected boolean isCountDown() {
        return false;
    }

    @Override
    protected void handleDefaultAction(Intent intent, int flags, long startId) {
        // TODO: Why do we need this check? Won't KEY_START_TIME always have a value of 0 here?
        if (mPrefs.getLong(StopwatchFragment.KEY_START_TIME, 0) == 0) {
            mCurrentLap = new Lap();
            mUpdateHandler.asyncInsert(mCurrentLap);
        }
        // TODO: String resource [Stopwatch: Lap %1$s]. If no laps, just [Stopwatch]
        setContentTitle(getString(R.string.stopwatch));
        syncNotificationWithStopwatchState(true/*always true*/);
        // We don't need to write anything to SharedPrefs because if we're here, StopwatchFragment
        // will start this service again with ACTION_START_PAUSE, which will do the writing.
    }

    @Override
    protected void handleStartPauseAction(Intent intent, int flags, long startId) {
        boolean running = mPrefs.getBoolean(StopwatchFragment.KEY_CHRONOMETER_RUNNING, false);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(StopwatchFragment.KEY_CHRONOMETER_RUNNING, !running);
        if (running) {
            editor.putLong(StopwatchFragment.KEY_PAUSE_TIME, SystemClock.elapsedRealtime());
            mCurrentLap.pause();
            mUpdateHandler.asyncUpdate(mCurrentLap.getId(), mCurrentLap);
        } else {
            long startTime = mPrefs.getLong(StopwatchFragment.KEY_START_TIME, 0);
            long pauseTime = mPrefs.getLong(StopwatchFragment.KEY_PAUSE_TIME, 0);
            startTime += SystemClock.elapsedRealtime() - pauseTime;
            editor.putLong(StopwatchFragment.KEY_START_TIME, startTime);
            editor.putLong(StopwatchFragment.KEY_PAUSE_TIME, 0);
            // TODO: Why do we need this check? Won't this lap always be paused here?
            if (!mCurrentLap.isRunning()) {
                mCurrentLap.resume();
                mUpdateHandler.asyncUpdate(mCurrentLap.getId(), mCurrentLap);
            }
        }
        editor.apply();
        syncNotificationWithStopwatchState(!running);
    }

    @Override
    protected void handleStopAction(Intent intent, int flags, long startId) {
        mPrefs.edit()
                .putLong(StopwatchFragment.KEY_START_TIME, 0)
                .putLong(StopwatchFragment.KEY_PAUSE_TIME, 0)
                .putBoolean(StopwatchFragment.KEY_CHRONOMETER_RUNNING, false)
                .apply();
        // If after this we restart the application, and then start the stopwatch in StopwatchFragment,
        // we will see that first and second laps appear in the list immediately. This is because
        // the laps from before we made this stop action are still in our SQLite database, because
        // they weren't cleared.
        //
        // We can either clear the laps table here, as we've done already, or do as the TODO above
        // says and tell StopwatchFragment to stop itself. The latter would also stop the
        // chronometer view if the fragment is still in view (i.e. app is still open).
        mCurrentLap = null;
        mUpdateHandler.asyncClear();
        stopSelf();
    }

    @Override
    protected void handleAction(@NonNull String action, Intent intent, int flags, long startId) {
        if (ACTION_ADD_LAP.equals(action)) {
            if (mPrefs.getBoolean(StopwatchFragment.KEY_CHRONOMETER_RUNNING, false)) {
                String timestamp = mDelegate.formatElapsedTime(SystemClock.elapsedRealtime(),
                        null/*Resources not needed here*/).toString();
                mCurrentLap.end(timestamp);
                mUpdateHandler.asyncUpdate(mCurrentLap.getId(), mCurrentLap);

                Lap newLap = new Lap();
                mUpdateHandler.asyncInsert(newLap);
                mCurrentLap = newLap;
            }
        } else if (ACTION_UPDATE_LAP_TITLE.equals(action)) {
            int lapNumber = intent.getIntExtra(EXTRA_LAP_NUMBER, 0);
            if (lapNumber == 0) {
                Log.w(TAG, "Lap number was not passed in with intent");
            }
            // Unfortunately, the ID is only assigned when retrieving a Lap instance from
            // its cursor; the value here will always be 0.
            setContentTitle(getString(R.string.stopwatch_and_lap_number, lapNumber));
            updateNotification(true);
        } else {
            throw new IllegalArgumentException("StopwatchNotificationService cannot handle action " + action);
        }
    }

    private void syncNotificationWithStopwatchState(boolean running) {
        clearActions();
        // TODO: Change fillColor to white, to accommodate API < 21.
        // Apparently, notifications on 21+ are automatically
        // tinted to gray to contrast against the native notification background color.
        //
        // No request code needed, so use 0.
        addAction(ACTION_ADD_LAP, R.drawable.ic_add_lap_24dp, getString(R.string.lap), 0);
        addStartPauseAction(running, 0);
        addStopAction(0);

        quitCurrentThread();
        if (running) {
            startChronometer();
        }
    }

    /**
     * Reads the value of KEY_START_TIME and passes it to {@link #startNewThread(int, long)} for you.
     */
    private void startChronometer() {
        long startTime = mPrefs.getLong(StopwatchFragment.KEY_START_TIME, SystemClock.elapsedRealtime());
        startNewThread(0, startTime);
    }
}
