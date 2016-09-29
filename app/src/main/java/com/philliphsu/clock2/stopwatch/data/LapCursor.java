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

package com.philliphsu.clock2.stopwatch.data;

import android.database.Cursor;

import com.philliphsu.clock2.data.BaseItemCursor;
import com.philliphsu.clock2.stopwatch.Lap;

/**
 * Created by Phillip Hsu on 8/8/2016.
 */
public class LapCursor extends BaseItemCursor<Lap> {

    public LapCursor(Cursor cursor) {
        super(cursor);
    }

    @Override
    public Lap getItem() {
        Lap lap = new Lap();
        lap.setId(getLong(getColumnIndexOrThrow(LapsTable.COLUMN_ID)));
        lap.setT1(getLong(getColumnIndexOrThrow(LapsTable.COLUMN_T1)));
        lap.setT2(getLong(getColumnIndexOrThrow(LapsTable.COLUMN_T2)));
        lap.setPauseTime(getLong(getColumnIndexOrThrow(LapsTable.COLUMN_PAUSE_TIME)));
        lap.setTotalTimeText(getString(getColumnIndexOrThrow(LapsTable.COLUMN_TOTAL_TIME_TEXT)));
        return lap;
    }
}
