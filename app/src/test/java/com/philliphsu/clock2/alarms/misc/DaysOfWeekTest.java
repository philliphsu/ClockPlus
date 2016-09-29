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

package com.philliphsu.clock2.alarms.misc;

import org.junit.Test;

import static com.philliphsu.clock2.alarms.misc.DaysOfWeek.FRIDAY;
import static com.philliphsu.clock2.alarms.misc.DaysOfWeek.MONDAY;
import static com.philliphsu.clock2.alarms.misc.DaysOfWeek.SATURDAY;
import static com.philliphsu.clock2.alarms.misc.DaysOfWeek.SUNDAY;
import static com.philliphsu.clock2.alarms.misc.DaysOfWeek.THURSDAY;
import static com.philliphsu.clock2.alarms.misc.DaysOfWeek.TUESDAY;
import static com.philliphsu.clock2.alarms.misc.DaysOfWeek.WEDNESDAY;
import static org.junit.Assert.assertEquals;

/**
 * Created by Phillip Hsu on 6/1/2016.
 */
public class DaysOfWeekTest {

    @Test
    public void testSundayAsFirstDayOfWeek() {
        DaysOfWeek days = new DaysOfWeek(SUNDAY);
        assertEquals(days.weekDayAt(0), SUNDAY);
        assertEquals(days.weekDayAt(1), MONDAY);
        assertEquals(days.weekDayAt(2), TUESDAY);
        assertEquals(days.weekDayAt(3), WEDNESDAY);
        assertEquals(days.weekDayAt(4), THURSDAY);
        assertEquals(days.weekDayAt(5), FRIDAY);
        assertEquals(days.weekDayAt(6), SATURDAY);

        assertEquals(days.positionOf(SUNDAY), 0);
        assertEquals(days.positionOf(MONDAY), 1);
        assertEquals(days.positionOf(TUESDAY), 2);
        assertEquals(days.positionOf(WEDNESDAY), 3);
        assertEquals(days.positionOf(THURSDAY), 4);
        assertEquals(days.positionOf(FRIDAY), 5);
        assertEquals(days.positionOf(SATURDAY), 6);
    }

    @Test
    public void testSaturdayAsFirstDayOfWeek() {
        DaysOfWeek days = new DaysOfWeek(SATURDAY);
        assertEquals(days.weekDayAt(0), SATURDAY);
        assertEquals(days.weekDayAt(1), SUNDAY);
        assertEquals(days.weekDayAt(2), MONDAY);
        assertEquals(days.weekDayAt(3), TUESDAY);
        assertEquals(days.weekDayAt(4), WEDNESDAY);
        assertEquals(days.weekDayAt(5), THURSDAY);
        assertEquals(days.weekDayAt(6), FRIDAY);

        assertEquals(days.positionOf(SUNDAY), 1);
        assertEquals(days.positionOf(MONDAY), 2);
        assertEquals(days.positionOf(TUESDAY), 3);
        assertEquals(days.positionOf(WEDNESDAY), 4);
        assertEquals(days.positionOf(THURSDAY), 5);
        assertEquals(days.positionOf(FRIDAY), 6);
        assertEquals(days.positionOf(SATURDAY), 0);
    }

    @Test
    public void testMondayAsFirstDayOfWeek() {
        DaysOfWeek days = new DaysOfWeek(MONDAY);
        assertEquals(days.weekDayAt(0), MONDAY);
        assertEquals(days.weekDayAt(1), TUESDAY);
        assertEquals(days.weekDayAt(2), WEDNESDAY);
        assertEquals(days.weekDayAt(3), THURSDAY);
        assertEquals(days.weekDayAt(4), FRIDAY);
        assertEquals(days.weekDayAt(5), SATURDAY);
        assertEquals(days.weekDayAt(6), SUNDAY);

        assertEquals(days.positionOf(SUNDAY), 6);
        assertEquals(days.positionOf(MONDAY), 0);
        assertEquals(days.positionOf(TUESDAY), 1);
        assertEquals(days.positionOf(WEDNESDAY), 2);
        assertEquals(days.positionOf(THURSDAY), 3);
        assertEquals(days.positionOf(FRIDAY), 4);
        assertEquals(days.positionOf(SATURDAY), 5);
    }
}
