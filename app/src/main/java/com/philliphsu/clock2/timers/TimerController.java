package com.philliphsu.clock2.timers;

import android.util.Log;

import com.philliphsu.clock2.AsyncTimersTableUpdateHandler;
import com.philliphsu.clock2.Timer;

/**
 * Created by Phillip Hsu on 7/27/2016.
 */
public class TimerController {
    private static final String TAG = "TimerController";
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
    
    public void updateLabel(String label) {
        Log.d(TAG, "Updating Timer with label = " + label);
        Timer newTimer = Timer.create(
                mTimer.hour(),
                mTimer.minute(),
                mTimer.second(),
                mTimer.group(),
                label);
        mTimer.copyMutableFieldsTo(newTimer);
        mUpdateHandler.asyncUpdate(mTimer.getId(), newTimer);
        // Prompts a reload of the list data, so the list will reflect this modified timer
    }

    public void deleteTimer() {
        mUpdateHandler.asyncDelete(mTimer);
    }

    private void update() {
        mUpdateHandler.asyncUpdate(mTimer.getId(), mTimer);
    }
}
