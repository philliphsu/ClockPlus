package com.philliphsu.clock2;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.philliphsu.clock2.timers.ChronometerDelegate;

/**
 * Created by Phillip Hsu on 9/9/2016.
 *
 * Based on the framework's Chronometer class. Can be configured as a countdown
 * chronometer and can also show centiseconds.
 */
public class BaseChronometer extends TextView {
    private static final String TAG = "BaseChronometer";

    /**
     * A callback that notifies when the chronometer has incremented on its own.
     */
    public interface OnChronometerTickListener {
        /**
         * Notification that the chronometer has changed.
         */
        void onChronometerTick(BaseChronometer chronometer);
    }

    private boolean mVisible;
    private boolean mStarted;
    private boolean mRunning;
    private long mTickInterval;
    private OnChronometerTickListener mOnChronometerTickListener;
    private final ChronometerDelegate mDelegate = new ChronometerDelegate();

    /**
     * Initialize this Chronometer object.
     * Sets the base to the current time.
     */
    public BaseChronometer(Context context) {
        this(context, null, 0);
    }

    /**
     * Initialize with standard view layout information.
     * Sets the base to the current time.
     */
    public BaseChronometer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Initialize with standard view layout information and style.
     * Sets the base to the current time.
     */
    public BaseChronometer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

//        final TypedArray a = context.obtainStyledAttributes(
//                attrs, com.android.internal.R.styleable.Chronometer, defStyleAttr, 0);
//        setFormat(a.getString(com.android.internal.R.styleable.Chronometer_format));
//        a.recycle();

        init();
    }

    @TargetApi(21)
    public BaseChronometer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

//        final TypedArray a = context.obtainStyledAttributes(
//                attrs, com.android.internal.R.styleable.Chronometer, defStyleAttr, defStyleRes);
//        setFormat(a.getString(com.android.internal.R.styleable.Chronometer_format));
//        a.recycle();

        init();
    }

    private void init() {
        mDelegate.init();
        updateText(SystemClock.elapsedRealtime());
        mTickInterval = 1000;
    }

    /**
     * Set this view to count down to the base instead of counting up from it.
     *
     * @param countDown whether this view should count down
     *
     * @see #setBase(long)
     */
    public void setCountDown(boolean countDown) {
        mDelegate.setCountDown(countDown);
        updateText(SystemClock.elapsedRealtime());
    }

    /**
     * @return whether this view counts down
     *
     * @see #setCountDown(boolean)
     */
    public boolean isCountDown() {
        return mDelegate.isCountDown();
    }

    /**
     * Set this view to show centiseconds and to apply a size span on the centiseconds text.
     * <b>NOTE: Calling this method will reset the chronometer, so the visibility
     * of the centiseconds text can be updated.</b> You should call this method
     * before you {@link #start()} this chronometer, because it makes no sense to show the
     * centiseconds any time after the start of ticking.
     *
     * @param showCentiseconds whether this view should show centiseconds
     * @param applySizeSpan whether a size span should be applied to the centiseconds text
     */
    public void setShowCentiseconds(boolean showCentiseconds, boolean applySizeSpan) {
        mDelegate.setShowCentiseconds(showCentiseconds, applySizeSpan);
        // Clear and update the text again. The reason we don't just update the text, as in
        // setCountDown(), is that if showCentiseconds is true, we will be increasing the
        // granularity of this time display; the time delta between the first
        // setting of the base time (when this view is first instantiated), and the next call
        // to updateText() is, while minuscule, significant enough that there would be some nonzero
        // centiseconds value initially displayed. By resetting the timer, we minimize
        // this time delta, and that should display an initial centiseconds value of zero.
        init();
        // ----------------------------------------------------------------------------
        // TOneverDO: Precede init(), because init() resets mTickInterval to 1000.
        mTickInterval = showCentiseconds ? 10 : 1000;
        // ----------------------------------------------------------------------------
    }

    /**
     * @return whether this view shows centiseconds
     *
     * @see #setShowCentiseconds(boolean, boolean)
     */
    public boolean showsCentiseconds() {
        return mDelegate.showsCentiseconds();
    }

    /**
     * @return whether this view is currently running
     *
     * @see #start()
     * @see #stop()
     */
    public boolean isRunning() {
        return mRunning;
    }

    /**
     * Set the time that the count-up timer is in reference to.
     *
     * @param base Use the {@link SystemClock#elapsedRealtime} time base.
     */
//    @android.view.RemotableViewMethod
    public void setBase(long base) {
        mDelegate.setBase(base);
        dispatchChronometerTick();
        updateText(SystemClock.elapsedRealtime());
    }

    /**
     * Return the base time as set through {@link #setBase}.
     */
    public long getBase() {
        return mDelegate.getBase();
    }

    /**
     * If {@link #isCountDown()}, equivalent to {@link #setBase(long)
     * setBase(SystemClock.elapsedRealtime() + duration)}.
     * <p>
     * Otherwise, equivalent to {@link #setBase(long)
     * setBase(SystemClock.elapsedRealtime() - duration)}.
     */
    public void setDuration(long duration) {
        setBase(SystemClock.elapsedRealtime() + (isCountDown() ? duration : -duration));
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
        mDelegate.setFormat(format);
    }

    /**
     * Returns the current format string as set through {@link #setFormat}.
     */
    public String getFormat() {
        return mDelegate.getFormat();
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

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        updateRunning();
    }

    private synchronized void updateText(long now) {
        setText(mDelegate.formatElapsedTime(now, getResources()));
    }

    private void updateRunning() {
        // The isShown() check is new to the Chronometer source in API 24.
        // It is preventing the chronometer in TimerViewHolder from ticking, so leave it off.
        boolean running = mVisible && mStarted /*&& isShown()*/;
        if (running != mRunning) {
            if (running) {
                Log.d(TAG, "Running");
                updateText(SystemClock.elapsedRealtime());
                dispatchChronometerTick();
                postDelayed(mTickRunnable, mTickInterval);
            } else {
                Log.d(TAG, "Not running anymore");
                removeCallbacks(mTickRunnable);
            }
            mRunning = running;
        }
    }

    private final Runnable mTickRunnable = new Runnable() {
        @Override
        public void run() {
            if (mRunning) {
                updateText(SystemClock.elapsedRealtime());
                dispatchChronometerTick();
                postDelayed(mTickRunnable, mTickInterval);
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
//                        // TODO: Copy the resource into our own project
//                        com.android.internal.R.plurals.duration_hours, h, h));
//            }
//            if (m > 0) {
//                if (text.length() > 0) {
//                    text.append(' ');
//                }
//                text.append(res.getQuantityString(
//                        // TODO: Copy the resource into our own project
//                        com.android.internal.R.plurals.duration_minutes, m, m));
//            }
//
//            if (text.length() > 0) {
//                text.append(' ');
//            }
//            text.append(res.getQuantityString(
//                    // TODO: Copy the resource into our own project
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
//        return BaseChronometer.class.getName();
//    }
}
