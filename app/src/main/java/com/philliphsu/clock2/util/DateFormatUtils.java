package com.philliphsu.clock2.util;

import android.content.Context;

import java.util.Date;

import static android.text.format.DateFormat.getTimeFormat;

/**
 * Created by Phillip Hsu on 6/3/2016.
 */
public final class DateFormatUtils {

    private DateFormatUtils() {}

    public static String formatTime(Context context, long millis) {
        return getTimeFormat(context).format(new Date(millis));
    }
}
