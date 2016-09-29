/*
 * Copyright (C) 2016 Phillip Hsu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.philliphsu.clock2.timers.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.SystemClock;
import android.util.Log;

import com.philliphsu.clock2.timers.Timer;
import com.philliphsu.clock2.data.DatabaseTableManager;

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

    public TimerCursor queryStartedTimers() {
        String where = TimersTable.COLUMN_END_TIME + " > " + SystemClock.elapsedRealtime()
                + " OR " + TimersTable.COLUMN_PAUSE_TIME + " > 0";
        return queryItems(where, null);
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
        Log.d(TAG, "toContentValues() label = " + timer.label());
        cv.put(TimersTable.COLUMN_LABEL, timer.label());
//        cv.put(TimersTable.COLUMN_GROUP, timer.group());
        Log.d(TAG, "endTime = " + timer.endTime() + ", pauseTime = " + timer.pauseTime());
        cv.put(TimersTable.COLUMN_END_TIME, timer.endTime());
        cv.put(TimersTable.COLUMN_PAUSE_TIME, timer.pauseTime());
        cv.put(TimersTable.COLUMN_DURATION, timer.duration());
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
