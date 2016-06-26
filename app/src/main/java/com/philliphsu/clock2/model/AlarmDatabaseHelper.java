package com.philliphsu.clock2.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.DaysOfWeek;

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
        ContentValues values = new ContentValues();
        values.put(COLUMN_HOUR, alarm.hour());
        values.put(COLUMN_MINUTES, alarm.minutes());
        values.put(COLUMN_LABEL, alarm.label());
        values.put(COLUMN_RINGTONE, alarm.ringtone());
        values.put(COLUMN_VIBRATES, alarm.vibrates());
        values.put(COLUMN_ENABLED, alarm.isEnabled());
        values.put(COLUMN_SNOOZING_UNTIL_MILLIS, alarm.snoozingUntil());
        long id = getWritableDatabase().insert(TABLE_ALARMS, null, values);
        alarm.setId(id);

        values = new ContentValues();
        values.put(COLUMN_ALARM_ID, id);
        values.put(COLUMN_SUNDAY, alarm.isRecurring(DaysOfWeek.SUNDAY));
        values.put(COLUMN_MONDAY, alarm.isRecurring(DaysOfWeek.MONDAY));
        values.put(COLUMN_TUESDAY, alarm.isRecurring(DaysOfWeek.TUESDAY));
        values.put(COLUMN_WEDNESDAY, alarm.isRecurring(DaysOfWeek.WEDNESDAY));
        values.put(COLUMN_THURSDAY, alarm.isRecurring(DaysOfWeek.THURSDAY));
        values.put(COLUMN_FRIDAY, alarm.isRecurring(DaysOfWeek.FRIDAY));
        values.put(COLUMN_SATURDAY, alarm.isRecurring(DaysOfWeek.SATURDAY));
        getWritableDatabase().insert(TABLE_ALARM_RECURRING_DAYS, null, values);
        return id;
    }
}
