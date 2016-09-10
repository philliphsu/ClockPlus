package com.philliphsu.clock2.timers;

import android.os.SystemClock;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.RelativeSizeSpan;
import android.util.Log;

import java.util.Formatter;
import java.util.IllegalFormatException;
import java.util.Locale;

/**
 * Created by Phillip Hsu on 9/7/2016.
 *
 * A helper class for BaseChronometer that handles formatting the text.
 * Can also be used independent of Chronometer to format elapsed times and return the result
 * as a CharSequence.
 */
public final class ChronometerDelegate {
    private static final String TAG = "ChronometerDelegate";

    private static final RelativeSizeSpan SIZE_SPAN = new RelativeSizeSpan(0.5f);

    private long mBase;
    private long mNow; // the currently displayed time
    private boolean mLogged;
    private String mFormat;
    private Formatter mFormatter;
    private Locale mFormatterLocale;
    private Object[] mFormatterArgs = new Object[1];
    private StringBuilder mFormatBuilder;
    private StringBuilder mRecycle = new StringBuilder(8);
    private boolean mCountDown;
    private boolean mShowCentiseconds;
    private boolean mApplySizeSpanOnCentiseconds;

    public void init() {
        mBase = SystemClock.elapsedRealtime();
    }

    public void setCountDown(boolean countDown) {
        mCountDown = countDown;
    }

    public boolean isCountDown() {
        return mCountDown;
    }

    public void setShowCentiseconds(boolean showCentiseconds, boolean applySizeSpan) {
        mShowCentiseconds = showCentiseconds;
        mApplySizeSpanOnCentiseconds = applySizeSpan;
    }

    public boolean showsCentiseconds() {
        return mShowCentiseconds;
    }

    public void setBase(long base) {
        mBase = base;
    }

    public long getBase() {
        return mBase;
    }

    public void setFormat(String format) {
        mFormat = format;
        if (format != null && mFormatBuilder == null) {
            mFormatBuilder = new StringBuilder(format.length() * 2);
        }
    }

    public String getFormat() {
        return mFormat;
    }

    public CharSequence formatElapsedTime(long now) {
        mNow = now;
        long millis = mCountDown ? mBase - now : now - mBase;
        String text = DateUtils.formatElapsedTime(mRecycle, millis / 1000);

        Locale loc = Locale.getDefault();
        if (mFormat != null) {
            if (mFormatter == null || !loc.equals(mFormatterLocale)) {
                mFormatterLocale = loc;
                mFormatter = new Formatter(mFormatBuilder, loc);
            }
            mFormatBuilder.setLength(0);
            mFormatterArgs[0] = text;
            try {
                mFormatter.format(mFormat, mFormatterArgs);
                text = mFormatBuilder.toString();
            } catch (IllegalFormatException ex) {
                if (!mLogged) {
                    Log.w(TAG, "Illegal format string: " + mFormat);
                    mLogged = true;
                }
            }
        }
        if (mShowCentiseconds) {
            long centiseconds = (millis % 1000) / 10;
            String centisecondsText = String.format(loc,
                    // TODO: Different locales use different decimal marks.
                    // The two most common are . and ,
                    // Consider removing the . and just let the size span
                    // represent this as fractional seconds?
                    // ...or figure out how to get the correct mark for the
                    // current locale.
                    // It looks like Google's Clock app strictly uses .
                    ".%02d", // The . before % is not a format specifier
                    centiseconds);
            if (mApplySizeSpanOnCentiseconds) {
                SpannableString span = new SpannableString(centisecondsText);
                span.setSpan(SIZE_SPAN, 0, centisecondsText.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                return TextUtils.concat(text, span);
            } else {
                return text.concat(centisecondsText);
            }
        }

        return text;
    }
}
