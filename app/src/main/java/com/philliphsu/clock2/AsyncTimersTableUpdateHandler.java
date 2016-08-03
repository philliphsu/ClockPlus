package com.philliphsu.clock2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.philliphsu.clock2.alarms.ScrollHandler;
import com.philliphsu.clock2.model.TimersTableManager;
import com.philliphsu.clock2.timers.TimerNotificationService;
import com.philliphsu.clock2.timers.TimesUpActivity;

/**
 * Created by Phillip Hsu on 8/2/2016.
 */
public final class AsyncTimersTableUpdateHandler extends AsyncDatabaseTableUpdateHandler<Timer, TimersTableManager> {
    private static final String TAG = "TimersTableUpdater"; // TAG max 23 chars

    public AsyncTimersTableUpdateHandler(Context context, ScrollHandler scrollHandler) {
        super(context, scrollHandler);
    }

    @Override
    protected TimersTableManager getTableManager(Context context) {
        return new TimersTableManager(context);
    }

    @Override
    protected void onPostAsyncDelete(Integer result, Timer timer) {
        cancelAlarm(timer);
    }

    @Override
    protected void onPostAsyncInsert(Long result, Timer timer) {
        Log.d(TAG, "onPostAsyncInsert()");
        if (timer.isRunning()) {
            Log.d(TAG, "Scheduling alarm for timer launch");
            scheduleAlarm(timer);
        }
    }

    @Override
    protected void onPostAsyncUpdate(Long result, Timer timer) {
        if (timer.isRunning()) {
            // We don't need to cancel the previous alarm, because this one
            // will remove and replace it.
            scheduleAlarm(timer);
        } else {
            cancelAlarm(timer);
        }
    }

    // TODO: Consider changing to just a long id param
    private PendingIntent createTimesUpIntent(Timer timer) {
        Intent intent = new Intent(getContext(), TimesUpActivity.class);
//        intent.putExtra(TimesUpActivity.EXTRA_ITEM_ID, timer.getId());
        // There's no point to determining whether to retrieve a previous instance, because
        // we chose to ignore it since we had issues with NPEs. TODO: Perhaps these issues
        // were caused by you using the same reference variable for every Intent/PI that
        // needed to be recreated, and you reassigning the reference each time you were done with
        // one of them, which leaves the one before unreferenced and hence eligible for GC.
        return PendingIntent.getActivity(getContext(), timer.getIntId(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private void scheduleAlarm(Timer timer) {
        Log.d(TAG, String.format("now = %d, endTime = %d", SystemClock.elapsedRealtime(), timer.endTime()));
        AlarmManager am = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, timer.endTime(), createTimesUpIntent(timer));
        TimerNotificationService.showNotification(getContext(), timer.getId());
    }

    private void cancelAlarm(Timer timer) {
        // Cancel the alarm scheduled. If one was never scheduled, does nothing.
        AlarmManager am = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = createTimesUpIntent(timer);
        // Now can't be null
        am.cancel(pi);
        pi.cancel();
        TimerNotificationService.cancelNotification(getContext(), timer.getId());
    }
}
