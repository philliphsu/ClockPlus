package com.philliphsu.clock2.model;

import android.content.Context;

import com.philliphsu.clock2.Timer;

/**
 * Created by Phillip Hsu on 7/29/2016.
 */
public class TimersListCursorLoader extends NewSQLiteCursorLoader<Timer, TimerDatabaseHelper.TimerCursor> {

    public TimersListCursorLoader(Context context) {
        super(context);
    }

    @Override
    protected TimerDatabaseHelper.TimerCursor loadCursor() {
        return new TimerDatabaseHelper(getContext()).queryItems();
    }
}
