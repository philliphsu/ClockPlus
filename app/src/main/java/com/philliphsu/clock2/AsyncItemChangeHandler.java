package com.philliphsu.clock2;

import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.philliphsu.clock2.alarms.ScrollHandler;
import com.philliphsu.clock2.model.DatabaseManager;
import com.philliphsu.clock2.util.AlarmUtils;

/**
 * Created by Phillip Hsu on 7/1/2016.
 *
 * TODO: Generify this class for any item.
 */
public final class AsyncItemChangeHandler {
    private static final String TAG = "AsyncItemChangeHandler";

    private final Context mContext;
    private final View mSnackbarAnchor;
    private final ScrollHandler mScrollHandler;

    /**
     * @param snackbarAnchor an optional anchor for a Snackbar to anchor to
     * @param scrollHandler
     */
    public AsyncItemChangeHandler(Context context, View snackbarAnchor, ScrollHandler scrollHandler) {
        mContext = context.getApplicationContext(); // to prevent memory leaks
        mSnackbarAnchor = snackbarAnchor;
        mScrollHandler = scrollHandler;
    }

    public void asyncAddAlarm(final Alarm alarm) {
        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... params) {
                return DatabaseManager.getInstance(mContext).insertAlarm(alarm);
            }

            @Override
            protected void onPostExecute(Long aLong) {
                // TODO: Snackbar/Toast here? If so, remove the code in AlarmUtils.scheduleAlarm() that does it.
                // Then, consider scheduling the alarm in the background.
                AlarmUtils.scheduleAlarm(mContext, alarm, true);
                if (mScrollHandler != null) {
                    // Prepare to scroll to the newly added alarm
                    mScrollHandler.setScrollToStableId(aLong);
                }
            }
        }.execute();
    }

    /**
     * We only need one Alarm param because we called newAlarm.setId(oldAlarm.id())
     * when we were in the edit activity.
     * TODO: Consider changing the signature of updateAlarm() in DatabaseManager and
     * AlarmDatabaseHelper to only require one Alarm param.
     * TODO: The AsyncTask employed here is very similar to the one employed in
     * asyncAddAlarm(). Figure out a way to refactor the code in common. Possible
     * starts are to:
     *  * Change the Result type to Long, and then the onPostExecute() can be
     *   expressed the same between the two methods.
     *  * Similar to what you did in AlarmsFragment with the static
     *   inner Runnables, write a static inner abstract class that extends
     *   AsyncTask that takes in an Alarm; leave doInBackground() unimplemented
     *   in this base class. Then, define methods in this base class that subclasses
     *   can call to do their desired CRUD task in their doInBackground().
     */
    public void asyncUpdateAlarm(final Alarm newAlarm) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                return DatabaseManager.getInstance(mContext).updateAlarm(newAlarm.id(), newAlarm);
            }

            @Override
            protected void onPostExecute(Integer integer) {
                // TODO: Snackbar/Toast here? If so, remove the code in AlarmUtils.scheduleAlarm() that does it.
                AlarmUtils.scheduleAlarm(mContext, newAlarm, true);
                if (mScrollHandler != null) {
                    // The new alarm could have a different sort order from the old alarm.
                    // TODO: Sometimes this won't scrolls to the new alarm if the old alarm is
                    // towards the bottom and the new alarm is ordered towards the top. This
                    // may have something to do with us breaking the stable id guarantee?
                    mScrollHandler.setScrollToStableId(newAlarm.id());
                }
            }
        }.execute();
    }

    public void asyncRemoveAlarm(final Alarm alarm) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                return DatabaseManager.getInstance(mContext).deleteAlarm(alarm);
            }

            @Override
            protected void onPostExecute(Integer integer) {
                if (mSnackbarAnchor != null) {
                    String message = mContext.getString(R.string.snackbar_item_deleted,
                            mContext.getString(R.string.alarm));
                    Snackbar.make(mSnackbarAnchor, message, Snackbar.LENGTH_LONG)
                            .setAction(R.string.snackbar_undo_item_deleted, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    asyncAddAlarm(alarm);
                                }
                            }).show();
                }
            }
        }.execute();
    }
}
