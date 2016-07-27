package com.philliphsu.clock2.timers;

import android.os.SystemClock;
import android.view.View;
import android.widget.ImageButton;

import com.philliphsu.clock2.Timer;

/**
 * Created by Phillip Hsu on 7/27/2016.
 */
public class TimerController {
    private final Timer mTimer;
    private final CountdownChronometer mChronometer;
    private final ImageButton mAddOneMinute;
    private final ImageButton mStartPause;
    private final ImageButton mStop;

    public TimerController(Timer timer, CountdownChronometer chronometer, ImageButton addOneMinute,
                           ImageButton startPause, ImageButton stop) {
        mTimer = timer;
        mChronometer = chronometer;
        mAddOneMinute = addOneMinute;
        mStartPause = startPause;
        mStop = stop;
        init();
    }
    
    private void init() {
        mChronometer.setBase(SystemClock.elapsedRealtime() + mTimer.duration());
        updateStartPauseIcon();
        setSecondaryButtonsVisible(false);
    }

    public void start() {
        mTimer.start();
        mChronometer.setBase(mTimer.endTime());
        mChronometer.start();
        updateStartPauseIcon();
        setSecondaryButtonsVisible(true);
    }

    public void pause() {
        mTimer.pause();
        mChronometer.stop();
        updateStartPauseIcon();
    }

    public void resume() {
        mTimer.resume();
        mChronometer.setBase(mTimer.endTime());
        mChronometer.start();
        updateStartPauseIcon();
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

    public void updateStartPauseIcon() {
        // TODO: Pause and start icons, resp.
//        mStartPause.setImageResource(mTimer.isRunning() ? 0 : 0);
    }

    public void setSecondaryButtonsVisible(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.INVISIBLE;
        mAddOneMinute.setVisibility(visibility);
        mStop.setVisibility(visibility);
    }
}
