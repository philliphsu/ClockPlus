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

import android.database.Cursor;

import com.philliphsu.clock2.timers.Timer;
import com.philliphsu.clock2.data.BaseItemCursor;

/**
 * Created by Phillip Hsu on 7/30/2016.
 */
public class TimerCursor extends BaseItemCursor<Timer> {

    public TimerCursor(Cursor cursor) {
        super(cursor);
    }

    @Override
    public Timer getItem() {
        if (isBeforeFirst() || isAfterLast())
            return null;
        int hour = getInt(getColumnIndexOrThrow(TimersTable.COLUMN_HOUR));
        int minute = getInt(getColumnIndexOrThrow(TimersTable.COLUMN_MINUTE));
        int second = getInt(getColumnIndexOrThrow(TimersTable.COLUMN_SECOND));
        String label = getString(getColumnIndexOrThrow(TimersTable.COLUMN_LABEL));
//            String group = getString(getColumnIndexOrThrow(COLUMN_GROUP));
        Timer t = Timer.create(hour, minute, second, ""/*group*/, label);
        t.setId(getLong(getColumnIndexOrThrow(TimersTable.COLUMN_ID)));
        t.setEndTime(getLong(getColumnIndexOrThrow(TimersTable.COLUMN_END_TIME)));
        t.setPauseTime(getLong(getColumnIndexOrThrow(TimersTable.COLUMN_PAUSE_TIME)));
        t.setDuration(getLong(getColumnIndexOrThrow(TimersTable.COLUMN_DURATION)));
        return t;
    }
}
