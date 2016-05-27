package com.philliphsu.clock;

import com.google.auto.value.AutoValue;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Phillip Hsu on 5/26/2016.
 */
@AutoValue
public abstract class Alarm {
    private static final int MAX_MINUTES_CAN_SNOOZE = 30;
    // Define our own day constants because those in the
    // Calendar class are not zero-based.
    public static final int SUNDAY = 0;
    public static final int MONDAY = 1;
    public static final int TUESDAY = 2;
    public static final int WEDNESDAY = 3;
    public static final int THURSDAY = 4;
    public static final int FRIDAY = 5;
    public static final int SATURDAY = 6;
    public static final int NUM_DAYS = 7;

    // =========== MUTABLE ===========
    private long snoozingUntilMillis;
    private boolean enabled;
    // ===============================
    public abstract long id(); // TODO: Counter in the repository. Set this field as the repo creates instances.
    public abstract int hour();
    public abstract int minutes();
    @SuppressWarnings("mutable")
    // TODO: Consider using an immutable collection instead
    public abstract boolean[] recurringDays(); // array itself is immutable, but elements are not
    public abstract String label();
    public abstract String ringtone();
    public abstract boolean vibrates();
    /** Initializes a Builder to the same property values as this instance */
    public abstract Builder toBuilder();

    public static void main(String[] args) {
        Alarm a = Alarm.builder().build();
    }

    public static Builder builder() {
        // Unfortunately, default values must be provided for generated Builders.
        // Fields that were not set when build() is called will throw an exception.
        return new AutoValue_Alarm.Builder()
                .id(-1)
                .hour(0)
                .minutes(0)
                .recurringDays(new boolean[NUM_DAYS])
                .label("")
                .ringtone("")
                .vibrates(false);
    }

    public final void snooze(int minutes) {
        if (minutes <= 0 || minutes > MAX_MINUTES_CAN_SNOOZE)
            throw new IllegalArgumentException("Cannot snooze for "+minutes+" minutes");
        snoozingUntilMillis = System.currentTimeMillis() + minutes * 60000;
    }

    public final long snoozingUntil() {
        return snoozingUntilMillis;
    }

    public final boolean isSnoozed() {
        if (snoozingUntilMillis <= System.currentTimeMillis()) {
            snoozingUntilMillis = 0;
            return false;
        }
        return true;
    }

    public final void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public final boolean isEnabled() {
        return enabled;
    }

    public final void setRecurring(int day, boolean recurring) {
        checkDay(day);
        recurringDays()[day] =  recurring;
    }

    public final boolean isRecurring(int day) {
        checkDay(day);
        return recurringDays()[day];
    }

    public final boolean hasRecurrence() {
        for (boolean b : recurringDays())
            if (b) return true;
        return false;
    }

    public final long ringsAt() {
        // Always with respect to the current date and time
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, hour());
        calendar.set(Calendar.MINUTE, minutes());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            // The specified time has passed for today
            // TODO: This should be wrapped in an if (!hasRecurrence())?
            calendar.add(Calendar.HOUR_OF_DAY, 24);
            // TODO: Else, compute ring time for the next closest recurring day
        }

        /* // Not fully thought out or completed!
        // TODO: Compute ring time for the next closest recurring day
        for (int i = 0; i < NUM_DAYS; i++) {
            // The constants for the days defined in Calendar are
            // not zero-based, but we are, so we must add 1.
            int day = i + 1; // day for this index
            int calendarDay = mCalendar.get(Calendar.DAY_OF_WEEK);
            if (mRecurringDays[day]) {
                mCalendar.add(Calendar.DAY_OF_WEEK, day);
                break;
            }
        }
        */
        return calendar.getTimeInMillis();
    }

    public final long ringsIn() {
        return ringsAt() - System.currentTimeMillis();
    }

    /** @return true if this Alarm will ring in the next {@code hours} hours */
    public final boolean ringsWithinHours(int hours) {
        return ringsIn() <= hours * 3600000;
    }

    @AutoValue.Builder
    public abstract static class Builder {
        // Builder is mutable, so these are inherently setter methods.
        // By omitting the set- prefix, we reduce the number of changes required to define the Builder
        // class after copying and pasting the accessor fields here.
        public abstract Builder id(long id);
        public abstract Builder hour(int hour);
        public abstract Builder minutes(int minutes);
        /* // TODO: If using an immutable collection instead, can use its Builder instance
           // and provide an "accumulating" method
        abstract boolean[] recurringDays();
        public final Builder setRecurring(int day, boolean recurs) {
            recurringDays()[day] = recurs;
            return this;
        }
        */
        public abstract Builder recurringDays(boolean[] recurringDays);
        public abstract Builder label(String label);
        public abstract Builder ringtone(String ringtone);
        public abstract Builder vibrates(boolean vibrates);
        // To enforce preconditions, split the build method into two. autoBuild() is hidden from
        // callers and is generated. You implement the public build(), which calls the generated
        // autoBuild() and performs your desired validations.
        /*not public*/abstract Alarm autoBuild();

        public final Alarm build() {
            Alarm alarm = autoBuild();
            if (alarm.hour() < 0 || alarm.hour() > 23 || alarm.minutes() < 0 || alarm.minutes() > 59) {
                throw new IllegalStateException("Hour and minutes invalid");
            }
            return alarm;
        }
    }

    private void checkDay(int day) {
        if (day < SUNDAY || day > SATURDAY)
            throw new IllegalArgumentException("Invalid day " + day);
    }
}
