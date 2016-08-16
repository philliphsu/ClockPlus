/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.philliphsu.clock2.alarms;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.philliphsu.clock2.util.DurationUtils;

import java.util.Formatter;
import java.util.IllegalFormatException;
import java.util.Locale;

/**
 * Created by Phillip Hsu on 4/30/2016.
 * 
 * A modified version of the framework's Chronometer class that counts down toward the base time,
 * uses the {@link System#currentTimeMillis()} timebase, and only has minute precision. It displays
 * the current timer value in the form "In H hrs and M mins". // TODO: This isn't the actual format. Verify what that is.
 */
public class AlarmCountdown extends TextView {
    private static final String TAG = "AlarmCountdown";

    /**
     * A callback that notifies when the chronometer has incremented on its own.
     */
    public interface OnChronometerTickListener {

        /**
         * Notification that the chronometer has changed.
         */
        void onChronometerTick(AlarmCountdown chronometer);

    }

    private long mBase;
    private long mNow; // the currently displayed time
    private boolean mVisible;
    private boolean mStarted;
    private boolean mRunning;
    private boolean mLogged;
    private String mFormat;
    private Formatter mFormatter;
    private Locale mFormatterLocale;
    private Object[] mFormatterArgs = new Object[1];
    private StringBuilder mFormatBuilder;
    private OnChronometerTickListener mOnChronometerTickListener;
    private StringBuilder mRecycle = new StringBuilder(8);

    private static final int TICK_WHAT = 2;

    /**
     * Initialize this Chronometer object.
     * Sets the base to the current time.
     */
    public AlarmCountdown(Context context) {
        this(context, null, 0);
    }

    /**
     * Initialize with standard view layout information.
     * Sets the base to the current time.
     */
    public AlarmCountdown(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Initialize with standard view layout information and style.
     * Sets the base to the current time.
     */
    public AlarmCountdown(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

//        final TypedArray a = context.obtainStyledAttributes(
//                attrs, com.android.internal.R.styleable.Chronometer, defStyleAttr, 0);
//        setFormat(a.getString(com.android.internal.R.styleable.Chronometer_format));
//        a.recycle();

        init();
    }

    @TargetApi(21)
    public AlarmCountdown(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

//        final TypedArray a = context.obtainStyledAttributes(
//                attrs, com.android.internal.R.styleable.Chronometer, defStyleAttr, defStyleRes);
//        setFormat(a.getString(com.android.internal.R.styleable.Chronometer_format));
//        a.recycle();

        init();
    }

    private void init() {
        mBase = System.currentTimeMillis();
        updateText(mBase);
    }

    /**
     * Set the time that the count-up timer is in reference to.
     *
     * @param base Use the {@link SystemClock#elapsedRealtime} time base.
     */
//    @android.view.RemotableViewMethod
    public void setBase(long base) {
        mBase = base;
        dispatchChronometerTick();
        updateText(System.currentTimeMillis());
    }

    /**
     * Return the base time as set through {@link #setBase}.
     */
    public long getBase() {
        return mBase;
    }

    /**
     * Sets the format string used for display.  The Chronometer will display
     * this string, with the first "%s" replaced by the current timer value in
     * "MM:SS" or "H:MM:SS" form.
     *
     * If the format string is null, or if you never call setFormat(), the
     * Chronometer will simply display the timer value in "MM:SS" or "H:MM:SS"
     * form.
     *
     * @param format the format string.
     */
//    @android.view.RemotableViewMethod
    public void setFormat(String format) {
        mFormat = format;
        if (format != null && mFormatBuilder == null) {
            mFormatBuilder = new StringBuilder(format.length() * 2);
        }
    }

    /**
     * Returns the current format string as set through {@link #setFormat}.
     */
    public String getFormat() {
        return mFormat;
    }

    /**
     * Sets the listener to be called when the chronometer changes.
     *
     * @param listener The listener.
     */
    public void setOnChronometerTickListener(OnChronometerTickListener listener) {
        mOnChronometerTickListener = listener;
    }

    /**
     * @return The listener (may be null) that is listening for chronometer change
     *         events.
     */
    public OnChronometerTickListener getOnChronometerTickListener() {
        return mOnChronometerTickListener;
    }

    /**
     * Start counting up.  This does not affect the base as set from {@link #setBase}, just
     * the view display.
     *
     * Chronometer works by regularly scheduling messages to the handler, even when the
     * Widget is not visible.  To make sure resource leaks do not occur, the user should
     * make sure that each start() call has a reciprocal call to {@link #stop}.
     */
    public void start() {
        mStarted = true;
        updateRunning();
    }

    /**
     * Stop counting up.  This does not affect the base as set from {@link #setBase}, just
     * the view display.
     *
     * This stops the messages to the handler, effectively releasing resources that would
     * be held as the chronometer is running, via {@link #start}.
     */
    public void stop() {
        mStarted = false;
        updateRunning();
    }

    /**
     * The same as calling {@link #start} or {@link #stop}.
     * @hide pending API council approval
     */
//    @android.view.RemotableViewMethod
    public void setStarted(boolean started) {
        mStarted = started;
        updateRunning();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mVisible = false;
        updateRunning();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        Log.d(TAG, "onWindowVisibilityChanged()");
        super.onWindowVisibilityChanged(visibility);
        mVisible = visibility == VISIBLE;
        updateRunning();
    }

    private synchronized void updateText(long now) {
        mNow = now;
        long millis = mBase - now;
//        seconds /= 1000;
        // This only shows HH:MM:SS, but we need days as well!
//        String text = DateUtils.formatElapsedTime(mRecycle, seconds);
        String text = DurationUtils.toString(getContext(), millis, true/*abbreviate labels*/);
        
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
        setText(text);
    }

    private void updateRunning() {
        boolean running = mVisible && mStarted;
        if (running != mRunning) {
            if (running) {
                Log.d(TAG, "Running");
                updateText(System.currentTimeMillis());
                dispatchChronometerTick();
                // TODO: Verify that every 60000ms is what we want.
                mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), 60000);
            } else {
                Log.d(TAG, "Not running anymore");
                mHandler.removeMessages(TICK_WHAT);
            }
            mRunning = running;
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message m) {
            if (mRunning) {
                updateText(System.currentTimeMillis());
                dispatchChronometerTick();
                // TODO: Verify that every 60000ms is what we want.
                sendMessageDelayed(Message.obtain(this, TICK_WHAT), 60000);
            }
        }
    };

    void dispatchChronometerTick() {
        if (mOnChronometerTickListener != null) {
            mOnChronometerTickListener.onChronometerTick(this);
        }
    }

    private static final int MIN_IN_SEC = 60;
    private static final int HOUR_IN_SEC = MIN_IN_SEC*60;
