package com.philliphsu.clock2.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by Phillip Hsu on 6/14/2016.
 */
public final class LocalBroadcastHelper {

    /** Sends a local broadcast using an intent with the action specified */
    public static void sendBroadcast(Context context, String action) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(action));
    }

    /** Registers a BroadcastReceiver that filters intents by the action specified */
    // TODO: Refactor any local registrations to use this utility method.
    public static void registerReceiver(Context context, BroadcastReceiver receiver, String action) {
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, new IntentFilter(action));
    }

    // TODO: Refactor any local unregistrations to use this utility method.
    public static void unregisterReceiver(Context context, BroadcastReceiver receiver) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    }

    private LocalBroadcastHelper() {}
}
