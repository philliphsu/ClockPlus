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

package com.philliphsu.clock2.stopwatch;

import android.os.SystemClock;

import com.philliphsu.clock2.data.ObjectWithId;

/**
 * Created by Phillip Hsu on 8/8/2016.
 */
public class Lap extends ObjectWithId {

    private long t1;
    private long t2;
    private long pauseTime;
    private String totalTimeText;

    public Lap() {
        // TOneverDO: nanos because chronometer expects times in the elapsedRealtime base
        t1 = SystemClock.elapsedRealtime();
    }

    public long t1() {
        return t1;
    }

    public long t2() {
        return t2;
    }

    public long pauseTime() {
        return pauseTime;
    }

    public String totalTimeText() {
        return totalTimeText;
    }

    public void pause() {
        if (isEnded())
            throw new IllegalStateException("Cannot pause a Lap that has already ended");
        if (!isRunning())
            throw new IllegalStateException("Cannot pause a Lap that is already paused");
        pauseTime = SystemClock.elapsedRealtime();
    }

    public void resume() {
        if (isEnded())
            throw new IllegalStateException("Cannot resume a Lap that has already ended");
        if (isRunning())
            throw new IllegalStateException("Cannot resume a Lap that is not paused");
        t1 += SystemClock.elapsedRealtime() - pauseTime;
        pauseTime = 0;
    }

    public void end(String totalTime) {
        if (isEnded())
            throw new IllegalStateException("Cannot end a Lap that has already ended");
        t2 = SystemClock.elapsedRealtime();
        totalTimeText = totalTime;
        pauseTime = 0;
    }

    public long elapsed() {
        if (isRunning())
            return SystemClock.elapsedRealtime() - t1;
        else if (isEnded())
            return t2 - t1;
        else return pauseTime - t1;
    }

    public boolean isRunning() {
        return !isEnded() && pauseTime == 0;
    }

    public boolean isEnded() {
        return t2 > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lap lap = (Lap) o;

        if (t1 != lap.t1) return false;
        if (t2 != lap.t2) return false;
        if (pauseTime != lap.pauseTime) return false;
        return totalTimeText != null ? totalTimeText.equals(lap.totalTimeText) : lap.totalTimeText == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (t1 ^ (t1 >>> 32));
        result = 31 * result + (int) (t2 ^ (t2 >>> 32));
        result = 31 * result + (int) (pauseTime ^ (pauseTime >>> 32));
        result = 31 * result + (totalTimeText != null ? totalTimeText.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Lap{" +
                "t1=" + t1 +
                ", t2=" + t2 +
                ", pauseTime=" + pauseTime +
                ", totalTimeText='" + totalTimeText + '\'' +
                '}';
    }

    // ================ TO ONLY BE CALLED BY LapsTableManager ================

    public void setT1(long t1) {
        this.t1 = t1;
    }

    public void setT2(long t2) {
        this.t2 = t2;
    }

    public void setPauseTime(long pauseTime) {
        this.pauseTime = pauseTime;
    }

    public void setTotalTimeText(String totalTimeText) {
        this.totalTimeText = totalTimeText;
    }
}
