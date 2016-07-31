package com.philliphsu.clock2;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.philliphsu.clock2.alarms.ScrollHandler;
import com.philliphsu.clock2.model.AlarmsTableManager;
import com.philliphsu.clock2.util.AlarmController;

/**
 * Created by Phillip Hsu on 7/1/2016.
 *
 * TODO: Rename to AsyncAlarmChangeHandler
 * TODO: Consider making an AsyncDatabaseChangeHandlerWithSnackbar abstract class
 */
public final class AsyncItemChangeHandler extends AsyncDatabaseChangeHandler<Alarm, AlarmsTableManager> {
    private static final String TAG = "AsyncItemChangeHandler";

    private final View mSnackbarAnchor;
    private final AlarmController mAlarmController;

    /**
     * @param context the Context from which we get the application context
     * @param snackbarAnchor
     */
    public AsyncItemChangeHandler(Context context, View snackbarAnchor,
                                  ScrollHandler scrollHandler,
                                  AlarmController alarmController) {
        super(context, scrollHandler);
        mSnackbarAnchor = snackbarAnchor;
        mAlarmController = alarmController;
    }

    @Override
    protected AlarmsTableManager getTableManager(Context context) {
        return new AlarmsTableManager(context);
    }

    @Override
    protected void onPostAsyncDelete(Integer result, final Alarm alarm) {
        mAlarmController.cancelAlarm(alarm, false);
        if (mSnackbarAnchor != null) {
            // TODO: Consider adding delay to allow the alarm item animation
            // to finish first before we show the snackbar. Inbox app does this.
            String message = getContext().getString(R.string.snackbar_item_deleted,
                    getContext().getString(R.string.alarm));
            Snackbar.make(mSnackbarAnchor, message, Snackbar.LENGTH_LONG)
                    .setAction(R.string.snackbar_undo_item_deleted, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            asyncInsert(alarm);
                        }
                    }).show();
        }
    }

    @Override
    protected void onPostAsyncInsert(Long result, Alarm alarm) {
        mAlarmController.scheduleAlarm(alarm, true);
    }

    @Override
    protected void onPostAsyncUpdate(Long result, Alarm alarm) {
        mAlarmController.scheduleAlarm(alarm, true);
    }
}
