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

import android.database.Cursor;
import android.database.CursorWrapper;
import android.util.Log;

/**
 * Created by Phillip Hsu on 7/29/2016.
 */
public abstract class BaseItemCursor<T extends ObjectWithId> extends CursorWrapper {
    private static final String TAG = "BaseItemCursor";

    public BaseItemCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * @return an item instance configured for the current row,
     * or null if the current row is invalid
     */
    public abstract T getItem();

    public long getId() {
        if (isBeforeFirst() || isAfterLast()) {
            Log.e(TAG, "Failed to retrieve id, cursor out of range");
            return -1;
        }
        return getLong(getColumnIndexOrThrow("_id")); // TODO: Refer to a constant instead of a hardcoded value
    }

    /**
     * Helper method to determine boolean-valued columns.
     * SQLite does not support a BOOLEAN data type.
     */
    protected boolean isTrue(String columnName) {
        return getInt(getColumnIndexOrThrow(columnName)) == 1;
    }
}
