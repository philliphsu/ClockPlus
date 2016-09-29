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

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Phillip Hsu on 7/30/2016.
 */
public final class TimersTable {
    private TimersTable() {}

    // TODO: Consider defining index constants for each column,
    // and then removing all cursor getColumnIndex() calls.
    public static final String TABLE_TIMERS = "timers";

    // TODO: Consider implementing BaseColumns instead to get _id column.
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_HOUR = "hour";
    public static final String COLUMN_MINUTE = "minute";
    public static final String COLUMN_SECOND = "second";
    public static final String COLUMN_LABEL = "label";
    
    // http://stackoverflow.com/q/24183958/5055032
    // https://www.sqlite.org/lang_keywords.html
    // GROUP is a reserved keyword, so your CREATE TABLE statement
    // will not compile if you include this!
//    public static final String COLUMN_GROUP = "group";
    
    public static final String COLUMN_END_TIME = "end_time";
    public static final String COLUMN_PAUSE_TIME = "pause_time";
    public static final String COLUMN_DURATION = "duration";

    public static final String SORT_ORDER = COLUMN_HOUR + " ASC, "
                    + COLUMN_MINUTE + " ASC, "
                    + COLUMN_SECOND + " ASC, "
                    // All else equal, newer timers first
                    + COLUMN_ID + " DESC";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_TIMERS + " ("
                // https://sqlite.org/autoinc.html
                // If the AUTOINCREMENT keyword appears after INTEGER PRIMARY KEY, that changes the
                // automatic ROWID assignment algorithm to prevent the reuse of ROWIDs over the
                // lifetime of the database. In other words, the purpose of AUTOINCREMENT is to
                // prevent the reuse of ROWIDs from previously deleted rows.
                + COLUMN_ID + " INTEGER PRIMARY KEY, "
                + COLUMN_HOUR + " INTEGER NOT NULL, "
                + COLUMN_MINUTE + " INTEGER NOT NULL, "
                + COLUMN_SECOND + " INTEGER NOT NULL, "
                + COLUMN_LABEL + " TEXT NOT NULL, "
//                + COLUMN_GROUP + " TEXT NOT NULL, "
                + COLUMN_END_TIME + " INTEGER NOT NULL, "
                + COLUMN_PAUSE_TIME + " INTEGER NOT NULL, "
                + COLUMN_DURATION + " INTEGER NOT NULL);");
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIMERS);
        onCreate(db);
    }
}
