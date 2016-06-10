package com.philliphsu.clock2;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;
import com.philliphsu.clock2.model.JsonSerializable;

import org.json.JSONArray;
import org.json.JSONException;
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

    // JSON property names
    private static final String KEY_SNOOZING_UNTIL_MILLIS = "snoozing_until_millis";
    private static final String KEY_ENABLED = "enabled";
    //private static final String KEY_ID = "id"; // Defined in JsonSerializable
    private static final String KEY_HOUR = "hour";
    private static final String KEY_MINUTES = "minutes";
    private static final String KEY_RECURRING_DAYS = "recurring_days";
    private static final String KEY_LABEL = "label";
    private static final String KEY_RINGTONE = "ringtone";
    private static final String KEY_VIBRATES = "vibrates";

    // ========= MUTABLE ==============
    private long snoozingUntilMillis;
    private boolean enabled;
    // ================================

    //public abstract long id();
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

    public static Alarm create(JSONObject jsonObject) {
        try {
            JSONArray a = (JSONArray) jsonObject.get(KEY_RECURRING_DAYS);
            boolean[] recurringDays = new boolean[a.length()];
            for (int i = 0; i < recurringDays.length; i++) {
                recurringDays[i] = a.getBoolean(i);
            }
            Alarm alarm = new AutoValue_Alarm.Builder()
                    .id(jsonObject.getLong(KEY_ID))
                    .hour(jsonObject.getInt(KEY_HOUR))
                    .minutes(jsonObject.getInt(KEY_MINUTES))
                    .recurringDays(recurringDays)
                    .label(jsonObject.getString(KEY_LABEL))
                    .ringtone(jsonObject.getString(KEY_RINGTONE))
                    .vibrates(jsonObject.getBoolean(KEY_VIBRATES))
                    .rebuild();
            alarm.setEnabled(jsonObject.getBoolean(KEY_ENABLED));
            alarm.snoozingUntilMillis = jsonObject.getLong(KEY_SNOOZING_UNTIL_MILLIS);
            return alarm;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static Builder builder() {
        // Unfortunately, default values must be provided for generated Builders.
        // Fields that were not set when build() is called will throw an exception.
        // TODO: How can QualityMatters get away with not setting defaults?????
        return new AutoValue_Alarm.Builder()
                .id(-1)
                .hour(0)
                .minutes(0)
                .recurringDays(new boolean[NUM_DAYS])
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

    public void stopSnoozing() {
        snoozingUntilMillis = 0;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setRecurring(int day, boolean recurring) {
        checkDay(day);
        recurringDays()[day] =  recurring;
    }

    public boolean isRecurring(int day) {
        checkDay(day);
        return recurringDays()[day];
    }

    public boolean hasRecurrence() {
        return numRecurringDays() > 0;
    }

    public int numRecurringDays() {
        int count = 0;
        for (boolean b : recurringDays())
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

    public int intId() {
        return (int) id();
    }

    @Override
    @NonNull
    public JSONObject toJsonObject() {
        try {
            return new JSONObject()
                    .put(KEY_SNOOZING_UNTIL_MILLIS, snoozingUntilMillis)
                    .put(KEY_ENABLED, enabled)
                    .put(KEY_ID, id())
                    .put(KEY_HOUR, hour())
                    .put(KEY_MINUTES, minutes())
                    .put(KEY_RECURRING_DAYS, new JSONArray(recurringDays()))
                    .put(KEY_LABEL, label())
                    .put(KEY_RINGTONE, ringtone())
                    .put(KEY_VIBRATES, vibrates());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @AutoValue.Builder
    public abstract static class Builder {
        private static long idCount = 0; // TODO: change to AtomicLong?
        // Builder is mutable, so these are inherently setter methods.
        // By omitting the set- prefix, we reduce the number of changes required to define the Builder
        // class after copying and pasting the accessor fields here.
        public abstract Builder id(long id);
        public abstract Builder hour(int hour);
        public abstract Builder minutes(int minutes);
        // TODO: If using an immutable collection instead, can use its Builder instance
        // and provide an "accumulating" method
        /*abstract boolean[] recurringDays();
        public final Builder setRecurring(int day, boolean recurs) {
            checkDay(day)
            recurringDays()[day] = recurs;
            return this;
        }
        */
        public abstract Builder recurringDays(boolean[] recurringDays);
        /*
        public final Builder recurringDay(boolean[] recurringDays) {
            this.recurringDays = Arrays.copyOf(recurringDays, NUM_DAYS);
            return this;
        }
        */
        public abstract Builder label(String label);
        public abstract Builder ringtone(String ringtone);
        public abstract Builder vibrates(boolean vibrates);
        // To enforce preconditions, split the build method into two. autoBuild() is hidden from
        // callers and is generated. You implement the public build(), which calls the generated
        // autoBuild() and performs your desired validations.
        /*not public*/abstract Alarm autoBuild();

        public Alarm build() {
            this.id(++idCount); // TOneverDO: change to post-increment without also adding offset of 1 to idCount in rebuild()
            Alarm alarm = autoBuild();
            checkTime(alarm.hour(), alarm.minutes());
            checkRecurringDaysArrayLength(alarm.recurringDays());
            return alarm;
        }

        /** <b>Should only be called when recreating an instance from JSON</b> */
        private Alarm rebuild() {
            Alarm alarm = autoBuild();
            idCount = alarm.id(); // prevent future instances from id collision
            checkTime(alarm.hour(), alarm.minutes());
            checkRecurringDaysArrayLength(alarm.recurringDays());
            return alarm;
        }
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

    private static void checkRecurringDaysArrayLength(boolean[] b) {
        if (b.length != NUM_DAYS) {
            throw new IllegalStateException("Invalid length for recurring days array");
        }
    }
}
