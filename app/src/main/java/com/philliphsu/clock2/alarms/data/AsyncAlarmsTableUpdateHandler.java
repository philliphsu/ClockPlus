/*
 * Copyright (C) 2016 Phillip Hsu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
