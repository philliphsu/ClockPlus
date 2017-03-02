/*
 * Copyright 2017 Phillip Hsu
 *
 * This file is part of ClockPlus.
 *
 * ClockPlus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ClockPlus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ClockPlus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.philliphsu.clock2.alarms.data;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.philliphsu.clock2.data.AsyncDatabaseTableUpdateHandler;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.alarms.Alarm;
import com.philliphsu.clock2.list.ScrollHandler;
import com.philliphsu.clock2.alarms.misc.AlarmController;

/**
 * Created by Phillip Hsu on 7/1/2016.
 * TODO: Consider making an AsyncDatabaseChangeHandlerWithSnackbar abstract class
 */
public final class AsyncAlarmsTableUpdateHandler extends AsyncDatabaseTableUpdateHandler<Alarm, AlarmsTableManager> {
    private static final String TAG = "AsyncAlarmsTableUpdateHandler";

    private final View mSnackbarAnchor;
    private final AlarmController mAlarmController;

    /**
     * @param context the Context from which we get the application context
     * @param snackbarAnchor
     */
    public AsyncAlarmsTableUpdateHandler(Context context, View snackbarAnchor,
                                         ScrollHandler scrollHandler,
                                         AlarmController alarmController) {
        super(context, scrollHandler);
        mSnackbarAnchor = snackbarAnchor;
        mAlarmController = alarmController;
    }

    @Override
    protected AlarmsTableManager onCreateTableManager(Context context) {
        return new AlarmsTableManager(context);
    }

    @Override
    protected void onPostAsyncDelete(Integer result, final Alarm alarm) {
        mAlarmController.cancelAlarm(alarm, false, false);
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
