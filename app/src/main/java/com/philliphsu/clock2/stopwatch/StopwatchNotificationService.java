package com.philliphsu.clock2.stopwatch;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.philliphsu.clock2.ChronometerNotificationService;
import com.philliphsu.clock2.MainActivity;
import com.philliphsu.clock2.R;

public class StopwatchNotificationService extends ChronometerNotificationService {
    private static final String ACTION_ADD_LAP = "com.philliphsu.clock2.stopwatch.action.ADD_LAP";

    private AsyncLapsTableUpdateHandler mLapsTableUpdateHandler;
    private SharedPreferences mPrefs;

    @Override
    public void onCreate() {
        super.onCreate();
        mLapsTableUpdateHandler = new AsyncLapsTableUpdateHandler(this, null);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        // TODO: I think we can make this a foreground service so even
        // if the process is killed, this service remains alive.
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
        // TODO: String resource [Stopwatch: Lap %1$s]. If no laps, just [Stopwatch]
        setContentTitle(getString(R.string.stopwatch));
        syncNotificationWithStopwatchState(true/*always true*/);
        // We don't need to write anything to SharedPrefs because if we're here, StopwatchFragment
        // already wrote the necessary values to file.
    }

    @Override
    protected void handleStartPauseAction(Intent intent, int flags, long startId) {
        // TODO: Tell StopwatchFragment to start/pause itself.. perhaps with an Intent?
        boolean running = mPrefs.getBoolean(StopwatchFragment.KEY_CHRONOMETER_RUNNING, false);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(StopwatchFragment.KEY_CHRONOMETER_RUNNING, !running);
        if (running) {
            editor.putLong(StopwatchFragment.KEY_PAUSE_TIME, SystemClock.elapsedRealtime());
        } else {
            long startTime = mPrefs.getLong(StopwatchFragment.KEY_START_TIME, 0);
            long pauseTime = mPrefs.getLong(StopwatchFragment.KEY_PAUSE_TIME, 0);
            startTime += SystemClock.elapsedRealtime() - pauseTime;
            editor.putLong(StopwatchFragment.KEY_START_TIME, startTime);
            editor.putLong(StopwatchFragment.KEY_PAUSE_TIME, 0);
        }
        editor.apply();
        syncNotificationWithStopwatchState(!running);
    }

    @Override
    protected void handleStopAction(Intent intent, int flags, long startId) {
        // TODO: Tell StopwatchFragment to stop itself.. perhaps with an Intent?
        stopSelf();
    }

    @Override
    protected void handleAction(@NonNull String action, Intent intent, int flags, long startId) {
        if (ACTION_ADD_LAP.equals(action)) {
            mLapsTableUpdateHandler.asyncInsert(null/*TODO*/);
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
            long startTime = mPrefs.getLong(StopwatchFragment.KEY_START_TIME, SystemClock.elapsedRealtime());
            startNewThread(0, startTime);
        }
    }
}
