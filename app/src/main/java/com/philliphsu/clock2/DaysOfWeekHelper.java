package com.philliphsu.clock2;

/**
 * Created by Phillip Hsu on 6/9/2016.
 */
public interface DaysOfWeekHelper {
    /** @return the week day at {@code position} within the user-defined week */
    int weekDayAt(int position);

    /** @return the position of the {@code weekDay} within the user-defined week */
    int positionOf(int weekDay);
}
