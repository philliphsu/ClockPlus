package com.philliphsu.clock2.model;

import android.content.Context;

import com.philliphsu.clock2.Timer;

/**
 * Created by Phillip Hsu on 8/3/2016.
 */
public class TimerLoader extends DataLoader<Timer> {

    private long mTimerId;

    // TODO: Consider writing a super ctor that has the id param, so
    // subclasses don't need to write their own.
    public TimerLoader(Context context, long timerId) {
        super(context);
        mTimerId = timerId;
    }

    @Override
    public Timer loadInBackground() {
        return new TimersTableManager(getContext()).queryItem(mTimerId).getItem();
    }
}
