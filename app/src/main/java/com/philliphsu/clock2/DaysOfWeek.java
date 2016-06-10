package com.philliphsu.clock2;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.philliphsu.clock2.util.AlarmUtils;

import java.util.Arrays;

/**
 * Created by Phillip Hsu on 5/30/2016.
 */
public class DaysOfWeek implements DaysOfWeekHelper {
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
    private static final int[] LABELS_RES = new int[NUM_DAYS];
    
    static {
        LABELS_RES[SUNDAY] = R.string.sun;
        LABELS_RES[MONDAY] = R.string.mon;
        LABELS_RES[TUESDAY] = R.string.tue;
        LABELS_RES[WEDNESDAY] = R.string.wed;
        LABELS_RES[THURSDAY] = R.string.thu;
        LABELS_RES[FRIDAY] = R.string.fri;
        LABELS_RES[SATURDAY] = R.string.sat;
    }
    
    private static Context sAppContext;
    private static DaysOfWeek sInstance;
    private static int sLastPreferredFirstDay;

    public static DaysOfWeek getInstance(Context context) {
        sAppContext = context.getApplicationContext();
        int preferredFirstDay = AlarmUtils.firstDayOfWeek(context);
        if (sInstance == null || preferredFirstDay != sLastPreferredFirstDay) {
            sLastPreferredFirstDay = preferredFirstDay;
            sInstance = new DaysOfWeek(preferredFirstDay);
        }
        Log.d(TAG, sInstance.toString());
        return sInstance;
    }

    /** @param weekDay the day constant as defined in this class */
    public static String getLabel(int weekDay) {
        return sAppContext.getString(LABELS_RES[weekDay]);
    }

    @Override
    public int weekDayAt(int position) {
        if (position < 0 || position > 6)
            throw new ArrayIndexOutOfBoundsException("Ordinal day out of range");
        return DAYS[position];
    }

    @Override
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
