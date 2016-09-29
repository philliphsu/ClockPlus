/*
 * Copyright (C) 2016 Phillip Hsu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.philliphsu.clock2;

import com.philliphsu.clock2.alarms.Alarm;
import com.philliphsu.clock2.alarms.misc.DaysOfWeek;

import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.philliphsu.clock2.alarms.misc.DaysOfWeek.SATURDAY;
import static com.philliphsu.clock2.alarms.misc.DaysOfWeek.SUNDAY;
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
    public void alarm_RingsAt_NoRecurrence_ReturnsCorrectRingTime() {
        GregorianCalendar now = new GregorianCalendar();
        for (int h = 0; h < 24; h++) {
            for (int m = 0; m < 60; m++) {
                out.println(String.format("Testing %02d:%02d", h, m));
                Alarm a = Alarm.builder().hour(h).minutes(m).build();
                calculateRingTimeAndTest(h, m, 0 /*days*/, now, a.ringsAt());
            }
        }
    }

    @Test
    public void alarm_RingsAt_RecurringDays_ReturnsCorrectRingTime() {
        Calendar cal = new GregorianCalendar();
        int D_C = cal.get(Calendar.DAY_OF_WEEK);

        for (int h = 0; h < 24; h++) {
            for (int m = 0; m < 60; m++) {
                for (int D = Calendar.SUNDAY; D <= Calendar.SATURDAY; D++) {
                    out.println("Testing (h, m, d) = ("+h+", "+m+", "+ (D-1) +")");
                    int hC = cal.get(HOUR_OF_DAY); // Current hour
                    int mC = cal.get(MINUTE);      // Current minute
                    Alarm a = Alarm.builder().hour(h).minutes(m).build();
                    a.setRecurring(D - 1, true);

                    int days = 0;

                    if (h > hC || (h == hC && m > mC)) {
                        if (D < D_C) {
                            days = Calendar.SATURDAY - D_C + D;
                        } else if (D == D_C) {
                            days = 0; // upcoming on the same day
                        } else {
                            days = D - D_C;
                        }
                    } else if (h <= hC) {
                        if (D < D_C) {
                            days = Calendar.SATURDAY - D_C + D - 1;
                        } else if (D == D_C) {
                            days = 6;
                        } else {
                            days = D - D_C - 1;
                        }
                    }

                    calculateRingTimeAndTest(h, m, days, cal, a.ringsAt());
                }
            }
        }
    }

    /*
     * Set recurring days in a queue and test that, regardless, ringsAt() ALWAYS returns the ring time
     * that is closest to the current day. In other words, the ring time that comes first in the queue.
     */
    @Test
    public void alarm_RingsAt_ForwardQueueingRecurringDays() {
        Calendar cal = new GregorianCalendar();
        int D_C = cal.get(Calendar.DAY_OF_WEEK);

        for (int h = 0; h < 24; h++) {
            for (int m = 0; m < 60; m++) {
                Alarm a = Alarm.builder().hour(h).minutes(m).build();
                for (int D = Calendar.SUNDAY; D <= Calendar.SATURDAY; D++) {
                    out.println("Testing (h, m, d) = ("+h+", "+m+", "+ (D-1) +")");
                    int hC = cal.get(HOUR_OF_DAY); // Current hour
                    int mC = cal.get(MINUTE);      // Current minute
                    a.setRecurring(D - 1, true);

                    int days = 0;

                    if (D == D_C) {
                        if (h > hC || (h == hC && m > mC)) {
                            days = 0;
                        } else if (h <= hC) {
                            days = 6;
                        }
                    }

                    calculateRingTimeAndTest(h, m, days, cal, a.ringsAt());
                }
            }
        }
    }

    /*
     * Set recurring days in a queue and test that, regardless, ringsAt() ALWAYS returns the ring time
     * that is closest to the current day. In other words, the ring time that comes first in the queue.
     */
    @Test
    public void alarm_RingsAt_BackwardQueueingRecurringDays() {
        Calendar cal = new GregorianCalendar();
        int D_C = cal.get(Calendar.DAY_OF_WEEK);

        for (int h = 0; h < 24; h++) {
            for (int m = 0; m < 60; m++) {
                Alarm a = Alarm.builder().hour(h).minutes(m).build();
                for (int D = Calendar.SATURDAY; D >= Calendar.SUNDAY; D--) {
                    out.println("Testing (h, m, d) = ("+h+", "+m+", "+ (D-1) +")");
                    int hC = cal.get(HOUR_OF_DAY); // Current hour
                    int mC = cal.get(MINUTE);      // Current minute
                    a.setRecurring(D - 1, true);

                    int days = 0;

                    if (h > hC || (h == hC && m > mC)) {
                        if (D < D_C) {
                            days = Calendar.SATURDAY - D_C + D;
                        } else if (D == D_C) {
                            days = 0; // upcoming on the same day
                        } else {
                            days = D - D_C;
                        }
                    } else if (h <= hC) {
                        if (D < D_C) {
                            days = Calendar.SATURDAY - D_C + D - 1;
                        } else if (D == D_C) {
                            days = 0;
                        } else {
                            days = D - D_C - 1;
                        }
                    }
                    
                    calculateRingTimeAndTest(h, m, days, cal, a.ringsAt());
                }
            }
        }
    }

    @Test
    public void alarm_RingsAt_MiddleOutQueueingRecurringDays() {
        Calendar cal = new GregorianCalendar();
        int D_C = cal.get(Calendar.DAY_OF_WEEK);

        for (int h = 0; h < 24; h++) {
            for (int m = 0; m < 60; m++) {
                Alarm a = Alarm.builder().hour(h).minutes(m).build();
                a.setRecurring(DaysOfWeek.WEDNESDAY, true);
                long wednesdayRingTime = a.ringsAt();
                for (int D = Calendar.THURSDAY; D <= Calendar.SATURDAY; D++) {
                    out.println("Wednesday ring time: " + wednesdayRingTime);
                    // Check that the ring time is always on Wednesday
                    a.setRecurring(D - 1, true);
                    assertEquals(wednesdayRingTime, a.ringsAt());
                }
                for (int D = Calendar.TUESDAY; D >= Calendar.SUNDAY; D--) {
                    // Check that the ring time is earlier after each iteration
                    int hC = cal.get(HOUR_OF_DAY); // Current hour
                    int mC = cal.get(MINUTE);      // Current minute
                    a.setRecurring(D - 1, true);

                    int days = 0;

                    if (h > hC || (h == hC && m > mC)) {
                        if (D < D_C) {
                            days = Calendar.SATURDAY - D_C + D;
                        } else if (D == D_C) {
                            days = 0; // upcoming on the same day
                        } else {
                            days = D - D_C;
                        }
                    } else if (h <= hC) {
                        if (D < D_C) {
                            days = Calendar.SATURDAY - D_C + D - 1;
                        } else if (D == D_C) {
                            days = 0;
                        } else {
                            days = D - D_C - 1;
                        }
                    }

                    calculateRingTimeAndTest(h, m, days, cal, a.ringsAt());
                }
            }
        }
    }

    @Test
    public void alarm_RingsAt_AllRecurringDays_ReturnsCorrectRingTime() {
        // The results of this test should be the same as the normal ringsAt test:
        // alarm_RingsAt_NoRecurrence_ReturnsCorrectRingTime().
        GregorianCalendar now = new GregorianCalendar();
        for (int h = 0; h < 24; h++) {
            for (int m = 0; m < 60; m++) {
                Alarm a = Alarm.builder().hour(h).minutes(m).build();
                for (int i = 0; i < 7; i++) {
                    a.setRecurring(i, true);
                }
                calculateRingTimeAndTest(h, m, 0, now, a.ringsAt());
            }
        }
    }

    @Test
    public void alarm_RingsAt_RecurringDayIsCurrentDay_ReturnsCorrectRingTime() {
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

    /**
     * Calculates the remaining time until the ring time (h, m) and tests if the
     * calculated value agrees with the {@code expected} value. Callers are expected
     * to calculate the number of days until this ring time (h, m) themselves, because
     * the manner of its calculation is dependent on the recurrence situation.
     * @param h the hours of the ring time under test
     * @param m the minutes of the ring time under test
     * @param days your calculated number of days until this ring time
     * @param calendar the Calendar instantiated in your test
     * @param expected the expected ring time to be compared against the calculated value
     */
    private void calculateRingTimeAndTest(int h, int m, int days, Calendar calendar, long expected) {
        int hC = calendar.get(HOUR_OF_DAY); // Current hour
        int mC = calendar.get(MINUTE);      // Current minute

        int hours = 0;
        int minutes = 0;

        if (h <= hC) {
            if (m <= mC) {
                hours = 23 - hC + h;
                minutes = 60 - mC + m;
            } else {
                minutes = m - mC;
                if (h < hC) {
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

        calendar.add(HOUR_OF_DAY, 24 * days);
        calendar.add(HOUR_OF_DAY, hours);
        calendar.add(MINUTE, minutes);
        calendar.set(SECOND, 0);
        calendar.set(MILLISECOND, 0);

        long time = calendar.getTimeInMillis();
        out.println("Calculated time: " + time);
        assertEquals(time, expected);
        // RESET AT END!!!!
        calendar.setTimeInMillis(System.currentTimeMillis());
    }
}
