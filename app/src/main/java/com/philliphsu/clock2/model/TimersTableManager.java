package com.philliphsu.clock2.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.philliphsu.clock2.Timer;

/**
 * Created by Phillip Hsu on 7/30/2016.
 */
public class TimersTableManager extends DatabaseTableManager<Timer> {
    public static final String TAG = "TimersTableManager";

    public TimersTableManager(Context context) {
        super(context);
    }

    @Override
    protected String getQuerySortOrder() {
        return TimersTable.SORT_ORDER;
    }

    @Override
    public TimerCursor queryItem(long id) {
        return wrapInTimerCursor(super.queryItem(id));
    }

    @Override
    public TimerCursor queryItems() {
        return wrapInTimerCursor(super.queryItems());
    }

    @Override
    protected TimerCursor queryItems(String where, String limit) {
        return wrapInTimerCursor(super.queryItems(where, limit));
    }

    @Override
    protected String getTableName() {
        return TimersTable.TABLE_TIMERS;
    }

    @Override
    protected ContentValues toContentValues(Timer timer) {
        ContentValues cv = new ContentValues();
        cv.put(TimersTable.COLUMN_HOUR, timer.hour());
        cv.put(TimersTable.COLUMN_MINUTE, timer.minute());
        cv.put(TimersTable.COLUMN_SECOND, timer.second());
        cv.put(TimersTable.COLUMN_LABEL, timer.label());
//        cv.put(TimersTable.COLUMN_GROUP, timer.group());
        Log.d(TAG, "endTime = " + timer.endTime() + ", pauseTime = " + timer.pauseTime());
        cv.put(TimersTable.COLUMN_END_TIME, timer.endTime());
        cv.put(TimersTable.COLUMN_PAUSE_TIME, timer.pauseTime());
        return cv;
    }

    @Override
    protected String getOnContentChangeAction() {
        return TimersListCursorLoader.ACTION_CHANGE_CONTENT;
    }

    private TimerCursor wrapInTimerCursor(Cursor c) {
        return new TimerCursor(c);
    }
}
