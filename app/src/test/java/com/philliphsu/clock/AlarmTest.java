package com.philliphsu.clock;

import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.YEAR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Phillip Hsu on 5/27/2016.
 */
public class AlarmTest {

    @Test
    public void testRecurrence() {
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
    }

    @Test
    public void alarm_RingsAt_ReturnsCorrectRingTime() {
        GregorianCalendar now = new GregorianCalendar();
        for (int h = 0; h < 24; h++) {
            for (int m = 0; m < 60; m++) {
                System.out.println(String.format("Testing %02d:%02d", h, m));
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
        // Expected
        Calendar cal = new GregorianCalendar();
        cal.add(MINUTE, 10);
        // Actual
        Calendar snoozeCal = new GregorianCalendar();
        Alarm alarm = Alarm.builder().build();
        alarm.snooze(10);
        snoozeCal.setTimeInMillis(alarm.snoozingUntil());

        assertEquals(cal.get(YEAR), snoozeCal.get(YEAR));
        assertEquals(cal.get(MONTH), snoozeCal.get(MONTH));
        assertEquals(cal.get(DAY_OF_MONTH), snoozeCal.get(DAY_OF_MONTH));
        assertEquals(cal.get(DAY_OF_WEEK), snoozeCal.get(DAY_OF_WEEK));
        assertEquals(cal.get(HOUR_OF_DAY), snoozeCal.get(HOUR_OF_DAY));
        assertEquals(cal.get(MINUTE), snoozeCal.get(MINUTE));
        assertEquals(cal.get(SECOND), snoozeCal.get(SECOND));
        // Milliseconds not required to be equal, because they will always
        // have some difference
    }

    @Test
    public void snoozeAlarm_IsSnoozed_ReturnsTrue_ForAllMillisUpToButExcluding_SnoozingUntilMillis() {
        Alarm alarm = Alarm.builder().build();
        alarm.snooze(1);
        // If all iterations leading up to 20ms before the target time evaluate to true,
        // that is good enough and we don't really care about the last 20ms.
        while (alarm.snoozingUntil() - System.currentTimeMillis() > 20) {
            assertTrue(alarm.isSnoozed());
        }
        // Wait long enough so the target time passes.
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
