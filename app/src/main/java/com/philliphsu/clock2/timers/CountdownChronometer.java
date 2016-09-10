/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.philliphsu.clock2.timers;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;

import com.philliphsu.clock2.BaseChronometer;

/**
 * Created by Phillip Hsu on 7/25/2016.
 *
 * A modified version of the framework's Chronometer widget to count down
 * towards the base time. The ability to count down was added to Chronometer
 * in API 24.
 */
public class CountdownChronometer extends BaseChronometer {
    private static final String TAG = "CountdownChronometer";

    public CountdownChronometer(Context context) {
        this(context, null, 0);
    }

    public CountdownChronometer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CountdownChronometer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public CountdownChronometer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setCountDown(true);
    }
}
