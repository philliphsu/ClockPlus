package com.philliphsu.clock2.stopwatch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.philliphsu.clock2.model.DatabaseTableManager;

/**
 * Created by Phillip Hsu on 8/8/2016.
 */
public class LapsTableManager extends DatabaseTableManager<Lap> {

    public LapsTableManager(Context context) {
        super(context);
    }

    @Override
    protected String getQuerySortOrder() {
        return LapsTable.SORT_ORDER;
    }

    @Override
    public LapCursor queryItem(long id) {
        return wrapInLapCursor(super.queryItem(id));
    }

    @Override
    public LapCursor queryItems() {
        return wrapInLapCursor(super.queryItems());
    }

    @Override
    protected LapCursor queryItems(String where, String limit) {
        return wrapInLapCursor(super.queryItems(where, limit));
    }

//    public LapCursor queryTwoMostRecentLaps(long currentLapId, long previousLapId) {
//        String where = LapsTable.COLUMN_ID + " = " + currentLapId
//                + " OR " + LapsTable.COLUMN_ID + " = " + previousLapId;
//        LapCursor cursor = queryItems(where, "2");
//        cursor.moveToFirst();
//        return cursor;
//    }

    public LapCursor queryCurrentLap() {
        // The default sort order for the laps table is ID descending.
        // Normally, not specifying a where clause would return all rows.
        // Here, we limit the result to 1.
        // This has the effect of retrieving the row with the highest ID value.
        LapCursor c = queryItems(null, "1");
        c.moveToFirst();
        return c;
    }

    @Override
    protected String getTableName() {
        return LapsTable.TABLE_LAPS;
    }

    @Override
    protected ContentValues toContentValues(Lap lap) {
        ContentValues cv = new ContentValues();
//        cv.put(LapsTable.COLUMN_ID, lap.getId()); // NEVER SET THE ID, BECAUSE THE DATABASE SETS AN AUTOINCREMENTING PRIMARY KEY FOR YOU!
        cv.put(LapsTable.COLUMN_T1, lap.t1());
        cv.put(LapsTable.COLUMN_T2, lap.t2());
        cv.put(LapsTable.COLUMN_PAUSE_TIME, lap.pauseTime());
        cv.put(LapsTable.COLUMN_TOTAL_TIME_TEXT, lap.totalTimeText());
        return cv;
    }

    @Override
    protected String getOnContentChangeAction() {
        return LapsCursorLoader.ACTION_CHANGE_CONTENT;
    }

    private LapCursor wrapInLapCursor(Cursor c) {
        return new LapCursor(c);
    }
}
