package com.philliphsu.clock2;

import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.philliphsu.clock2.DaysOfWeek.SATURDAY;
import static com.philliphsu.clock2.DaysOfWeek.SUNDAY;
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
        for (int i = SUNDAY; i <= SATURDAY; i++) {
            alarm.setRecurring(i, i % 2 == 0);
            assertTrue(alarm.isRecurring(i) == (i % 2 == 0));
        }
        assertTrue(alarm.hasRecurrence());
        
        // All false
        for (int i = SUNDAY; i <= SATURDAY; i++) {
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
    public void alarm_RingsAt_RecurringDays_ReturnsCorrectRingTime() {
        Calendar cal = new GregorianCalendar();
        int weekDayToday = cal.get(Calendar.DAY_OF_WEEK);

        for (int h = 0; h < 24; h++) {
            for (int m = 0; m < 60; m++) {
                for (int d = SUNDAY; d <= SATURDAY; d++) {
                    out.println(String.format("Testing %02d:%02d for day %d", h, m, d));
                    int hC = cal.get(HOUR_OF_DAY); // Current hour
                    int mC = cal.get(MINUTE);      // Current minute
                    Alarm a = Alarm.builder().hour(h).minutes(m).build();
                    a.setRecurring(d, true);
                    long calculatedRingTime;
                    boolean calculatedToNextDay = true;
                    if (h <= hC) {
                        if (m <= mC) {
                            calculatedRingTime = (23 - hC + h) * 3600000 + (60 - mC + m) * 60000;
                        } else {
                            calculatedRingTime = (m - mC) * 60000;
                            if (h < hC) {
                                calculatedRingTime += (24 - hC + h) * 3600000;
                            } else {
                                // h == hC
                                calculatedToNextDay = false;
                            }
                        }
                    } else {
                        if (m <= mC) {
                            calculatedRingTime = (h - hC - 1) * 3600000 + (60 - mC + m) * 60000;
                        } else {
                            calculatedRingTime = (h - hC) * 3600000 + (m - mC) * 60000;
                        }
                        calculatedToNextDay = false;
                    }

                    int day = d + 1; // Match up with day constant defined in Calendar class
                    int amount = calculatedToNextDay ? d : day; // the amount to add on
                    if (day > weekDayToday) {
                        calculatedRingTime += 24 * (amount - weekDayToday) * 3600000;
                    } else if (day < weekDayToday) {
                        calculatedRingTime += 24 * (Calendar.SATURDAY - weekDayToday + amount) * 3600000;
                    } else {
                        long initialTime = cal.getTimeInMillis();
                        // Temporarily add on whatever we have so far
                        cal.setTimeInMillis(initialTime + calculatedRingTime);
                        cal.set(SECOND, 0);
                        cal.set(MILLISECOND, 0);
                        if (calculatedToNextDay) {
                            // Temporarily subtract off a whole day's worth of millis
                            cal.add(HOUR_OF_DAY, -24);
                        }
                        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
                            calculatedRingTime += 24 * (calculatedToNextDay ? 6 : 7) * 3600000;
                        }
                        cal.setTimeInMillis(initialTime);
                    }

                    cal.setTimeInMillis(cal.getTimeInMillis() + calculatedRingTime);
                    cal.set(SECOND, 0);
                    cal.set(MILLISECOND, 0);
                    assertEquals(a.ringsAt(), cal.getTimeInMillis());
                    // VERY IMPORTANT TO RESET AT THE END!!!!
                    cal.setTimeInMillis(System.currentTimeMillis());
                }
            }
        }
    }

    @Test
    public void alarm_RingsAt_AllRecurringDays_ReturnsCorrectRingTime() {
        // The results of this test should be the same as the normal ringsAt test:
        // alarm_RingsAt_ReturnsCorrectRingTime().
        GregorianCalendar now = new GregorianCalendar();
        for (int h = 0; h < 24; h++) {
            for (int m = 0; m < 60; m++) {
                out.println(String.format("Testing %02d:%02d", h, m));
                int hC = now.get(HOUR_OF_DAY); // Current hour
                int mC = now.get(MINUTE);      // Current minute
                Alarm a = Alarm.builder().hour(h).minutes(m).build();
                for (int i = 0; i < 7; i++) {
                    a.setRecurring(i, true);
                }
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
    public void alarm_RingsAt_CurrentDayRecurring_ReturnsCorrectRingTime() {
        Calendar cal = new GregorianCalendar();
        int dC = cal.get(Calendar.DAY_OF_WEEK) - 1; // Current week day, converted to our values

        for (int h = 0; h < 24; h++) {
            for (int m = 0; m < 60; m++) {
                int hC = cal.get(HOUR_OF_DAY); // Current hour
                int mC = cal.get(MINUTE);      // Current minute
                Alarm a = Alarm.builder().hour(h).minutes(m).build();
                a.setRecurring(dC, true);

                // Quantities until the ring time (h, m)
                int days = 0;
                int hours = 0;
                int minutes = 0;

                if (h <= hC) {
                    if (m <= mC) {
                        days = 6;
                        hours = 23 - hC + h;
                        minutes = 60 - mC + m;
                    } else {
                        minutes = m - mC;
                        if (h < hC) {
                            days = 6;
                            hours = 24 - hC + h;
                        }
                    }
                } else {
                    if (m <= mC) {
                        hours = h - hC - 1;
                        minutes = 60 - mC + m;
                    } else {
                        hours = h - hC;
                        minutes = m - mC;
                    }
                }

                cal.add(HOUR_OF_DAY, 24 * days);
                cal.add(HOUR_OF_DAY, hours);
                cal.add(MINUTE, minutes);
                cal.set(SECOND, 0);
                cal.set(MILLISECOND, 0);
                assertEquals(a.ringsAt(), cal.getTimeInMillis());
                // VERY IMPORTANT TO RESET AT THE END!!!!
                cal.setTimeInMillis(System.currentTimeMillis());
            }
        }
    }

    @Test
    public void alarm_RingsAt_RecurringDayAfterCurrentDay_ReturnsCorrectRingTime() {
        Calendar cal = new GregorianCalendar();
        int dC = cal.get(Calendar.DAY_OF_WEEK) - 1; // Current week day, converted to our values

        for (int h = 0; h < 24; h++) {
            for (int m = 0; m < 60; m++) {
                // Start after the current day, using our value. Note that if the current day is Saturday,
                // this test won't run anything and would still pass.
                for (int d = dC + 1; d <= DaysOfWeek.SATURDAY; d++) {
                    out.println("Testing (h, m, d) = ("+h+", "+m+", "+d+")");
                    int hC = cal.get(HOUR_OF_DAY); // Current hour
                    int mC = cal.get(MINUTE);      // Current minute
                    Alarm a = Alarm.builder().hour(h).minutes(m).build();
                    a.setRecurring(d, true);

                    // Quantities until the ring time (h, m)
                    int days = 0;
                    int hours = 0;
                    int minutes = 0;

                    if (h <= hC) {
                        if (m <= mC) {
                            days = d - dC - 1;
                            hours = 23 - hC + h;
                            minutes = 60 - mC + m;
                        } else {
                            minutes = m - mC;
                            if (h < hC) {
                                days = d - dC - 1;
                                hours = 24 - hC + h;
                            } else {
                                // h == hC
                                days = d - dC;
                            }
                        }
                    } else {
                        if (m <= mC) {
                            days = d - dC;
                            hours = h - hC - 1;
                            minutes = 60 - mC + m;
                        } else {
                            days = d - dC;
                            hours = h - hC;
                            minutes = m - mC;
                        }
                    }

                    cal.add(HOUR_OF_DAY, 24 * days);
                    cal.add(HOUR_OF_DAY, hours);
                    cal.add(MINUTE, minutes);
                    cal.set(SECOND, 0);
                    cal.set(MILLISECOND, 0);
                    assertEquals(a.ringsAt(), cal.getTimeInMillis());
                    // VERY IMPORTANT TO RESET AT THE END!!!!
                    cal.setTimeInMillis(System.currentTimeMillis());
                }
            }
        }
    }

    @Test
    public void alarm_RingsAt_RecurringDayBeforeCurrentDay_ReturnsCorrectRingTime() {
        Calendar cal = new GregorianCalendar();
        int D_C = cal.get(Calendar.DAY_OF_WEEK); // Current week day as defined in Calendar class

        for (int h = 0; h < 24; h++) {
            for (int m = 0; m < 60; m++) {
                for (int D = Calendar.SUNDAY; D < D_C; D++) {
                    out.println("Testing (h, m, d) = ("+h+", "+m+", "+(D-1)+")");
                    int hC = cal.get(HOUR_OF_DAY); // Current hour
                    int mC = cal.get(MINUTE);      // Current minute
                    Alarm a = Alarm.builder().hour(h).minutes(m).build();
                    a.setRecurring(D - 1, true);

                    // Quantities until the ring time (h, m)
                    int days = 0;
                    int hours = 0;
                    int minutes = 0;

                    if (h <= hC) {
                        if (m <= mC) {
                            days = Calendar.SATURDAY - D_C + D - 1;
                            hours = 23 - hC + h;
                            minutes = 60 - mC + m;
                        } else {
                            minutes = m - mC;
                            if (h < hC) {
                                days = Calendar.SATURDAY - D_C + D - 1;
                                hours = 24 - hC + h;
                            } else {
                                // h == hC
                                days = Calendar.SATURDAY - D_C + D;
                            }
                        }
                    } else {
                        if (m <= mC) {
                            days = Calendar.SATURDAY - D_C + D;
                            hours = h - hC - 1;
                            minutes = 60 - mC + m;
                        } else {
                            days = Calendar.SATURDAY - D_C + D;
                            hours = h - hC;
                            minutes = m - mC;
                        }
                    }

                    cal.add(HOUR_OF_DAY, 24 * days);
                    cal.add(HOUR_OF_DAY, hours);
                    cal.add(MINUTE, minutes);
                    cal.set(SECOND, 0);
                    cal.set(MILLISECOND, 0);
                    assertEquals(a.ringsAt(), cal.getTimeInMillis());
                    // VERY IMPORTANT TO RESET AT THE END!!!!
                    cal.setTimeInMillis(System.currentTimeMillis());
                }
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
