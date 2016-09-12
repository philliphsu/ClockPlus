package com.philliphsu.clock2.stopwatch;

import android.os.SystemClock;

/**
 * Created by Phillip Hsu on 9/11/2016.
 */
public final class Stopwatch {
    private static final String TAG = "Stopwatch";

    private long mStartTime;
    private long mPauseTime;

    public Stopwatch(long startTime, long pauseTime) {
        mStartTime = startTime;
        mPauseTime = pauseTime;
    }

    public synchronized void pause() {
        if (!isRunning())
            throw new IllegalStateException("This stopwatch cannot be paused because it is not running");
        if (mPauseTime > 0)
            throw new IllegalStateException("This stopwatch is already paused");
        mPauseTime = SystemClock.elapsedRealtime();
    }

    public synchronized void run() {
        if (isRunning())
            throw new IllegalStateException("This stopwatch is already running");
        mStartTime += SystemClock.elapsedRealtime() - mPauseTime;
        mPauseTime = 0;
    }

    public synchronized void stop() {
        mStartTime = 0;
        mPauseTime = 0;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public long getPauseTime() {
        return mPauseTime;
    }

    public boolean isRunning() {
        return hasStarted() && mPauseTime == 0;
    }

    public boolean hasStarted() {
        // Not required to be presently running to have been started
        return mStartTime > 0;
    }
}
