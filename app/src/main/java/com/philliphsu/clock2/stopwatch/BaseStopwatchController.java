package com.philliphsu.clock2.stopwatch;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * Created by Phillip Hsu on 9/11/2016.
 */
public abstract class BaseStopwatchController { // TODO: Extend this for use in StopwatchFragment and StopwatchNotificationSErvice
    // TODO: EIther expose these to subclasses or write an API for them to call
    // to write to prefs.
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_PAUSE_TIME = "pause_time";
    private static final String KEY_RUNNING = "running";

    private final AsyncLapsTableUpdateHandler mUpdateHandler;
    private final SharedPreferences mPrefs;

    private Stopwatch mStopwatch;
    private Lap mCurrentLap;
    private Lap mPreviousLap;

    public BaseStopwatchController(@NonNull AsyncLapsTableUpdateHandler updateHandler,
                                   @NonNull SharedPreferences prefs) {
        mUpdateHandler = updateHandler;
        mPrefs = prefs;
        long startTime = mPrefs.getLong(KEY_START_TIME, 0);
        long pauseTime = mPrefs.getLong(KEY_PAUSE_TIME, 0);
        mStopwatch = new Stopwatch(startTime, pauseTime);
    }

    public void run() {
        if (!mStopwatch.hasStarted()) {
            // addNewLap() won't call through unless chronometer is running, which
            // we can't start until we compute mStartTime
            mCurrentLap = new Lap();
            mUpdateHandler.asyncInsert(mCurrentLap);
        }
        mStopwatch.run();
        if (!mCurrentLap.isRunning()) {
            mCurrentLap.resume();
            mUpdateHandler.asyncUpdate(mCurrentLap.getId(), mCurrentLap);
        }
        savePrefs();
    }

    public void pause() {
        mStopwatch.pause();
        mCurrentLap.pause();
        mUpdateHandler.asyncUpdate(mCurrentLap.getId(), mCurrentLap);
        savePrefs();
    }

    public void stop() {
        mStopwatch.stop();
        mCurrentLap = null;
        mPreviousLap = null;
        mUpdateHandler.asyncClear(); // Clear laps
        savePrefs();
    }

    public void addNewLap(String currentLapTotalText) {
        if (mCurrentLap != null) {
            mCurrentLap.end(currentLapTotalText);
        }
        mPreviousLap = mCurrentLap;
        mCurrentLap = new Lap();
        if (mPreviousLap != null) {
//            if (getAdapter().getItemCount() == 0) {
//                mUpdateHandler.asyncInsert(mPreviousLap);
//            } else {
            mUpdateHandler.asyncUpdate(mPreviousLap.getId(), mPreviousLap);
//            }
        }
        mUpdateHandler.asyncInsert(mCurrentLap);
    }

    public final Stopwatch getStopwatch() {
        return mStopwatch;
    }

    public boolean isStopwatchRunning() {
        return mPrefs.getBoolean(KEY_RUNNING, false);
    }

    private void savePrefs() {
        mPrefs.edit().putLong(KEY_START_TIME, mStopwatch.getStartTime())
                .putLong(KEY_PAUSE_TIME, mStopwatch.getPauseTime())
                .putBoolean(KEY_RUNNING, mStopwatch.isRunning())
                .apply();
    }
}
