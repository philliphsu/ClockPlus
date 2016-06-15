package com.philliphsu.clock2.util;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by Phillip Hsu on 6/14/2016.
 */
public final class LocalBroadcastHelper {

    /** Sends a local broadcast using an intent with the action specified */
    public static void sendBroadcast(Context context, String action) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(action));
    }

    private LocalBroadcastHelper() {}
}
