package com.philliphsu.clock2.timers;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.philliphsu.clock2.AsyncTimersTableUpdateHandler;
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
    private static final String TAG = "TimerViewHolder";

//    private TimerController mController;
    private final AsyncTimersTableUpdateHandler mAsyncTimersTableUpdateHandler;

    @Bind(R.id.label) TextView mLabel;
    @Bind(R.id.duration) CountdownChronometer mChronometer;
    @Bind(R.id.progress_bar) ProgressBar mProgressBar;
    @Bind(R.id.add_one_minute) ImageButton mAddOneMinute;
    @Bind(R.id.start_pause) ImageButton mStartPause;
    @Bind(R.id.stop) ImageButton mStop;

    // TODO: Controller param
    public TimerViewHolder(ViewGroup parent, OnListItemInteractionListener<Timer> listener,
                           AsyncTimersTableUpdateHandler asyncTimersTableUpdateHandler) {
        super(parent, R.layout.item_timer, listener);
        mAsyncTimersTableUpdateHandler = asyncTimersTableUpdateHandler;
    }

    @Override
    public void onBind(Timer timer) {
        super.onBind(timer);
        bindLabel(timer.label());
//        // We can't create the controller until this VH binds, because
//        // the widgets only exist after this point.
//        mController = new TimerController(timer, mChronometer, mAddOneMinute, mStartPause, mStop);
        bindChronometer(timer);
        bindButtonControls(timer);
    }

    @OnClick(R.id.start_pause)
    void startPause() {
        Timer t = getItem();
        if (t.isRunning()) {
//            mController.pause();
            t.pause();
        } else {
            if (t.hasStarted()) {
                t.resume();
            } else {
                t.start();
            }
        }
        // Persist value changes
        update();
    }

    @OnClick(R.id.add_one_minute)
    void addOneMinute() {
        getItem().addOneMinute();
        // Persist end time increase
        update();
    }

    @OnClick(R.id.stop)
    void stop() {
        getItem().stop();
        update();
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

    private void bindButtonControls(Timer timer) {
        // TODO: Pause and start icons, resp.
//        mStartPause.setImageResource(timer.isRunning() ? 0 : 0);
        int visibility = timer.hasStarted() ? View.VISIBLE : View.INVISIBLE;
        mAddOneMinute.setVisibility(visibility);
        mStop.setVisibility(visibility);
    }

    private void update() {
        Timer t = getItem();
        mAsyncTimersTableUpdateHandler.asyncUpdate(
                // Alternatively, use ViewHolder#getItemId() because we can forget
                // to set the id on the object in BaseItemCursor#getItem(). We
                // luckily remembered to this time!
                t.getId(), t);
    }
}
