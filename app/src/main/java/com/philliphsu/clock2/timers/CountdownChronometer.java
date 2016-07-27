package com.philliphsu.clock2.timers;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.util.Formatter;
import java.util.IllegalFormatException;
import java.util.Locale;

/**
 * Created by Phillip Hsu on 7/25/2016.
 *
 * A modified version of the framework's Chronometer widget to count down
 * towards the base time. The ability to count down was added to Chronometer
 * in API 24.
 */
public class CountdownChronometer extends TextView {
    private static final String TAG = "CountdownChronometer";

    /**
     * A callback that notifies when the chronometer has incremented on its own.
     */
    public interface OnChronometerTickListener {

        /**
         * Notification that the chronometer has changed.
         */
        void onChronometerTick(CountdownChronometer chronometer);

    }

    private long mBase;
    private long mNow; // the currently displayed time
//    private long mPause; // the time at which pause() was called
//    private long mDuration;
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
    public CountdownChronometer(Context context) {
        this(context, null, 0);
    }

    /**
     * Initialize with standard view layout information.
     * Sets the base to the current time.
     */
    public CountdownChronometer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Initialize with standard view layout information and style.
     * Sets the base to the current time.
     */
    public CountdownChronometer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

//        final TypedArray a = context.obtainStyledAttributes(
//                attrs, com.android.internal.R.styleable.Chronometer, defStyleAttr, 0);
//        setFormat(a.getString(com.android.internal.R.styleable.Chronometer_format));
//        a.recycle();

        init();
    }

    @TargetApi(21)
    public CountdownChronometer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

//        final TypedArray a = context.obtainStyledAttributes(
//                attrs, com.android.internal.R.styleable.Chronometer, defStyleAttr, defStyleRes);
//        setFormat(a.getString(com.android.internal.R.styleable.Chronometer_format));
//        a.recycle();

        init();
    }

    private void init() {
        mBase = SystemClock.elapsedRealtime();
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
//        mDuration = base - SystemClock.elapsedRealtime();
        dispatchChronometerTick();
        updateText(SystemClock.elapsedRealtime());
    }

    /**
     * Return the base time as set through {@link #setBase}.
     */
    public long getBase() {
        return mBase;
    }

    /**
     * Equivalent to {@link #setBase(long) setBase(SystemClock.elapsedRealtime() + duration)}.
     */
    public void setDuration(long duration) {
//        mDuration = duration;
        setBase(SystemClock.elapsedRealtime() + duration);
    }

//    /**
//     * Return the duration of this countdown.
//     */
//    public long getDuration() {
//        return mDuration;
//    }

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
        // If we don't do this, the text display won't get to count
        // all of the seconds in the set duration. Time passes
        // between the call to setDuration(), or setBase(), and start().
//        mBase = SystemClock.elapsedRealtime() + mDuration;
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
//        setDuration(mDuration); // Reset the text display
    }

//    public void pause() {
//        if (mPause == 0 && mRunning) {
//            mPause = SystemClock.elapsedRealtime();
//        }
//        mStarted = false;
//        updateRunning();
//    }
//
//    public void resume() {
//        if (mPause > 0 && !mRunning) {
//            mBase += SystemClock.elapsedRealtime() - mPause;
//            mPause = 0;
//        }
//        mStarted = true;
//        updateRunning();
//    }

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
        setText(text);
    }

    private void updateRunning() {
        boolean running = mVisible && mStarted;
        if (running != mRunning) {
            if (running) {
                Log.d(TAG, "Running");
                updateText(SystemClock.elapsedRealtime());
                dispatchChronometerTick();
                mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), 1000);
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
                updateText(SystemClock.elapsedRealtime());
                dispatchChronometerTick();
                sendMessageDelayed(Message.obtain(this, TICK_WHAT), 1000);
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
//        return CountdownChronometer.class.getName();
//    }
}
