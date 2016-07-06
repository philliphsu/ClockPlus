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
                // Prepare to scroll to the newly added alarm
                mScrollHandler.setScrollToStableId(aLong);
            }
        }.execute();
    }

    public void asyncUpdateAlarm(final Alarm oldAlarm, final Alarm newAlarm) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                return DatabaseManager.getInstance(mContext).updateAlarm(oldAlarm.id(), newAlarm);
            }

            @Override
            protected void onPostExecute(Integer integer) {
                // TODO: Snackbar/Toast here? If so, remove the code in AlarmUtils.scheduleAlarm() that does it.
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
                                    DatabaseManager.getInstance(mContext).insertAlarm(alarm);
                                    if (alarm.isEnabled()) {
                                        AlarmUtils.scheduleAlarm(mContext, alarm, true);
                                    }
                                }
                            }).show();
                }
            }
        }.execute();
    }
}
