package com.philliphsu.clock2.stopwatch.data;

import android.database.Cursor;

import com.philliphsu.clock2.model.BaseItemCursor;
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
