package com.philliphsu.clock2.timers;

import com.philliphsu.clock2.Timer;

/**
 * Created by Phillip Hsu on 7/27/2016.
 */
public class TimerController {
    private final Timer mTimer;

    /**
     * Calls the appropriate state on the given Timer, based on
     * its current state.
     */
    public static void startPause(Timer timer) {
        if (timer.hasStarted()) {
            if (timer.isRunning()) {
                timer.pause();
            } else {
                timer.resume();
            }
        } else {
            timer.start();
        }
    }

    public TimerController(Timer timer) {
        mTimer = timer;
    }

    public void start() {
        mTimer.start();
//        mChronometer.setBase(mTimer.endTime());
//        mChronometer.start();
//        updateStartPauseIcon();
//        setSecondaryButtonsVisible(true);

    }

    public void pause() {
        mTimer.pause();
//        mChronometer.stop();
//        updateStartPauseIcon();
    }

    public void resume() {
        mTimer.resume();
//        mChronometer.setBase(mTimer.endTime());
//        mChronometer.start();
//        updateStartPauseIcon();
    }

    public void stop() {
        mTimer.stop();
//        mChronometer.stop();
//        init();
    }

    public void addOneMinute() {
        mTimer.addOneMinute();
//        mChronometer.setBase(mTimer.endTime());
    }

//    public void updateStartPauseIcon() {
//        // TODO: Pause and start icons, resp.
//        mStartPause.setImageResource(mTimer.isRunning() ? 0 : 0);
//    }

//    public void setSecondaryButtonsVisible(boolean visible) {
//        int visibility = visible ? View.VISIBLE : View.INVISIBLE;
//        mAddOneMinute.setVisibility(visibility);
//        mStop.setVisibility(visibility);
//    }
}
