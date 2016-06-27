package com.philliphsu.clock2;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;
import com.philliphsu.clock2.model.JsonSerializable;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.philliphsu.clock2.DaysOfWeek.NUM_DAYS;
import static com.philliphsu.clock2.DaysOfWeek.SATURDAY;
import static com.philliphsu.clock2.DaysOfWeek.SUNDAY;

/**
 * Created by Phillip Hsu on 5/26/2016.
 */
@AutoValue
public abstract class Alarm implements JsonSerializable {
    private static final int MAX_MINUTES_CAN_SNOOZE = 30;

    // =================== MUTABLE =======================
    private long id;
    private long snoozingUntilMillis;
    private boolean enabled;
    private final boolean[] recurringDays = new boolean[NUM_DAYS];
    // ====================================================

    public abstract int hour();
    public abstract int minutes();
    public abstract String label();
    public abstract String ringtone();
    public abstract boolean vibrates();
    /** Initializes a Builder to the same property values as this instance */
    public abstract Builder toBuilder();

    @Deprecated
    public static Alarm create(JSONObject jsonObject) {
        return null;
    }

    public static Builder builder() {
        // Unfortunately, default values must be provided for generated Builders.
        // Fields that were not set when build() is called will throw an exception.
        return new AutoValue_Alarm.Builder()
                .hour(0)
                .minutes(0)
                .label("")
                .ringtone("")
                .vibrates(false);
    }

    public void snooze(int minutes) {
        if (minutes <= 0 || minutes > MAX_MINUTES_CAN_SNOOZE)
            throw new IllegalArgumentException("Cannot snooze for "+minutes+" minutes");
        snoozingUntilMillis = System.currentTimeMillis() + minutes * 60000;
    }

    public long snoozingUntil() {
        return isSnoozed() ? snoozingUntilMillis : 0;
    }

    public boolean isSnoozed() {
        if (snoozingUntilMillis <= System.currentTimeMillis()) {
            snoozingUntilMillis = 0;
            return false;
        }
        return true;
    }

    /** <b>ONLY CALL THIS WHEN CREATING AN ALARM INSTANCE FROM A CURSOR</b> */
    // TODO: To be even more safe, create a ctor that takes the two Cursors and
    // initialize the instance here instead of in AlarmDatabaseHelper.
    public void setSnoozing(long snoozingUntilMillis) {
        this.snoozingUntilMillis = snoozingUntilMillis;
    }

    public void stopSnoozing() {
        snoozingUntilMillis = 0;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean[] recurringDays() {
        return recurringDays;
    }

    public void setRecurring(int day, boolean recurring) {
        checkDay(day);
        recurringDays[day] = recurring;
    }

    public boolean isRecurring(int day) {
        checkDay(day);
        return recurringDays[day];
    }

    public boolean hasRecurrence() {
        return numRecurringDays() > 0;
    }

    public int numRecurringDays() {
        int count = 0;
        for (boolean b : recurringDays)
            if (b) count++;
        return count;
    }

    public long ringsAt() {
        // Always with respect to the current date and time
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, hour());
        calendar.set(Calendar.MINUTE, minutes());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (!hasRecurrence()) {
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                // The specified time has passed for today
                calendar.add(Calendar.HOUR_OF_DAY, 24);
            }
        } else {
            // Compute the ring time just for the next closest recurring day.
            // Remember that day constants defined in the Calendar class are not zero-based like ours, so we have to
            // compensate with an offset of magnitude one, with the appropriate sign based on the situation.
            int weekdayToday = calendar.get(Calendar.DAY_OF_WEEK);
            int numDaysFromToday = -1;

            for (int i = weekdayToday; i <= Calendar.SATURDAY; i++) {
                if (isRecurring(i - 1 /*match up with our day constant*/)) {
                    if (i == weekdayToday) {
                        if (calendar.getTimeInMillis() > System.currentTimeMillis()) {
                            // The normal ring time has not passed yet
                            numDaysFromToday = 0;
                            break;
                        }
                    } else {
                        numDaysFromToday = i - weekdayToday;
                        break;
                    }
                }
            }

            // Not computed yet
            if (numDaysFromToday < 0) {
                for (int i = Calendar.SUNDAY; i < weekdayToday; i++) {
                    if (isRecurring(i - 1 /*match up with our day constant*/)) {
                        numDaysFromToday = Calendar.SATURDAY - weekdayToday + i;
                        break;
                    }
                }
            }

            // Still not computed yet. The only recurring day is weekdayToday,
            // and its normal ring time has already passed.
            if (numDaysFromToday < 0 && isRecurring(weekdayToday - 1)
                    && calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                numDaysFromToday = 7;
            }

            if (numDaysFromToday < 0)
                throw new IllegalStateException("How did we get here?");

            calendar.add(Calendar.HOUR_OF_DAY, 24 * numDaysFromToday);
        }

        return calendar.getTimeInMillis();
    }

    public long ringsIn() {
        return ringsAt() - System.currentTimeMillis();
    }

    /** @return true if this Alarm will ring in the next {@code hours} hours */
    public boolean ringsWithinHours(int hours) {
        return ringsIn() <= hours * 3600000;
    }

    @Deprecated
    public int intId() {
        return -1;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Deprecated
    @Override
    public final long id() {
        return -1;
    }

    @Deprecated
    @Override
    @NonNull
    public JSONObject toJsonObject() {
        return null;
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder hour(int hour);
        public abstract Builder minutes(int minutes);
        public abstract Builder label(String label);
        public abstract Builder ringtone(String ringtone);
        public abstract Builder vibrates(boolean vibrates);
        /* package */ abstract Alarm autoBuild();

        public Alarm build() {
            Alarm alarm = autoBuild();
            doChecks(alarm);
            return alarm;
        }
    }

    private static void doChecks(Alarm alarm) {
        checkTime(alarm.hour(), alarm.minutes());
    }

    private static void checkDay(int day) {
        if (day < SUNDAY || day > SATURDAY) {
            throw new IllegalArgumentException("Invalid day of week: " + day);
        }
    }

    private static void checkTime(int hour, int minutes) {
        if (hour < 0 || hour > 23 || minutes < 0 || minutes > 59) {
            throw new IllegalStateException("Hour and minutes invalid");
        }
    }
}
