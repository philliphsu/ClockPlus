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

package com.philliphsu.clock2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.philliphsu.clock2.alarms.data.AlarmsTable;
import com.philliphsu.clock2.stopwatch.data.LapsTable;
import com.philliphsu.clock2.timers.data.TimersTable;

/**
 * Created by Phillip Hsu on 7/30/2016.
 */
public class ClockAppDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "ClockAppDatabaseHelper";
    private static final String DB_NAME = "clock_app.db";
    private static final int VERSION_1 = 1;

    private static ClockAppDatabaseHelper sDatabaseHelper;

    public static ClockAppDatabaseHelper getInstance(Context context) {
        if (sDatabaseHelper == null)
            sDatabaseHelper = new ClockAppDatabaseHelper(context);
        return sDatabaseHelper;
    }

    /**
     * @param context the Context with which the application context will be retrieved
     */
    private ClockAppDatabaseHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, VERSION_1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        AlarmsTable.onCreate(db);
        TimersTable.onCreate(db);
        LapsTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        AlarmsTable.onUpgrade(db, oldVersion, newVersion);
        TimersTable.onUpgrade(db, oldVersion, newVersion);
        LapsTable.onUpgrade(db, oldVersion, newVersion);
    }
}
