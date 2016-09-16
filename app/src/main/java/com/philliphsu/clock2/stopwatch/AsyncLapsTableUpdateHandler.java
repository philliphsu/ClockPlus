package com.philliphsu.clock2.stopwatch;

import android.content.Context;
import android.content.Intent;

import com.philliphsu.clock2.AsyncDatabaseTableUpdateHandler;
import com.philliphsu.clock2.alarms.ScrollHandler;

/**
 * Created by Phillip Hsu on 8/9/2016.
 */
public class AsyncLapsTableUpdateHandler extends AsyncDatabaseTableUpdateHandler<Lap, LapsTableManager> {

    public AsyncLapsTableUpdateHandler(Context context, ScrollHandler scrollHandler) {
        super(context, scrollHandler);
    }

    @Override
    protected LapsTableManager onCreateTableManager(Context context) {
        return new LapsTableManager(context);
    }

    @Override
    protected void onPostAsyncInsert(Long result, Lap item) {
        if (result > 1) {
            // Update the notification's title with this lap number
            Intent intent = new Intent(getContext(), StopwatchNotificationService.class)
                    .setAction(StopwatchNotificationService.ACTION_UPDATE_LAP_TITLE)
                    .putExtra(StopwatchNotificationService.EXTRA_LAP_NUMBER, result.intValue());
            getContext().startService(intent);
        }
    }

    // ===================== DO NOT IMPLEMENT =========================

    @Override
    protected void onPostAsyncDelete(Integer result, Lap item) {
        // Leave blank.
    }

    @Override
    protected void onPostAsyncUpdate(Long result, Lap item) {
        // Leave blank.
    }
}
