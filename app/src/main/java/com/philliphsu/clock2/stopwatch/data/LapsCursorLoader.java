package com.philliphsu.clock2.stopwatch.data;

import android.content.Context;

import com.philliphsu.clock2.data.SQLiteCursorLoader;
import com.philliphsu.clock2.stopwatch.Lap;

/**
 * Created by Phillip Hsu on 8/9/2016.
 */
public class LapsCursorLoader extends SQLiteCursorLoader<Lap, LapCursor> {
    public static final String ACTION_CHANGE_CONTENT
            = "com.philliphsu.clock2.stopwatch.data.action.CHANGE_CONTENT";

    public LapsCursorLoader(Context context) {
        super(context);
    }

    @Override
    protected LapCursor loadCursor() {
        return new LapsTableManager(getContext()).queryItems();
    }

    @Override
    protected String getOnContentChangeAction() {
        return ACTION_CHANGE_CONTENT;
    }
}
