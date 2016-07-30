package com.philliphsu.clock2.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.philliphsu.clock2.Timer;

/**
 * Created by Phillip Hsu on 7/29/2016.
 */
@Deprecated
public class TimerDatabaseHelper extends BaseDatabaseHelper<Timer> {
    private static final String TAG = "TimerDatabaseHelper";
    private static final String DB_NAME = "timers.db";
    private static final int VERSION_1 = 1;

    private static final String TABLE_TIMERS = "timers";
    // TODO: Consider making these public, so we can move TimerCursor to its own top-level class.
    private static final String COLUMN_HOUR = "hour";
    private static final String COLUMN_MINUTE = "minute";
    private static final String COLUMN_SECOND = "second";
    private static final String COLUMN_LABEL = "label";

    // http://stackoverflow.com/q/24183958/5055032
    // https://www.sqlite.org/lang_keywords.html
    // GROUP is a reserved keyword, so your CREATE TABLE statement
    // will not compile if you include this!
//    private static final String COLUMN_GROUP = "group";

    private static final String COLUMN_END_TIME = "end_time";
    private static final String COLUMN_PAUSE_TIME = "pause_time";

    private static final String SORT_ORDER =
            COLUMN_HOUR + " ASC, "
            + COLUMN_MINUTE + " ASC, "
            + COLUMN_SECOND + " ASC, "
            // All else equal, newer timers first
            + COLUMN_ID + " DESC";

    public TimerDatabaseHelper(Context context) {
        super(context, DB_NAME, VERSION_1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_TIMERS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_HOUR + " INTEGER NOT NULL, "
                + COLUMN_MINUTE + " INTEGER NOT NULL, "
                + COLUMN_SECOND + " INTEGER NOT NULL, "
                + COLUMN_LABEL + " TEXT NOT NULL, "
//                + COLUMN_GROUP + " TEXT NOT NULL, "
                + COLUMN_END_TIME + " INTEGER NOT NULL, "
                + COLUMN_PAUSE_TIME + " INTEGER NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // I don't think we need to drop current tables unless you make structural changes
        // to the schema in the new version.
    }

    // =============================================================================================
    // Overridden methods can have a more specific return type, as long as that type
    // is a subtype of the original return type.

    @Override
    public TimerCursor queryItem(long id) {
        return wrapInTimerCursor(queryItem(id));
    }

    @Override
    public TimerCursor queryItems() {
        return wrapInTimerCursor(super.queryItems());
    }

    @Override
    protected TimerCursor queryItems(String where, String limit) {
        return wrapInTimerCursor(super.queryItems(where, limit));
    }
    // =============================================================================================

    @Override
    protected String getTableName() {
        return TABLE_TIMERS;
    }

    @Override
    protected ContentValues toContentValues(Timer timer) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_HOUR, timer.hour());
        cv.put(COLUMN_MINUTE, timer.minute());
        cv.put(COLUMN_SECOND, timer.second());
        cv.put(COLUMN_LABEL, timer.label());
//        cv.put(COLUMN_GROUP, timer.group());
        cv.put(COLUMN_END_TIME, timer.endTime());
        cv.put(COLUMN_PAUSE_TIME, timer.pauseTime());
        return cv;
    }

    @Override
    protected String getQuerySortOrder() {
        return SORT_ORDER;
    }

    private TimerCursor wrapInTimerCursor(Cursor c) {
        return new TimerCursor(c);
    }

}
