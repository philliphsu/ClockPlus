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

package com.philliphsu.clock2.timers.ui;

import android.view.ViewGroup;

import com.philliphsu.clock2.timers.data.AsyncTimersTableUpdateHandler;
import com.philliphsu.clock2.list.BaseCursorAdapter;
import com.philliphsu.clock2.list.OnListItemInteractionListener;
import com.philliphsu.clock2.timers.Timer;
import com.philliphsu.clock2.timers.data.TimerCursor;

/**
 * Created by Phillip Hsu on 7/29/2016.
 */
public class TimersCursorAdapter extends BaseCursorAdapter<Timer, TimerViewHolder, TimerCursor> {

    private final AsyncTimersTableUpdateHandler mAsyncTimersTableUpdateHandler;

    public TimersCursorAdapter(OnListItemInteractionListener<Timer> listener,
                               AsyncTimersTableUpdateHandler asyncTimersTableUpdateHandler) {
        super(listener);
        mAsyncTimersTableUpdateHandler = asyncTimersTableUpdateHandler;
    }

    @Override
    protected TimerViewHolder onCreateViewHolder(ViewGroup parent, OnListItemInteractionListener<Timer> listener, int viewType) {
        return new TimerViewHolder(parent, listener, mAsyncTimersTableUpdateHandler);
    }


}
