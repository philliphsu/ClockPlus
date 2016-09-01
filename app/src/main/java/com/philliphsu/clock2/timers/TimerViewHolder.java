package com.philliphsu.clock2.timers;

import android.animation.ObjectAnimator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.philliphsu.clock2.AddLabelDialog;
import com.philliphsu.clock2.AsyncTimersTableUpdateHandler;
import com.philliphsu.clock2.BaseViewHolder;
import com.philliphsu.clock2.OnListItemInteractionListener;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.Timer;
import com.philliphsu.clock2.util.ProgressBarUtils;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by Phillip Hsu on 7/25/2016.
 */
public class TimerViewHolder extends BaseViewHolder<Timer> {
    private static final String TAG = "TimerViewHolder";

    private final AsyncTimersTableUpdateHandler mAsyncTimersTableUpdateHandler;
    private TimerController mController;
    private ObjectAnimator mProgressAnimator;

    @Bind(R.id.label) TextView mLabel;
    @Bind(R.id.duration) CountdownChronometer mChronometer;
    @Bind(R.id.seek_bar) SeekBar mSeekBar;
    @Bind(R.id.add_one_minute) TextView mAddOneMinute;
    @Bind(R.id.start_pause) ImageButton mStartPause;
    @Bind(R.id.stop) ImageButton mStop;

    public TimerViewHolder(ViewGroup parent, OnListItemInteractionListener<Timer> listener,
                           AsyncTimersTableUpdateHandler asyncTimersTableUpdateHandler) {
        super(parent, R.layout.item_timer, listener);
        Log.d(TAG, "New TimerViewHolder");
        mAsyncTimersTableUpdateHandler = asyncTimersTableUpdateHandler;
    }

    @Override
    public void onBind(final Timer timer) {
        super.onBind(timer);
        Log.d(TAG, "Binding TimerViewHolder");
        // TOneverDO: create before super
        mController = new TimerController(timer, mAsyncTimersTableUpdateHandler);
        bindLabel(timer.label());
        bindChronometer(timer);
        bindButtonControls(timer);
        bindProgressBar(timer);
    }

    @OnClick(R.id.start_pause)
    void startPause() {
        mController.startPause();
    }

    @OnClick(R.id.add_one_minute)
    void addOneMinute() {
        mController.addOneMinute();
    }

    @OnClick(R.id.stop)
    void stop() {
        mController.stop();
    }

    @OnClick(R.id.label)
    void openLabelEditor() {
        AddLabelDialog dialog = AddLabelDialog.newInstance(new AddLabelDialog.OnLabelSetListener() {
            @Override
            public void onLabelSet(String label) {
                mLabel.setText(label);
                // TODO: persist change. Use TimerController and its update()
            }
        }, mLabel.getText());
        // TODO: This is bad! Use a Controller instead!
        AppCompatActivity act = (AppCompatActivity) getContext();
        dialog.show(act.getSupportFragmentManager(), "TAG");
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

        // TODO: I think we can simplify all this to just:
        // mChronometer.setDuration(timer.timeRemaining())
        // if we make the modification to the method as
        // described in the Timer class.
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

    private void bindProgressBar(Timer timer) {
        final long timeRemaining = timer.timeRemaining();
        double ratio = (double) timeRemaining / timer.duration();

        // In case we're reusing an animator instance that could be running
        if (mProgressAnimator != null && mProgressAnimator.isRunning()) {
            mProgressAnimator.end();
        }

        if (!timer.isRunning()) {
            // If our scale were 1, then casting ratio to an int will ALWAYS
            // truncate down to zero.
//            mSeekBar.setMax(100);
//            final int progress = (int) (100 * ratio);
//            mSeekBar.setProgress(progress);
            ProgressBarUtils.setProgress(mSeekBar, ratio);
//            mSeekBar.getThumb().mutate().setAlpha(progress == 0 ? 0 : 255);
        } else {
//            mSeekBar.getThumb().mutate().setAlpha(255);
            mProgressAnimator = ProgressBarUtils.startNewAnimator(
                    mSeekBar, ratio, timeRemaining);
        }
        mSeekBar.getThumb().mutate().setAlpha(timeRemaining <= 0 ? 0 : 255);
    }
}
