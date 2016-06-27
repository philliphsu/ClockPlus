package com.philliphsu.clock2.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.philliphsu.clock2.Alarm;

import static com.philliphsu.clock2.DaysOfWeek.FRIDAY;
import static com.philliphsu.clock2.DaysOfWeek.MONDAY;
import static com.philliphsu.clock2.DaysOfWeek.SATURDAY;
import static com.philliphsu.clock2.DaysOfWeek.SUNDAY;
import static com.philliphsu.clock2.DaysOfWeek.THURSDAY;
import static com.philliphsu.clock2.DaysOfWeek.TUESDAY;
import static com.philliphsu.clock2.DaysOfWeek.WEDNESDAY;

/**
 * Created by Phillip Hsu on 6/24/2016.
 *
 * TODO: We can generalize this class to all data models, not just Alarms.
 */
public class AlarmDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "alarms.db";
    private static final int VERSION_1 = 1;

    // TODO: Consider creating an inner class that implements BaseColumns
    // and defines all the columns.
    // TODO: Consider statically defining index constants for each column,
    // and then removing all cursor getColumnIndex() calls.
    private static final String TABLE_ALARMS = "alarms";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_HOUR = "hour";
    private static final String COLUMN_MINUTES = "minutes";
    private static final String COLUMN_LABEL = "label";
    private static final String COLUMN_RINGTONE = "ringtone";
    private static final String COLUMN_VIBRATES = "vibrates";
    private static final String COLUMN_ENABLED = "enabled";
    private static final String COLUMN_SNOOZING_UNTIL_MILLIS = "snoozing_until_millis";

    // TODO: Consider creating an inner class that implements BaseColumns
    // and defines all the columns.
    private static final String TABLE_ALARM_RECURRING_DAYS = "alarm_recurring_days";
    private static final String COLUMN_ALARM_ID = "alarm_id";
    private static final String COLUMN_SUNDAY = "sunday";
    private static final String COLUMN_MONDAY = "monday";
    private static final String COLUMN_TUESDAY = "tuesday";
    private static final String COLUMN_WEDNESDAY = "wednesday";
    private static final String COLUMN_THURSDAY = "thursday";
    private static final String COLUMN_FRIDAY = "friday";
    private static final String COLUMN_SATURDAY = "saturday";

    // Statically define column indices for the days so we don't have
    // to call cursor.getColumnIndex(COLUMN_[DAY]). We offset the value
    // by one because COLUMN_ALARM_ID is actually first.
    private static final int INDEX_SUNDAY = SUNDAY + 1;
    private static final int INDEX_MONDAY = MONDAY + 1;
    private static final int INDEX_TUESDAY = TUESDAY + 1;
    private static final int INDEX_WEDNESDAY = WEDNESDAY + 1;
    private static final int INDEX_THURSDAY = THURSDAY + 1;
    private static final int INDEX_FRIDAY = FRIDAY + 1;
    private static final int INDEX_SATURDAY = SATURDAY + 1;

    private static void createAlarmsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_ALARMS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_HOUR + " INTEGER NOT NULL, "
                + COLUMN_MINUTES + " INTEGER NOT NULL, "
                + COLUMN_LABEL + " TEXT, "
                + COLUMN_RINGTONE + " TEXT NOT NULL, "
                + COLUMN_VIBRATES + " INTEGER NOT NULL, "
                + COLUMN_ENABLED + " INTEGER NOT NULL, "
                + COLUMN_SNOOZING_UNTIL_MILLIS + " INTEGER);");
    }

    private static void createRecurringDaysTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_ALARM_RECURRING_DAYS + " ("
                + COLUMN_ALARM_ID + " INTEGER REFERENCES " + TABLE_ALARMS + "(" + COLUMN_ID + "), "
                + COLUMN_SUNDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_MONDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_TUESDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_WEDNESDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_THURSDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_FRIDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_SATURDAY + " INTEGER NOT NULL DEFAULT 0);");
    }

    public AlarmDatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION_1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // https://www.sqlite.org/datatype3.html
        // INTEGER data type is stored in 1, 2, 3, 4, 6, or 8 bytes depending on the magnitude
        // of the value. As soon as INTEGER values are read off of disk and into memory for processing,
        // they are converted to the most general datatype (8-byte signed integer).
        // 8 byte == 64 bits so this means they are read as longs...?
        createAlarmsTable(db);
        createRecurringDaysTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // I don't think we need to drop current tables unless you make structural changes
        // to the schema in the new version.
    }

    // TODO: Consider changing param to Alarm.Builder so the Alarm that will be
    // built can have its ID set.
    public long insertAlarm(Alarm alarm) {
        long id = getWritableDatabase().insert(TABLE_ALARMS, null, toContentValues(alarm));
        alarm.setId(id);
        getWritableDatabase().insert(TABLE_ALARM_RECURRING_DAYS, null, toRecurrenceContentValues(id, alarm));
        return id;
    }

    public int updateAlarm(Alarm oldAlarm, Alarm newAlarm) {
        newAlarm.setId(oldAlarm.getId());
        SQLiteDatabase db = getWritableDatabase();
        int rowsUpdatedInAlarmsTable = db.update(TABLE_ALARMS,
                toContentValues(newAlarm),
                COLUMN_ID + " = " + newAlarm.getId(),
                null);
        int rowsUpdatedInRecurrencesTable = db.update(TABLE_ALARM_RECURRING_DAYS,
                toRecurrenceContentValues(newAlarm.getId(), newAlarm),
                COLUMN_ALARM_ID + " = " + newAlarm.getId(),
                null);
        if (rowsUpdatedInAlarmsTable == rowsUpdatedInRecurrencesTable && rowsUpdatedInAlarmsTable == 1) {
            return 1;
        }
        throw new IllegalStateException("rows updated in TABLE_ALARMS = " + rowsUpdatedInAlarmsTable +
                ", rows updated in TABLE_ALARM_RECURRING_DAYS = " + rowsUpdatedInRecurrencesTable);
    }

    public int deleteAlarm(Alarm alarm) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsDeletedInAlarmsTable = db.delete(TABLE_ALARMS,
                COLUMN_ID + " = " + alarm.getId(),
                null);
        int rowsDeletedInRecurrencesTable = db.delete(TABLE_ALARM_RECURRING_DAYS,
                COLUMN_ALARM_ID + " = " + alarm.getId(),
                null);
        if (rowsDeletedInAlarmsTable == rowsDeletedInRecurrencesTable && rowsDeletedInAlarmsTable == 1) {
            return 1;
        }
        throw new IllegalStateException("rows deleted in TABLE_ALARMS = " + rowsDeletedInAlarmsTable +
                ", rows deleted in TABLE_ALARM_RECURRING_DAYS = " + rowsDeletedInRecurrencesTable);
    }

    public AlarmCursor queryAlarm(long id) {
        Cursor alarmCursor = getReadableDatabase().query(TABLE_ALARMS,
                null, // All columns
                COLUMN_ID + " = " + id, // Selection for this alarm id
                null, // selection args, none b/c id already specified in selection
                null, // group by
                null, // order/sort by
                null, // having
                "1"); // limit 1 row
        Cursor recurrenceCursor = getReadableDatabase().query(TABLE_ALARM_RECURRING_DAYS,
                null, // All columns
                COLUMN_ID + " = " + id, // selection
                null, // selection args
                null, // group by
                null, // order by
                null, // having
                "1");
        return new AlarmCursor(alarmCursor, recurrenceCursor);
    }

    public AlarmCursor queryAlarms() {
        // Select all rows and columns from both tables
        Cursor alarmCursor = getReadableDatabase().query(TABLE_ALARMS,
                null, null, null, null, null, null);
        Cursor recurrenceCursor = getReadableDatabase().query(TABLE_ALARM_RECURRING_DAYS,
                null, null, null, null, null, null);
        return new AlarmCursor(alarmCursor, recurrenceCursor);
    }

    private ContentValues toContentValues(Alarm alarm) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_HOUR, alarm.hour());
        values.put(COLUMN_MINUTES, alarm.minutes());
        values.put(COLUMN_LABEL, alarm.label());
        values.put(COLUMN_RINGTONE, alarm.ringtone());
        values.put(COLUMN_VIBRATES, alarm.vibrates());
        values.put(COLUMN_ENABLED, alarm.isEnabled());
        values.put(COLUMN_SNOOZING_UNTIL_MILLIS, alarm.snoozingUntil());
        return values;
    }

    // Even though the current impl of insertAlarm() sets the given id on the alarm, we require it
    // as a param just as an extra measure, in case you happen to not set it.
    private ContentValues toRecurrenceContentValues(long id, Alarm alarm) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ALARM_ID, id); // *could* have used alarm.id() instead
        values.put(COLUMN_SUNDAY, alarm.isRecurring(SUNDAY));
        values.put(COLUMN_MONDAY, alarm.isRecurring(MONDAY));
        values.put(COLUMN_TUESDAY, alarm.isRecurring(TUESDAY));
        values.put(COLUMN_WEDNESDAY, alarm.isRecurring(WEDNESDAY));
        values.put(COLUMN_THURSDAY, alarm.isRecurring(THURSDAY));
        values.put(COLUMN_FRIDAY, alarm.isRecurring(FRIDAY));
        values.put(COLUMN_SATURDAY, alarm.isRecurring(SATURDAY));
        return values;
    }

    public static class AlarmCursor extends CursorWrapper {

        private final Cursor mRecurrenceCursor;

        public AlarmCursor(Cursor alarmCursor, Cursor recurrenceCursor) {
            super(alarmCursor);
            mRecurrenceCursor = recurrenceCursor;
        }

        /**
         * @return an Alarm instance configured for the current row,
         * or null if the current row is invalid
         */
        public Alarm getAlarm() {
            if (isBeforeFirst() || isAfterLast())
                return null;
            Alarm alarm = Alarm.builder()
                    .hour(getInt(getColumnIndex(COLUMN_HOUR)))
                    .minutes(getInt(getColumnIndex(COLUMN_MINUTES)))
                    .vibrates(getInt(getColumnIndex(COLUMN_VIBRATES)) == 1)
                    .ringtone(getString(getColumnIndex(COLUMN_RINGTONE)))
                    .label(getString(getColumnIndex(COLUMN_LABEL)))
                    .build();
            alarm.setId(getLong(getColumnIndex(COLUMN_ID)));
            alarm.setEnabled(getInt(getColumnIndex(COLUMN_ENABLED)) == 1);
            alarm.setSnoozing(getLong(getColumnIndex(COLUMN_SNOOZING_UNTIL_MILLIS)));
            alarm.setRecurring(SUNDAY, isRecurring(INDEX_SUNDAY));
            alarm.setRecurring(MONDAY, isRecurring(INDEX_MONDAY));
            alarm.setRecurring(TUESDAY, isRecurring(INDEX_TUESDAY));
            alarm.setRecurring(WEDNESDAY, isRecurring(INDEX_WEDNESDAY));
            alarm.setRecurring(THURSDAY, isRecurring(INDEX_THURSDAY));
            alarm.setRecurring(FRIDAY, isRecurring(INDEX_FRIDAY));
            alarm.setRecurring(SATURDAY, isRecurring(INDEX_SATURDAY));
            return alarm;
        }
        
        private boolean isRecurring(int dayColumnIndex) {
            return mRecurrenceCursor.getInt(dayColumnIndex) == 1;
        }
    }
}
