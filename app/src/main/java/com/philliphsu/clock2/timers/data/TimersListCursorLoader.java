package com.philliphsu.clock2.timers.data;

import android.content.Context;

import com.philliphsu.clock2.Timer;
import com.philliphsu.clock2.model.SQLiteCursorLoader;

/**
 * Created by Phillip Hsu on 7/29/2016.
 */
public class TimersListCursorLoader extends SQLiteCursorLoader<Timer, TimerCursor> {
    public static final String ACTION_CHANGE_CONTENT
            = "com.philliphsu.clock2.timers.data.action.CHANGE_CONTENT";

    public TimersListCursorLoader(Context context) {
        super(context);
    }

    @Override
    protected TimerCursor loadCursor() {
        return new TimersTableManager(getContext()).queryItems();
    }

    @Override
    protected String getOnContentChangeAction() {
        return ACTION_CHANGE_CONTENT;
    }
}
