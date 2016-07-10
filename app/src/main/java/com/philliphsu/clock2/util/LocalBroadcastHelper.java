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
