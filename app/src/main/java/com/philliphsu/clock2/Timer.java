package com.philliphsu.clock2;

import android.os.SystemClock;

import com.google.auto.value.AutoValue;
import com.philliphsu.clock2.model.ObjectWithId;

import java.util.concurrent.TimeUnit;

/**
 * Created by Phillip Hsu on 7/25/2016.
 */
@AutoValue
public abstract class Timer extends ObjectWithId {
    private static final long MINUTE = TimeUnit.MINUTES.toMillis(1);

    private long endTime;
    private long pauseTime;

    public abstract int hour();
    public abstract int minute();
    public abstract int second();
    public abstract String group();
    public abstract String label();

    public static Timer create(int hour, int minute, int second) {
        return create(hour, minute, second, "", "");
    }

    public static Timer createWithGroup(int hour, int minute, int second, String group) {
        return create(hour, minute, second, group, "");
    }

    public static Timer createWithLabel(int hour, int minute, int second, String label) {
        return create(hour, minute, second, "", label);
    }

    public static Timer create(int hour, int minute, int second, String group, String label) {
        if (hour < 0 || minute < 0 || second < 0 || (hour == 0 && minute == 0 && second == 0))
            throw new IllegalArgumentException("Cannot create a timer with h = "
                    + hour + ", m = " + minute + ", s = " + second);
        return new AutoValue_Timer(hour, minute, second, group, label);
    }

    public long endTime() {
        return endTime;
    }

    public boolean expired() {
        return /*!hasStarted() ||*/endTime <= SystemClock.elapsedRealtime();
    }

    public long timeRemaining() {
        if (!hasStarted())
            return 0;
        return isRunning()
                ? endTime - SystemClock.elapsedRealtime()
                : endTime - pauseTime;
    }

    public long duration() {
        return TimeUnit.HOURS.toMillis(hour())
                + TimeUnit.MINUTES.toMillis(minute())
                + TimeUnit.SECONDS.toMillis(second());
    }

    public void start() {
        if (isRunning())
            throw new IllegalStateException("Cannot start a timer that has already started OR is already running");
        // TOneverDO: use nanos, AlarmManager expects times in millis
        endTime = SystemClock.elapsedRealtime() + duration();
    }

    public void pause() {
        if (!isRunning())
            throw new IllegalStateException("Cannot pause a timer that is not running OR has not started");
        pauseTime = SystemClock.elapsedRealtime();
    }

    public void resume() {
        if (!hasStarted() || isRunning())
            throw new IllegalStateException("Cannot resume a timer that is already running OR has not started");
        endTime += SystemClock.elapsedRealtime() - pauseTime;
        pauseTime = 0;
    }

    public void stop() {
        endTime = 0;
        pauseTime = 0;
    }

    public void addOneMinute() {
        // Allow extending even if paused.
//        if (!isRunning())
//            throw new IllegalStateException("Cannot extend a timer that is not running");
        if (expired()) {
            endTime = SystemClock.elapsedRealtime() + MINUTE;
        } else {
            endTime += MINUTE;
        }
    }

    public boolean hasStarted() {
        return endTime > 0;
    }

    public boolean isRunning() {
        return hasStarted() && pauseTime == 0;
    }

    /**
     * TO ONLY BE CALLED BY TIMERDATABASEHELPER.
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /**
     * TO ONLY BE CALLED BY TIMERDATABASEHELPER.
     */
    public void setPauseTime(long pauseTime) {
        this.pauseTime = pauseTime;
    }

    /**
     * TO ONLY BE CALLED BY TIMERDATABASEHELPER.
     */
    public long pauseTime() {
        return pauseTime;
    }
}
