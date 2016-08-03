package com.philliphsu.clock2;

import android.content.Context;

import com.philliphsu.clock2.alarms.ScrollHandler;
import com.philliphsu.clock2.model.TimersTableManager;

/**
 * Created by Phillip Hsu on 8/2/2016.
 */
public final class AsyncTimersTableUpdateHandler extends AsyncDatabaseTableUpdateHandler<Timer, TimersTableManager> {

    public AsyncTimersTableUpdateHandler(Context context, ScrollHandler scrollHandler) {
        super(context, scrollHandler);
    }

    @Override
    protected TimersTableManager getTableManager(Context context) {
        return new TimersTableManager(context);
    }

    @Override
    protected void onPostAsyncDelete(Integer result, Timer timer) {
        // TODO: Cancel the alarm scheduled for this timer
    }

    @Override
    protected void onPostAsyncInsert(Long result, Timer timer) {
        // TODO: if running, schedule alarm
    }

    @Override
    protected void onPostAsyncUpdate(Long result, Timer timer) {
        // TODO: cancel and reschedule
    }
}
