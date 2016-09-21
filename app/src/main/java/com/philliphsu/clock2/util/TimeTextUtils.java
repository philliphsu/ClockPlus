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
