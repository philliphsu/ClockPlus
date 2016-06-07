package com.philliphsu.clock2.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.philliphsu.clock2.R;

import java.util.concurrent.TimeUnit;

/**
 * Created by Phillip Hsu on 6/6/2016.
 */
public class DurationUtils {
    public static final int HOURS = 0;
    public static final int MINUTES = 1;
    public static final int SECONDS = 2;
    public static final int MILLIS = 3;

    /** Return a string representing the duration, formatted in hours and minutes.
     * TODO: Need to adapt this to represent all time fields eventually */
    public static String toString(Context context, long millis, boolean abbreviate) {
        long[] fields = breakdown(millis);
        long numHours = fields[0];
        long numMins = fields[1];
        long numSecs = fields[2]; // only considered for rounding of minutes
        if (numSecs >= 31) {
            numMins++;
            numSecs = 0; // Not totally necessary since it won't be considered any more
            if (numMins == 60) {
                numHours++;
                numMins = 0;
            }
        }

        @StringRes int res;

        if (abbreviate) {
            if (numHours == 0) {
                if (numMins == 0) {
                    res = R.string.abbrev_less_than_one_minute;
                } else {
                    res = R.string.abbrev_minutes;
                }
            } else {
                if (numMins == 0) {
                    res = R.string.abbrev_hours;
                } else {
                    res = R.string.abbrev_hours_and_minutes;
                }
            }
        } else {
            if (numHours == 0) {
                if (numMins == 0) {
                    res = R.string.less_than_one_minute;
                } else if (numMins == 1) {
                    res = R.string.minute;
                } else {
                    res = R.string.minutes;
                }
            } else {
                if (numHours == 1) {
                    if (numMins == 0) {
                        res = R.string.hour;
                    } else if (numMins == 1) {
                        res = R.string.hour_and_minute;
                    } else {
                        res = R.string.hour_and_minutes;
                    }
                } else {
                    if (numMins == 0) {
                        res = R.string.hours;
                    } else if (numMins == 1) {
                        res = R.string.hours_and_minute;
                    } else {
                        res = R.string.hours_and_minutes;
                    }
                }
            }
        }
        
        return context.getString(res, numHours, numMins);
    }

    /**
     * Equivalent to
     * {@link #breakdown(long, TimeUnit, boolean)
     * breakdown(millis, TimeUnit.MILLISECONDS, true)},
     * which rounds milliseconds. Callers who use this are probably not
     * concerned about displaying the milliseconds value.
     */
    public static long[] breakdown(long millis) {
        return breakdown(millis, TimeUnit.MILLISECONDS, true);
    }

    /**
     * Equivalent to
     * {@link #breakdown(long, TimeUnit, boolean) breakdown(t, unit, false)},
     * i.e. does not round milliseconds.
     */
    public static long[] breakdown(long t, @NonNull TimeUnit unit) {
        return breakdown(t, unit, false);
    }

    /**
     * Returns a breakdown of a given time into its values
     * in hours, minutes, seconds and milliseconds.
     * @param t the time to break down
     * @param unit the {@link TimeUnit} the given time is expressed in
     * @param roundMillis whether rounding of milliseconds is desired
     * @return a {@code long[]} of the values in hours, minutes, seconds
     *         and milliseconds in that order
     */
    public static long[] breakdown(long t, @NonNull TimeUnit unit, boolean roundMillis) {
        long hours = unit.toHours(t);
        long minutes = unit.toMinutes(t) % 60;
        long seconds = unit.toSeconds(t) % 60;
        long msecs = unit.toMillis(t) % 1000;
        if (roundMillis) {
            if (msecs >= 500) {
                seconds++;
                msecs = 0;
                if (seconds == 60) {
                    minutes++;
                    seconds = 0;
                    if (minutes == 60) {
                        hours++;
                        minutes = 0;
                    }
                }
            }
        }
        return new long[] { hours, minutes, seconds, msecs };
    }

}
