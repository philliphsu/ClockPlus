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

package com.philliphsu.clock2.alarms.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnBootUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Note that this will be called when the device boots up, not when the app first launches.
        // We may have a lot of alarms to reschedule, so do this in the background using an IntentService.
        context.startService(new Intent(context, OnBootUpAlarmScheduler.class));
    }
}
