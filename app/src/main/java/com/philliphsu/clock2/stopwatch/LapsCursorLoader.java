package com.philliphsu.clock2.stopwatch;

import android.content.Context;

import com.philliphsu.clock2.model.SQLiteCursorLoader;

/**
 * Created by Phillip Hsu on 8/9/2016.
 */
public class LapsCursorLoader extends SQLiteCursorLoader<Lap, LapCursor> {
    public static final String ACTION_CHANGE_CONTENT
            // TODO: Correct package prefix
            = "com.philliphsu.clock2.model.LapsCursorLoader.action.CHANGE_CONTENT";

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
