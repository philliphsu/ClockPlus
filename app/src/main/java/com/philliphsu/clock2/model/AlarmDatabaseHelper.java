package com.philliphsu.clock2.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.util.LocalBroadcastHelper;

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
    private static final String TAG = "AlarmDatabaseHelper";
    private static final String DB_NAME = "alarms.db";
    private static final int VERSION_1 = 1;

    // TODO: Consider creating an inner class that implements BaseColumns
    // and defines all the columns.
    // TODO: Consider defining index constants for each column,
    // and then removing all cursor getColumnIndex() calls.
    // TODO: Consider making these public, so callers can customize their
    // WHERE queries.
    private static final String TABLE_ALARMS = "alarms";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_HOUR = "hour";
    private static final String COLUMN_MINUTES = "minutes";
    private static final String COLUMN_LABEL = "label";
    private static final String COLUMN_RINGTONE = "ringtone";
    private static final String COLUMN_VIBRATES = "vibrates";
    private static final String COLUMN_ENABLED = "enabled";

    // TODO: Delete this column, becuase new sort order does not consider it
    @Deprecated
    private static final String COLUMN_RING_TIME_MILLIS = "ring_time_millis";

    private static final String COLUMN_SNOOZING_UNTIL_MILLIS = "snoozing_until_millis";
    private static final String COLUMN_SUNDAY = "sunday";
    private static final String COLUMN_MONDAY = "monday";
    private static final String COLUMN_TUESDAY = "tuesday";
    private static final String COLUMN_WEDNESDAY = "wednesday";
    private static final String COLUMN_THURSDAY = "thursday";
    private static final String COLUMN_FRIDAY = "friday";
    private static final String COLUMN_SATURDAY = "saturday";
    private static final String COLUMN_IGNORE_UPCOMING_RING_TIME = "ignore_upcoming_ring_time";

    // https://www.sqlite.org/lang_select.html#orderby
    // Rows are first sorted based on the results of evaluating the left-most expression in the
    // ORDER BY list, then ties are broken by evaluating the second left-most expression and so on.
    // The order in which two rows for which all ORDER BY expressions evaluate to equal values are
    // returned is undefined. Each ORDER BY expression may be optionally followed by one of the keywords
    // ASC (smaller values are returned first) or DESC (larger values are returned first). If neither
    // ASC or DESC are specified, rows are sorted in ascending (smaller values first) order by default.

    // First sort by ring time in ascending order (smaller values first),
    // then break ties by sorting by id in ascending order.
    @Deprecated
    private static final String SORT_ORDER =
            COLUMN_RING_TIME_MILLIS + " ASC, " + COLUMN_ID + " ASC";

    private static final String NEW_SORT_ORDER = COLUMN_HOUR + " ASC, "
            + COLUMN_MINUTES + " ASC, "
            // TOneverDO: Sort COLUMN_ENABLED or else alarms could be reordered
            // if you toggle them on/off, which looks confusing.
            // TODO: Figure out how to get the order to be:
            // No recurring days ->
            // Recurring earlier in user's weekday order ->
            // Recurring everyday
            // As written now, this is incorrect! For one, it assumes
            // the standard week order (starting on Sunday).
            // DESC gives us (Sunday -> Saturday -> No recurring days),
            // ASC gives us the reverse (No recurring days -> Saturday -> Sunday).
            // TODO: If assuming standard week order, try ASC for all days but
            // write COLUMN_SATURDAY first, then COLUMN_FRIDAY, ... , COLUMN_SUNDAY.
            // Check if that gives us (No recurring days -> Sunday -> Saturday).
