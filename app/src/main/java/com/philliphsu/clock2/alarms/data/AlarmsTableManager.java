/*
 * Copyright 2017 Phillip Hsu
 *
 * This file is part of ClockPlus.
 *
 * ClockPlus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ClockPlus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ClockPlus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.philliphsu.clock2.alarms.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.philliphsu.clock2.alarms.Alarm;
import com.philliphsu.clock2.data.DatabaseTableManager;

import static com.philliphsu.clock2.alarms.misc.DaysOfWeek.FRIDAY;
import static com.philliphsu.clock2.alarms.misc.DaysOfWeek.MONDAY;
import static com.philliphsu.clock2.alarms.misc.DaysOfWeek.SATURDAY;
import static com.philliphsu.clock2.alarms.misc.DaysOfWeek.SUNDAY;
import static com.philliphsu.clock2.alarms.misc.DaysOfWeek.THURSDAY;
import static com.philliphsu.clock2.alarms.misc.DaysOfWeek.TUESDAY;
import static com.philliphsu.clock2.alarms.misc.DaysOfWeek.WEDNESDAY;

/**
 * Created by Phillip Hsu on 7/30/2016.
 */
public class AlarmsTableManager extends DatabaseTableManager<Alarm> {

    public AlarmsTableManager(Context context) {
        super(context);
    }

    @Override
    protected String getQuerySortOrder() {
        return AlarmsTable.NEW_SORT_ORDER;
    }

    @Override
    public AlarmCursor queryItem(long id) {
        return wrapInAlarmCursor(super.queryItem(id));
    }

    @Override
    public AlarmCursor queryItems() {
        return wrapInAlarmCursor(super.queryItems());
    }

    public AlarmCursor queryEnabledAlarms() {
        return queryItems(AlarmsTable.COLUMN_ENABLED + " = 1", null);
    }

    @Override
    protected AlarmCursor queryItems(String where, String limit) {
        return wrapInAlarmCursor(super.queryItems(where, limit));
    }

    @Override
    protected String getTableName() {
        return AlarmsTable.TABLE_ALARMS;
    }

    @Override
    protected ContentValues toContentValues(Alarm alarm) {
        ContentValues values = new ContentValues();
        values.put(AlarmsTable.COLUMN_HOUR, alarm.hour());
        values.put(AlarmsTable.COLUMN_MINUTES, alarm.minutes());
        values.put(AlarmsTable.COLUMN_LABEL, alarm.label());
        values.put(AlarmsTable.COLUMN_RINGTONE, alarm.ringtone());
        values.put(AlarmsTable.COLUMN_VIBRATES, alarm.vibrates());
        values.put(AlarmsTable.COLUMN_ENABLED, alarm.isEnabled());
        values.put(AlarmsTable.COLUMN_RING_TIME_MILLIS, alarm.ringsAt());
        values.put(AlarmsTable.COLUMN_SNOOZING_UNTIL_MILLIS, alarm.snoozingUntil());
        values.put(AlarmsTable.COLUMN_SUNDAY, alarm.isRecurring(SUNDAY));
        values.put(AlarmsTable.COLUMN_MONDAY, alarm.isRecurring(MONDAY));
        values.put(AlarmsTable.COLUMN_TUESDAY, alarm.isRecurring(TUESDAY));
        values.put(AlarmsTable.COLUMN_WEDNESDAY, alarm.isRecurring(WEDNESDAY));
        values.put(AlarmsTable.COLUMN_THURSDAY, alarm.isRecurring(THURSDAY));
        values.put(AlarmsTable.COLUMN_FRIDAY, alarm.isRecurring(FRIDAY));
        values.put(AlarmsTable.COLUMN_SATURDAY, alarm.isRecurring(SATURDAY));
        values.put(AlarmsTable.COLUMN_IGNORE_UPCOMING_RING_TIME, alarm.isIgnoringUpcomingRingTime());
        return values;
    }

    @Override
    protected String getOnContentChangeAction() {
        return AlarmsListCursorLoader.ACTION_CHANGE_CONTENT;
    }

    private AlarmCursor wrapInAlarmCursor(Cursor c) {
        return new AlarmCursor(c);
    }
}
