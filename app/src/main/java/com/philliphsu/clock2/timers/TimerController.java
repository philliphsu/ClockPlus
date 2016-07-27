package com.philliphsu.clock2.timers;

import android.os.SystemClock;

import com.philliphsu.clock2.Timer;

/**
 * Created by Phillip Hsu on 7/27/2016.
 */
public class TimerController {
    private final Timer mTimer;
    private final CountdownChronometer mChronometer;

    public TimerController(Timer timer, CountdownChronometer chronometer) {
        mTimer = timer;
        mChronometer = chronometer;
        init();
    }
    
    private void init() {
        mChronometer.setBase(SystemClock.elapsedRealtime() + mTimer.duration());
    }

    public void start() {
        mTimer.start();
        mChronometer.setBase(mTimer.endTime());
        mChronometer.start();
    }

    public void pause() {
        mTimer.pause();
        mChronometer.stop();
    }

    public void resume() {
        mTimer.resume();
        mChronometer.setBase(mTimer.endTime());
        mChronometer.start();
    }

    public void stop() {
        mTimer.stop();
        mChronometer.stop();
        init();
    }

    public void addOneMinute() {
        mTimer.addOneMinute();
        mChronometer.setBase(mTimer.endTime());
    }
}