//            + COLUMN_SUNDAY + " DESC, "
//            + COLUMN_MONDAY + " DESC, "
//            + COLUMN_TUESDAY + " DESC, "
//            + COLUMN_WEDNESDAY + " DESC, "
//            + COLUMN_THURSDAY + " DESC, "
//            + COLUMN_FRIDAY + " DESC, "
//            + COLUMN_SATURDAY + " DESC, "
            // All else equal, newer alarms first
            + COLUMN_ID + " DESC"; // TODO: If duplicate alarm times disallowed, delete this

    private final Context mAppContext;

    public AlarmDatabaseHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, VERSION_1);
        mAppContext = context.getApplicationContext();
        // TODO: Here is where you could compute the sort expression
        // for the recurring days order, based on the user's defined
        // weekday order. For example, if we read the first day of
        // the week as Sunday, then we build a String called RECURRENCE_ORDER:
        //   RECURRENCE_ORDER =
        //     COLUMN_SATURDAY + " ASC, "
        //     + ...
        //     + COLUMN_SUNDAY + " ASC";
        // Note how the weekday order is reversed when
        // we refer to the columns. We should also include
        // ordering by id as the last piece of this string:
        //    + COLUMN_ID + " DESC";
        // and remove that piece from the NEW_SORT_ORDER
        // constant. This is so we can later concatenate
        // NEW_SORT_ORDER and RECURRENCE_ORDER but maintain
        // the original order of the sort expressions.
        // We should also rename that constant
        // to BASE_SORT_ORDER. Last, in the query() methods,
        // we can pass in
        //   BASE_SORT_ORDER + RECURRENCE_ORDER
        // to its orderBy parameter.
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // https://www.sqlite.org/datatype3.html
        // INTEGER data type is stored in 1, 2, 3, 4, 6, or 8 bytes depending on the magnitude
        // of the value. As soon as INTEGER values are read off of disk and into memory for processing,
        // they are converted to the most general datatype (8-byte signed integer).
        // 8 byte == 64 bits so this means they are read as longs...?
        db.execSQL("CREATE TABLE " + TABLE_ALARMS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_HOUR + " INTEGER NOT NULL, "
                + COLUMN_MINUTES + " INTEGER NOT NULL, "
                + COLUMN_LABEL + " TEXT, "
                + COLUMN_RINGTONE + " TEXT NOT NULL, "
                + COLUMN_VIBRATES + " INTEGER NOT NULL, "
                + COLUMN_ENABLED + " INTEGER NOT NULL, "
                + COLUMN_RING_TIME_MILLIS + " INTEGER NOT NULL, "
                + COLUMN_SNOOZING_UNTIL_MILLIS + " INTEGER, "
                + COLUMN_SUNDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_MONDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_TUESDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_WEDNESDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_THURSDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_FRIDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_SATURDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_IGNORE_UPCOMING_RING_TIME + " INTEGER NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // I don't think we need to drop current tables unless you make structural changes
        // to the schema in the new version.
    }

    public long insertAlarm(Alarm alarm) {
        long id = getWritableDatabase().insert(TABLE_ALARMS,
                null, toContentValues(alarm));
        alarm.setId(id);
        notifyContentChanged();
        return id;
    }

    /**
     * @deprecated Use {@link #updateAlarm(long, Alarm)} instead
     */
    @Deprecated
    public int updateAlarm(Alarm oldAlarm, Alarm newAlarm) {
        newAlarm.setId(oldAlarm.id());
        SQLiteDatabase db = getWritableDatabase();
        int rowsUpdated = db.update(TABLE_ALARMS,
                toContentValues(newAlarm),
                COLUMN_ID + " = " + newAlarm.id(),
                null);
        notifyContentChanged();
        return rowsUpdated;
    }

    public int updateAlarm(long id, Alarm newAlarm) {
        newAlarm.setId(id);
        SQLiteDatabase db = getWritableDatabase();
        int rowsUpdated = db.update(TABLE_ALARMS,
                toContentValues(newAlarm),
                COLUMN_ID + " = " + id,
                null);
        notifyContentChanged();
        return rowsUpdated;
    }

    public int deleteAlarm(Alarm alarm) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_ALARMS,
                COLUMN_ID + " = " + alarm.id(),
                null);
        notifyContentChanged();
        return rowsDeleted;
    }

    public AlarmCursor queryAlarm(long id) {
        Cursor c = getReadableDatabase().query(TABLE_ALARMS,
                null, // All columns
                COLUMN_ID + " = " + id, // Selection for this alarm id
                null, // selection args, none b/c id already specified in selection
                null, // group by
                null, // order/sort by
                null, // having
                "1"); // limit 1 row
        return new AlarmCursor(c);
    }

    public AlarmCursor queryAlarms() {
        // Select all rows and columns
        return queryAlarms(null);
    }

    public AlarmCursor queryEnabledAlarms() {
        return queryAlarms(COLUMN_ENABLED + " = 1");
    }

    private AlarmCursor queryAlarms(String where) {
        Cursor c = getReadableDatabase().query(TABLE_ALARMS,
                null, where, null, null, null, NEW_SORT_ORDER);
        return new AlarmCursor(c);
    }

    private ContentValues toContentValues(Alarm alarm) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_HOUR, alarm.hour());
        values.put(COLUMN_MINUTES, alarm.minutes());
        values.put(COLUMN_LABEL, alarm.label());
        values.put(COLUMN_RINGTONE, alarm.ringtone());
        values.put(COLUMN_VIBRATES, alarm.vibrates());
        values.put(COLUMN_ENABLED, alarm.isEnabled());
        values.put(COLUMN_RING_TIME_MILLIS, alarm.ringsAt());
        values.put(COLUMN_SNOOZING_UNTIL_MILLIS, alarm.snoozingUntil());
        values.put(COLUMN_SUNDAY, alarm.isRecurring(SUNDAY));
        values.put(COLUMN_MONDAY, alarm.isRecurring(MONDAY));
        values.put(COLUMN_TUESDAY, alarm.isRecurring(TUESDAY));
        values.put(COLUMN_WEDNESDAY, alarm.isRecurring(WEDNESDAY));
        values.put(COLUMN_THURSDAY, alarm.isRecurring(THURSDAY));
        values.put(COLUMN_FRIDAY, alarm.isRecurring(FRIDAY));
        values.put(COLUMN_SATURDAY, alarm.isRecurring(SATURDAY));
        values.put(COLUMN_IGNORE_UPCOMING_RING_TIME, alarm.isIgnoringUpcomingRingTime());
        return values;
    }

    private void notifyContentChanged() {
        Log.d(TAG, "notifyContentChanged()");
        LocalBroadcastHelper.sendBroadcast(mAppContext,
                SQLiteCursorLoader.ACTION_CHANGE_CONTENT);
    }

    // An alternative method to creating an Alarm from a cursor is to
    // make an Alarm constructor that takes an Cursor param. However,
    // this method has the advantage of keeping all the constants
    // contained within this file. Another advantage is the contents of
    // the Alarm class remain as pure Java, which can facilitate unit testing
    // because it has no dependence on Cursor, which is part of the Android
    // SDK.
    public static class AlarmCursor extends CursorWrapper {
        
        public AlarmCursor(Cursor c) {
            super(c);
        }

        /**
         * @return an Alarm instance configured for the current row,
         * or null if the current row is invalid
         */
        public Alarm getAlarm() {
            if (isBeforeFirst() || isAfterLast())
                return null;
            // TODO: Use getColumnIndexOrThrow()
            Alarm alarm = Alarm.builder()
                    .hour(getInt(getColumnIndex(COLUMN_HOUR)))
                    .minutes(getInt(getColumnIndex(COLUMN_MINUTES)))
                    .vibrates(isTrue(COLUMN_VIBRATES))
                    .ringtone(getString(getColumnIndex(COLUMN_RINGTONE)))
                    .label(getString(getColumnIndex(COLUMN_LABEL)))
                    .build();
            alarm.setId(getLong(getColumnIndex(COLUMN_ID)));
            alarm.setEnabled(isTrue(COLUMN_ENABLED));
            alarm.setSnoozing(getLong(getColumnIndex(COLUMN_SNOOZING_UNTIL_MILLIS)));
            alarm.setRecurring(SUNDAY, isTrue(COLUMN_SUNDAY));
            alarm.setRecurring(MONDAY, isTrue(COLUMN_MONDAY));
            alarm.setRecurring(TUESDAY, isTrue(COLUMN_TUESDAY));
            alarm.setRecurring(WEDNESDAY, isTrue(COLUMN_WEDNESDAY));
            alarm.setRecurring(THURSDAY, isTrue(COLUMN_THURSDAY));
            alarm.setRecurring(FRIDAY, isTrue(COLUMN_FRIDAY));
            alarm.setRecurring(SATURDAY, isTrue(COLUMN_SATURDAY));
            alarm.ignoreUpcomingRingTime(isTrue(COLUMN_IGNORE_UPCOMING_RING_TIME));
            return alarm;
        }

        public long getId() {
            if (isBeforeFirst() || isAfterLast()) {
                Log.e(TAG, "Failed to retrieve id, cursor out of range");
                return -1;
            }
            return getLong(getColumnIndexOrThrow(COLUMN_ID));
        }
        
        private boolean isTrue(String columnName) {
            return getInt(getColumnIndex(columnName)) == 1;
        }
    }
}
