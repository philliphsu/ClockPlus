package com.philliphsu.clock2.timers;

import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.Log;

import java.util.Formatter;
import java.util.IllegalFormatException;
import java.util.Locale;

/**
 * Created by Phillip Hsu on 9/7/2016.
 *
 * A helper class for CountdownChronometer that handles formatting the countdown text.
 * TODO: A similar delegate class can also be made for ChronometerWithMillis. However, try to
 * use a common base class between this and ChronometerWithMillis.
 */
final class CountdownDelegate {
    private static final String TAG = "CountdownDelegate";

    private long mBase;
    private long mNow; // the currently displayed time
    private boolean mLogged;
    private String mFormat;
    private Formatter mFormatter;
    private Locale mFormatterLocale;
    private Object[] mFormatterArgs = new Object[1];
    private StringBuilder mFormatBuilder;
    private StringBuilder mRecycle = new StringBuilder(8);

    void init() {
        mBase = SystemClock.elapsedRealtime();
    }

    void setBase(long base) {
        mBase = base;
    }

    long getBase() {
        return mBase;
    }

    void setFormat(String format) {
        mFormat = format;
        if (format != null && mFormatBuilder == null) {
            mFormatBuilder = new StringBuilder(format.length() * 2);
        }
    }

    String getFormat() {
        return mFormat;
    }

    String formatElapsedTime(long now) {
        mNow = now;
        long seconds = mBase - now;
        seconds /= 1000;
        String text = DateUtils.formatElapsedTime(mRecycle, seconds);

        if (mFormat != null) {
            Locale loc = Locale.getDefault();
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

        return text;
    }
}
