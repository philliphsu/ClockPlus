/*
 * Copyright 2017 Phillip Hsu
 *
 * This file is part of ClockPlus.
 *
 * ClockPlus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ClockPlus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ClockPlus.  If not, see <http://www.gnu.org/licenses/>.
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
