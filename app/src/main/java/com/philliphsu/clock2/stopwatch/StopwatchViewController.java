package com.philliphsu.clock2.stopwatch;

import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by Phillip Hsu on 9/11/2016.
 */
public final class StopwatchViewController extends BaseStopwatchController {
    private static final String TAG = "StopwatchViewController";

    private final ChronometerWithMillis mChronometer;

    public StopwatchViewController(@NonNull AsyncLapsTableUpdateHandler updateHandler,
                                   @NonNull SharedPreferences prefs,
                                   @NonNull ChronometerWithMillis chronometer) {
        super(updateHandler, prefs);
        mChronometer = chronometer;

        mChronometer.setShowCentiseconds(true, true);
        final Stopwatch sw = getStopwatch();
        if (sw.getStartTime() > 0) {
            long base = sw.getStartTime();
            if (sw.getPauseTime() > 0) {
                base += SystemClock.elapsedRealtime() - sw.getPauseTime();
                // We're not done pausing yet, so don't reset mPauseTime.
            }
            chronometer.setBase(base);
        }
        if (isStopwatchRunning()) {
            chronometer.start();
            // Note: mChronometer.isRunning() will return false at this point and
            // in other upcoming lifecycle methods because it is not yet visible
            // (i.e. mVisible == false).
        }
    }

    @Override
    public void run() {
        super.run();
        mChronometer.setBase(getStopwatch().getStartTime());
        mChronometer.start();
    }

    @Override
    public void pause() {
        // Keep this call and the call to Stopwatch.pause() close together to minimize the time delta.
        mChronometer.stop();
        super.pause();
    }

    @Override
    public void stop() {
        mChronometer.stop();
        mChronometer.setBase(SystemClock.elapsedRealtime());
        super.stop();
    }

    @Override
    public void addNewLap(String currentLapTotalText) {
        if (!mChronometer.isRunning()) {
            Log.d(TAG, "Cannot add new lap");
            return;
        }
        super.addNewLap(currentLapTotalText);
    }

    /**
     * @return the state of the stopwatch when we're in a resumed and visible state,
     * or when we're going through a rotation
     */
    @Override
    public boolean isStopwatchRunning() {
        return mChronometer.isRunning() || super.isStopwatchRunning();
    }
}
