package com.philliphsu.clock2.model;

import android.content.Context;
import android.database.Cursor;

/**
 * Created by Phillip Hsu on 6/28/2016.
 */
public class AlarmsListCursorLoader extends SQLiteCursorLoader {

    public AlarmsListCursorLoader(Context context) {
        super(context);
    }

    @Override
    protected Cursor loadCursor() {
        return DatabaseManager.getInstance(getContext()).queryAlarms();
    }
}
