package com.philliphsu.clock2.alarms.misc;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import java.text.DateFormatSymbols;
import java.util.Arrays;

/**
 * Created by Phillip Hsu on 5/30/2016.
 */
public final class DaysOfWeek {
    private static final String TAG = "DaysOfWeek";
    // DAY_OF_WEEK constants in Calendar class not zero-based
    public static final int SUNDAY    = 0;
    public static final int MONDAY    = 1;
    public static final int TUESDAY   = 2;
    public static final int WEDNESDAY = 3;
    public static final int THURSDAY  = 4;
    public static final int FRIDAY    = 5;
    public static final int SATURDAY  = 6;
    public static final int NUM_DAYS  = 7;

    private static final int[] DAYS = new int[NUM_DAYS];
    private static final String[] LABELS = new DateFormatSymbols().getShortWeekdays();

    private static DaysOfWeek sInstance;
    private static int sLastPreferredFirstDay;

    public static DaysOfWeek getInstance(Context context) {
        int preferredFirstDay = AlarmPreferences.firstDayOfWeek(context);
        if (sInstance == null || preferredFirstDay != sLastPreferredFirstDay) {
            sLastPreferredFirstDay = preferredFirstDay;
            sInstance = new DaysOfWeek(preferredFirstDay);
        }
        Log.d(TAG, sInstance.toString());
        return sInstance;
    }

    /**
     * @param weekday the zero-based index of the week day you would like to get the label for.

     */
    public static String getLabel(int weekday) {
        // This array was returned from DateFormatSymbols.getShortWeekdays().
        // We are supposed to use the constants in the Calendar class as indices, but the previous
        // implementation of this method used our own zero-based indices. For backward compatibility,
        // we add one to the index passed in to match up with the values of the Calendar constants.
        return LABELS[weekday + 1];
    }

    /** @return the week day at {@code position} within the user-defined week */
    public int weekDayAt(int position) {
        if (position < 0 || position > 6)
            throw new ArrayIndexOutOfBoundsException("Ordinal day out of range");
        return DAYS[position];
    }

    /** @return the position of {@code weekDay} within the user-defined week */
    public int positionOf(int weekDay) {
        if (weekDay < SUNDAY || weekDay > SATURDAY)
            throw new ArrayIndexOutOfBoundsException("Week day ("+weekDay+") out of range");
        for (int i = 0; i < DAYS.length; i++)
            if (DAYS[i] == weekDay)
                return i;
        return -1;
    }

    @Override
    public String toString() {
        return "DaysOfWeek{"
                + "DAYS=" + Arrays.toString(DAYS)
                + "}";
    }

    @VisibleForTesting
    DaysOfWeek(int firstDayOfWeek) {
        if (firstDayOfWeek != SATURDAY && firstDayOfWeek != SUNDAY && firstDayOfWeek != MONDAY)
            throw new IllegalArgumentException("Invalid first day of week: " + firstDayOfWeek);
        DAYS[0] = firstDayOfWeek;
        for (int i = 1; i < 7; i++) {
            if (firstDayOfWeek == SATURDAY) {
                DAYS[i] = i - 1;
            } else if (firstDayOfWeek == MONDAY) {
                if (i == 6) {
                    DAYS[i] = SUNDAY;
                } else {
                    DAYS[i] = i + 1;
                }
            } else {
                DAYS[i] = i;
            }
        }
    }
}
