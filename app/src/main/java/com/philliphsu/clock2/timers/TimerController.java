package com.philliphsu.clock2.timers;

import com.philliphsu.clock2.AsyncTimersTableUpdateHandler;
import com.philliphsu.clock2.Timer;

/**
 * Created by Phillip Hsu on 7/27/2016.
 */
public class TimerController {
    private final Timer mTimer;
    private final AsyncTimersTableUpdateHandler mUpdateHandler;

    public TimerController(Timer timer, AsyncTimersTableUpdateHandler updateHandler) {
        mTimer = timer;
        mUpdateHandler = updateHandler;
    }

    /**
     * Start/resume or pause the timer.
     */
    public void startPause() {
        if (mTimer.hasStarted()) {
            if (mTimer.isRunning()) {
                mTimer.pause();
            } else {
                mTimer.resume();
            }
        } else {
            mTimer.start();
        }
        update();
    }

    public void stop() {
        mTimer.stop();
        update();
    }

    public void addOneMinute() {
        mTimer.addOneMinute();
        update();
    }

    private void update() {
        mUpdateHandler.asyncUpdate(mTimer.getId(), mTimer);
    }
}
