/*
 * Copyright 2017 Phillip Hsu
 *
 * This file is part of ClockPlus.
 *
 * ClockPlus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ClockPlus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ClockPlus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.philliphsu.clock2.timers;

import android.util.Log;

import com.philliphsu.clock2.timers.data.AsyncTimersTableUpdateHandler;

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
