package com.philliphsu.clock2;

import org.junit.Test;

import static com.philliphsu.clock2.DaysOfWeek.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Phillip Hsu on 6/1/2016.
 */
public class DaysOfWeekTest {

    @Test
    public void testSundayAsFirstDayOfWeek() {
        DaysOfWeek days = new DaysOfWeek(SUNDAY);
        assertEquals(days.weekDay(0), SUNDAY);
        assertEquals(days.weekDay(1), MONDAY);
        assertEquals(days.weekDay(2), TUESDAY);
        assertEquals(days.weekDay(3), WEDNESDAY);
        assertEquals(days.weekDay(4), THURSDAY);
        assertEquals(days.weekDay(5), FRIDAY);
        assertEquals(days.weekDay(6), SATURDAY);

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
        assertEquals(days.weekDay(0), SATURDAY);
        assertEquals(days.weekDay(1), SUNDAY);
        assertEquals(days.weekDay(2), MONDAY);
        assertEquals(days.weekDay(3), TUESDAY);
        assertEquals(days.weekDay(4), WEDNESDAY);
        assertEquals(days.weekDay(5), THURSDAY);
        assertEquals(days.weekDay(6), FRIDAY);

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
        assertEquals(days.weekDay(0), MONDAY);
        assertEquals(days.weekDay(1), TUESDAY);
        assertEquals(days.weekDay(2), WEDNESDAY);
        assertEquals(days.weekDay(3), THURSDAY);
        assertEquals(days.weekDay(4), FRIDAY);
        assertEquals(days.weekDay(5), SATURDAY);
        assertEquals(days.weekDay(6), SUNDAY);

        assertEquals(days.positionOf(SUNDAY), 6);
        assertEquals(days.positionOf(MONDAY), 0);
        assertEquals(days.positionOf(TUESDAY), 1);
        assertEquals(days.positionOf(WEDNESDAY), 2);
        assertEquals(days.positionOf(THURSDAY), 3);
        assertEquals(days.positionOf(FRIDAY), 4);
        assertEquals(days.positionOf(SATURDAY), 5);

        System.out.println(days.toString());
    }
}