//    private static String formatDuration(long ms) {
//        final Resources res = Resources.getSystem();
//        final StringBuilder text = new StringBuilder();
//
//        int duration = (int) (ms / DateUtils.SECOND_IN_MILLIS);
//        if (duration < 0) {
//            duration = -duration;
//        }
//
//        int h = 0;
//        int m = 0;
//
//        if (duration >= HOUR_IN_SEC) {
//            h = duration / HOUR_IN_SEC;
//            duration -= h * HOUR_IN_SEC;
//        }
//        if (duration >= MIN_IN_SEC) {
//            m = duration / MIN_IN_SEC;
//            duration -= m * MIN_IN_SEC;
//        }
//        int s = duration;
//
//        try {
//            if (h > 0) {
//                text.append(res.getQuantityString(
//                        com.android.internal.R.plurals.duration_hours, h, h));
//            }
//            if (m > 0) {
//                if (text.length() > 0) {
//                    text.append(' ');
//                }
//                text.append(res.getQuantityString(
//                        com.android.internal.R.plurals.duration_minutes, m, m));
//            }
//
//            if (text.length() > 0) {
//                text.append(' ');
//            }
//            text.append(res.getQuantityString(
//                    com.android.internal.R.plurals.duration_seconds, s, s));
//        } catch (Resources.NotFoundException e) {
//            // Ignore; plurals throws an exception for an untranslated quantity for a given locale.
//            return null;
//        }
//        return text.toString();
//    }
//
//    @Override
//    public CharSequence getContentDescription() {
//        return formatDuration(mNow - mBase);
//    }
//
//    @Override
//    public CharSequence getAccessibilityClassName() {
//        return AlarmCountdown.class.getName();
//    }
}
