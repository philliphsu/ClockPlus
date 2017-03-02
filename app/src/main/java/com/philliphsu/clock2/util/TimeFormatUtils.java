/*
 * Copyright 2017 Phillip Hsu
 *
 * This file is part of ClockPlus.
 *
 * ClockPlus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ClockPlus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ClockPlus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.philliphsu.clock2.util;

import android.content.Context;

import java.util.Calendar;
import java.util.Date;

import static android.text.format.DateFormat.getTimeFormat;

/**
 * Created by Phillip Hsu on 6/3/2016.
 */
public final class TimeFormatUtils {

    private TimeFormatUtils() {}

    public static String formatTime(Context context, long millis) {
        return getTimeFormat(context).format(new Date(millis));
    }

    public static String formatTime(Context context, int hourOfDay, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        return formatTime(context, cal.getTimeInMillis());
    }
}
