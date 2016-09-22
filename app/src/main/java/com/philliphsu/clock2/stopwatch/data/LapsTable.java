package com.philliphsu.clock2.stopwatch.data;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Phillip Hsu on 8/8/2016.
 */
public final class LapsTable {
    private LapsTable() {}

    public static final String TABLE_LAPS = "laps";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_T1 = "elapsed";
    public static final String COLUMN_T2 = "total";
    public static final String COLUMN_PAUSE_TIME = "pause_time";
    public static final String COLUMN_TOTAL_TIME_TEXT = "total_time_text";

    public static final String SORT_ORDER = COLUMN_ID + " DESC";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_LAPS + " ("
                // https://sqlite.org/autoinc.html
                // If the AUTOINCREMENT keyword appears after INTEGER PRIMARY KEY, that changes the
                // automatic ROWID assignment algorithm to prevent the reuse of ROWIDs over the
                // lifetime of the database. In other words, the purpose of AUTOINCREMENT is to
                // prevent the reuse of ROWIDs from previously deleted rows.
                // If we ever clear the laps of a stopwatch timing, we want the next timing's laps
                // to begin again from id = 1. Using AUTOINCREMENT prevents that from happening;
                // the laps will begin from the last ID used plus one.
                + COLUMN_ID + " INTEGER PRIMARY KEY, "
                + COLUMN_T1 + " INTEGER NOT NULL, "
                + COLUMN_T2 + " INTEGER NOT NULL, "
                + COLUMN_PAUSE_TIME + " INTEGER NOT NULL, "
                + COLUMN_TOTAL_TIME_TEXT + " TEXT);");
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LAPS);
        onCreate(db);
    }
}
