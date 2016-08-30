package com.philliphsu.clock2.util;

import android.content.res.Resources;

/**
 * Created by Phillip Hsu on 8/30/2016.
 */
public final class ConfigurationUtils {

    public static int getOrientation(Resources res) {
        return res.getConfiguration().orientation;
    }

    private ConfigurationUtils() {}

}
