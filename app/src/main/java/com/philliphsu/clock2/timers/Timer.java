package com.philliphsu.clock2.timers;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;

import com.google.auto.value.AutoValue;
import com.philliphsu.clock2.data.ObjectWithId;

import java.util.concurrent.TimeUnit;

/**
 * Created by Phillip Hsu on 7/25/2016.
 */
@AutoValue
public abstract class Timer extends ObjectWithId implements Parcelable {
    private static final long MINUTE = TimeUnit.MINUTES.toMillis(1);

    private long endTime;
    private long pauseTime;
    private long duration;

    // Using this crashes the app when we create a Timer and start it...
    // timeRemaining() is returning a negative value... but it doesn't even
    // consider duration()....?
    // My guess is the hour, minute, and second getters are returning 0
    // at this point...?
//    private final long normalDuration = TimeUnit.HOURS.toMillis(hour())
//            + TimeUnit.MINUTES.toMillis(minute())
//            + TimeUnit.SECONDS.toMillis(second());

    public abstract int hour();
    public abstract int minute();
    public abstract int second();
    // 9/6/2016: Just found/fixed a very subtle bug involving mixing up the parameter orders
    // of group and label when `create()`ing a Timer in TimerCursor.
    // TODO: We have never used this at all, so consider deleting this!
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

    public void copyMutableFieldsTo(Timer target) {
        target.setId(this.getId());
        target.endTime = this.endTime;
        target.pauseTime = this.pauseTime;
        target.duration = this.duration;
    }

    public long endTime() {
        return endTime;
    }

    public boolean expired() {
        return /*!hasStarted() ||*/endTime <= SystemClock.elapsedRealtime();
    }

    public long timeRemaining() {
        if (!hasStarted())
            // TODO: Consider returning duration instead? So we can simplify
            // bindChronometer() in TimerVH to:
            // if (isRunning())
            //  ...
            // else
            //  chronom.setDuration(timeRemaining())
            // ---
            // Actually, I think we can also simplify it even further to just:
            // chronom.setDuration(timeRemaining())
            // if (isRunning)
            //  chronom.start();
            return 0;
        return isRunning()
                ? endTime - SystemClock.elapsedRealtime()
                : endTime - pauseTime;
    }

    public long duration() {
        if (duration == 0) {
            duration = TimeUnit.HOURS.toMillis(hour())
                    + TimeUnit.MINUTES.toMillis(minute())
                    + TimeUnit.SECONDS.toMillis(second());
        }
        return duration;
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
        duration = 0;
    }

    public void addOneMinute() {
        if (!isRunning()) {
            resume();
            addOneMinute(); // recursion!
            pause();
            return;
        } else if (expired()) {
            endTime = SystemClock.elapsedRealtime() + MINUTE;
            // If the timer's normal duration is >= MINUTE, then an extra run time of one minute
            // will still be within the normal duration. Thus, the progress calculation does not
            // need to change. For example, if the timer's normal duration is 2 minutes, an extra
            // 1 minute run time is fully encapsulated within the 2 minute upper bound.
            if (duration < MINUTE) {
                // This scales the progress bar to a full minute.
                duration = MINUTE;
            }
            return;
        }

        endTime += MINUTE;
        duration += MINUTE;
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

    /**
     * TO ONLY BE CALLED BY TIMERDATABASEHELPER.
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(hour());
        dest.writeInt(minute());
        dest.writeInt(second());
        dest.writeString(group());
        dest.writeString(label());
        dest.writeLong(getId());
        dest.writeLong(endTime);
        dest.writeLong(pauseTime);
        dest.writeLong(duration);
    }

    public static final Creator<Timer> CREATOR = new Creator<Timer>() {
        @Override
        public Timer createFromParcel(Parcel source) {
            return Timer.create(source);
        }

        @Override
        public Timer[] newArray(int size) {
            return new Timer[size];
        }
    };

    private static Timer create(Parcel source) {
        Timer t = Timer.create(source.readInt(), source.readInt(), source.readInt(),
                source.readString(), source.readString());
        t.setId(source.readLong());
        t.endTime = source.readLong();
        t.pauseTime = source.readLong();
        t.duration = source.readLong();
        return t;
    }
}
