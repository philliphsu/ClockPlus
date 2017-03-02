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

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.widget.TextView;

/**
 * Created by Phillip Hsu on 7/16/2016.
 */
public class TimeTextUtils {

    private TimeTextUtils() {}

    private static final RelativeSizeSpan AMPM_SIZE_SPAN = new RelativeSizeSpan(0.5f);

    /**
     * Sets the given String on the TextView.
     * If the given String contains the "AM" or "PM" label,
     * this first applies a size span on the label.
     * @param textTime the time String that may contain "AM" or "PM"
     * @param textView the TextView to display {@code textTime}
     */
    public static void setText(String textTime, TextView textView) {
        if (textTime.contains("AM") || textTime.contains("PM")) {
            SpannableString s = new SpannableString(textTime);
            s.setSpan(AMPM_SIZE_SPAN, textTime.indexOf(" "), textTime.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(s, TextView.BufferType.SPANNABLE);
        } else {
            textView.setText(textTime);
        }
    }
}
