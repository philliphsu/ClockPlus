package com.philliphsu.clock2;

import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static java.lang.System.out;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.SECOND;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Phillip Hsu on 5/27/2016.
 */
public class AlarmTest {

    @Test
    public void setRecurringDays_VerifyElementsSetCorrectly() {
        Alarm alarm = Alarm.builder().build();
        
        // Some true, some false
        for (int i = Alarm.SUNDAY; i <= Alarm.SATURDAY; i++) {
            alarm.setRecurring(i, i % 2 == 0);
            assertTrue(alarm.isRecurring(i) == (i % 2 == 0));
        }
        assertTrue(alarm.hasRecurrence());
        
        // All false
        for (int i = Alarm.SUNDAY; i <= Alarm.SATURDAY; i++) {
            alarm.setRecurring(i, false);
            assertFalse(alarm.isRecurring(i));
        }
        assertFalse(alarm.hasRecurrence());

        try {
            alarm.setRecurring(7, true);
            alarm.setRecurring(-3, false);
        } catch (IllegalArgumentException e) {
            out.println("Caught setting recurrence for invalid days");
        }
    }

    @Test
    public void alarm_RingsAt_ReturnsCorrectRingTime() {
        GregorianCalendar now = new GregorianCalendar();
        for (int h = 0; h < 24; h++) {
            for (int m = 0; m < 60; m++) {
                out.println(String.format("Testing %02d:%02d", h, m));
                int hC = now.get(HOUR_OF_DAY); // Current hour
                int mC = now.get(MINUTE);      // Current minute
                Alarm a = Alarm.builder().hour(h).minutes(m).build();
                long calculatedRingTime;
                if (h <= hC) {
                    if (m <= mC) {
                        calculatedRingTime = (23-hC+h)*3600000 + (60-mC+m)*60000;
                    } else {
                        calculatedRingTime = (m-mC)*60000;
                        if (h < hC) {
                            calculatedRingTime += (24-hC+h)*3600000;
                        }
                    }
                } else {
                    if (m <= mC) {
                        calculatedRingTime = (h-hC-1)*3600000+(60-mC+m)*60000;
                    } else {
                        calculatedRingTime = (h-hC)*3600000+(m-mC)*60000;
                    }
                }
                now.setTimeInMillis(now.getTimeInMillis() + calculatedRingTime);
                now.set(SECOND, 0);
                now.set(MILLISECOND, 0);
                assertEquals(a.ringsAt(), now.getTimeInMillis());
                // VERY IMPORTANT TO RESET AT THE END!!!! THIS TOOK A WHOLE FUCKING DAY OF BUG HUNTING!!!
                now.setTimeInMillis(System.currentTimeMillis());
            }
        }
    }

    @Test
    public void snoozeAlarm_AssertEquals_SnoozingUntilMillis_CorrespondsToWallClock() {
        Calendar cal = new GregorianCalendar();
        cal.add(MINUTE, 10);
        Alarm alarm = Alarm.builder().build();
        alarm.snooze(10);
        // Unlike ring times, the snoozingUntilMillis has seconds and millis components.
        // Due to the overhead of computation, the two time values will inherently have some
        // millis difference. However, if the difference is meaningfully small enough, then
        // for all practical purposes, we can consider them equal.
        assertTrue(Math.abs(alarm.snoozingUntil() - cal.getTimeInMillis()) <= 10);
    }

    @Test
    public void snoozeAlarm_IsSnoozed_ReturnsTrue_ForAllMillisUpToButExcluding_SnoozingUntilMillis() {
        Alarm alarm = Alarm.builder().build();
        alarm.snooze(1);
        // Stop 10ms early so System.currentTimeMillis() doesn't exceed the target time in the middle
        // of an iteration.
        while (System.currentTimeMillis() < alarm.snoozingUntil() - 10) {
            assertTrue(alarm.isSnoozed());
        }
        // Wait just in case so the target time passes.
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            assertFalse(alarm.isSnoozed());
            // Check if the snoozingUntilMillis is cleared
            assertEquals(0, alarm.snoozingUntil());
        }
    }
}
