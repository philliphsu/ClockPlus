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

package com.philliphsu.clock2.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by Phillip Hsu on 6/14/2016.
 */
public final class LocalBroadcastHelper {

    /** Sends a local broadcast using an intent with the action specified */
    public static void sendBroadcast(Context context, String action) {
        sendBroadcast(context, action, null);
    }

    /** Sends a local broadcast using an intent with the action and the extras specified */
    public static void sendBroadcast(Context context, String action, Bundle extras) {
        Intent intent = new Intent(action);
        if (extras != null) {
            intent.putExtras(extras);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /** Registers a BroadcastReceiver that filters intents by the actions specified */
    public static void registerReceiver(Context context, BroadcastReceiver receiver, String... actions) {
        IntentFilter filter = new IntentFilter();
        for (String action : actions)
            filter.addAction(action);
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);
    }

    public static void unregisterReceiver(Context context, BroadcastReceiver receiver) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    }

    private LocalBroadcastHelper() {}
}
