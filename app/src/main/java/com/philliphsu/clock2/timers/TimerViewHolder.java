package com.philliphsu.clock2.timers;

import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.philliphsu.clock2.BaseViewHolder;
import com.philliphsu.clock2.OnListItemInteractionListener;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.Timer;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by Phillip Hsu on 7/25/2016.
 */
public class TimerViewHolder extends BaseViewHolder<Timer> {

    @Bind(R.id.label) TextView mLabel;
    @Bind(R.id.duration) CountdownChronometer mChronometer;
    @Bind(R.id.progress_bar) ProgressBar mProgressBar;
    @Bind(R.id.add_one_minute) ImageButton mAddOneMinute;
    @Bind(R.id.start_pause) ImageButton mStartPause;
    @Bind(R.id.stop) ImageButton mStop;

    public TimerViewHolder(ViewGroup parent, OnListItemInteractionListener<Timer> listener) {
        super(parent, R.layout.item_timer, listener);
    }

    @Override
    public void onBind(Timer timer) {
        super.onBind(timer);
        bindLabel(timer.label());
        bindChronometer(timer);
    }

    @OnClick(R.id.start_pause)
    void startPause() {
        // Every time BEFORE you call start() on the chronometer, you have to
        // call setBase() again because time passes between the time we first call
        // setBase() and the time we start the timer. Otherwise, after start() is called,
        // that period of time would appear to have counted off already, as the text
        // display jumps downward by that amount of time from its initial duration.
        Timer t = getItem();
        if (t.hasStarted()) {
            if (t.isRunning()) {
                // Records the start of this pause
                t.pause();
                // Stops the counting, but does not reset any values
                mChronometer.stop();
            } else {
                // Pushes up the end time
                t.resume();
                // Use the new end time as reference from now
                mChronometer.setBase(t.endTime());
                mChronometer.start();
            }
        } else {
            t.start();
            mChronometer.setBase(t.endTime());
            mChronometer.start();
        }
    }

    private void bindLabel(String label) {
        if (!label.isEmpty()) {
            mLabel.setText(label);
        }
    }

    private void bindChronometer(Timer timer) {
        // In case we're reusing a chronometer instance that could be running:
        // If the Timer instance is not running, this just guarantees the chronometer
        // won't tick, regardless of whether it was running.
        // If the Timer instance is running, we don't care whether the chronometer is
        // also running, because we call start() right after. Stopping it just
        // guarantees that, if it was running, we don't deliver another set of
        // concurrent messages to its handler.
        mChronometer.stop();

        if (!timer.hasStarted()) {
            // Set the initial text
            mChronometer.setDuration(timer.duration());
        } else if (timer.isRunning()) {
            // Re-initialize the base
            mChronometer.setBase(timer.endTime());
            // Previously stopped, so no old messages will interfere.
            mChronometer.start();
        } else {
            // Set the text as last displayed before we stopped.
            // When you call stop() on a Chronometer, it freezes the current text shown,
            // so why do we need this? While that is sufficient for a static View layout,
            // VH recycling will reuse the same Chronometer widget across multiple VHs,
            // so we would have invalid data across those VHs.
            // If a new VH is created, then the chronometer it contains will be in its
            // uninitialized state. We will always need to set the Chronometer's base
            // every time VHs are bound/recycled.
            mChronometer.setDuration(timer.timeRemaining());
        }
    }
}
