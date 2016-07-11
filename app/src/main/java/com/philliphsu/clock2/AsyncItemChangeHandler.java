package com.philliphsu.clock2;

import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.philliphsu.clock2.alarms.ScrollHandler;
import com.philliphsu.clock2.model.DatabaseManager;
import com.philliphsu.clock2.util.AlarmController;

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
    private final AlarmController mAlarmController;

    /**
     * @param context the Context from which we get the application context
     * @param snackbarAnchor
     */
    public AsyncItemChangeHandler(Context context, View snackbarAnchor,
                                  ScrollHandler scrollHandler,
                                  AlarmController alarmController) {
        mContext = context.getApplicationContext(); // to prevent memory leaks
        mSnackbarAnchor = snackbarAnchor;
        mScrollHandler = scrollHandler;
        mAlarmController = alarmController;
    }

    public void asyncAddAlarm(final Alarm alarm) {
        new InsertAlarmAsyncTask(alarm).execute();
    }

    /**
     * We only need one Alarm param because we called newAlarm.setId(oldAlarm.id())
     * when we were in the edit activity.
     * TODO: Consider changing the signature of updateAlarm() in DatabaseManager and
     * AlarmDatabaseHelper to only require one Alarm param.
     */
    public void asyncUpdateAlarm(final Alarm newAlarm) {
        new UpdateAlarmAsyncTask(newAlarm).execute();
    }

    public void asyncRemoveAlarm(final Alarm alarm) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                return DatabaseManager.getInstance(mContext).deleteAlarm(alarm);
            }

            @Override
            protected void onPostExecute(Integer integer) {
                mAlarmController.cancelAlarm(alarm, false);
                if (mSnackbarAnchor != null) {
                    // TODO: Consider adding delay to allow the alarm item animation
                    // to finish first before we show the snackbar. Inbox app does this.
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

    ////////////////////////////////////////////////////////////
    // Insert and update AsyncTasks
    ////////////////////////////////////////////////////////////

    /**
     * Created because the code in insert and update AsyncTasks are exactly the same.
     */
    private abstract class BaseAsyncTask extends AsyncTask<Void, Void, Long> {
        private final Alarm mAlarm;

        BaseAsyncTask(Alarm alarm) {
            mAlarm = alarm;
        }

        @Override
        protected void onPostExecute(Long result) {
            // TODO: Consider adding delay to allow the alarm item animation
            // to finish first before we show the snackbar. Inbox app does this.
            mAlarmController.scheduleAlarm(mAlarm, true);
            if (mScrollHandler != null) {
                // Prepare to scroll to this alarm
                mScrollHandler.setScrollToStableId(result);
            }
        }

        final Long insertAlarm() {
            return DatabaseManager.getInstance(mContext).insertAlarm(mAlarm);
        }

        final Long updateAlarm() {
            long id = mAlarm.id();
            DatabaseManager.getInstance(mContext).updateAlarm(id, mAlarm);
            return id;
        }
    }

    private class InsertAlarmAsyncTask extends BaseAsyncTask {
        InsertAlarmAsyncTask(Alarm alarm) {
            super(alarm);
        }

        @Override
        protected Long doInBackground(Void... params) {
            return insertAlarm();
        }
    }

    private class UpdateAlarmAsyncTask extends BaseAsyncTask {
        UpdateAlarmAsyncTask(Alarm alarm) {
            super(alarm);
        }

        @Override
        protected Long doInBackground(Void... params) {
            return updateAlarm();
        }
    }
}
