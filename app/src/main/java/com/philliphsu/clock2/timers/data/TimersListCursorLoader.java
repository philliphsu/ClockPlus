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

package com.philliphsu.clock2.timers.data;

import android.content.Context;

import com.philliphsu.clock2.timers.Timer;
import com.philliphsu.clock2.data.SQLiteCursorLoader;

/**
 * Created by Phillip Hsu on 7/29/2016.
 */
public class TimersListCursorLoader extends SQLiteCursorLoader<Timer, TimerCursor> {
    public static final String ACTION_CHANGE_CONTENT
            = "com.philliphsu.clock2.timers.data.action.CHANGE_CONTENT";

    public TimersListCursorLoader(Context context) {
        super(context);
    }

    @Override
    protected TimerCursor loadCursor() {
        return new TimersTableManager(getContext()).queryItems();
    }

    @Override
    protected String getOnContentChangeAction() {
        return ACTION_CHANGE_CONTENT;
    }
}
