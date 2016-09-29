/*
 * Copyright (C) 2016 Phillip Hsu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
