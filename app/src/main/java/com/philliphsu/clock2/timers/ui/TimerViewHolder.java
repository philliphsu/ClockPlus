/*
 * Copyright (C) 2016 Phillip Hsu
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

package com.philliphsu.clock2.timers.ui;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.IdRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.philliphsu.clock2.dialogs.AddLabelDialog;
import com.philliphsu.clock2.dialogs.AddLabelDialogController;
import com.philliphsu.clock2.timers.TimerController;
import com.philliphsu.clock2.ringtone.playback.TimerRingtoneService;
import com.philliphsu.clock2.timers.data.AsyncTimersTableUpdateHandler;
import com.philliphsu.clock2.list.BaseViewHolder;
import com.philliphsu.clock2.list.OnListItemInteractionListener;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.timers.Timer;
import com.philliphsu.clock2.util.FragmentTagUtils;
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
    private final Drawable mStartIcon;
    private final Drawable mPauseIcon;
    private final PopupMenu mPopupMenu;
    private final AddLabelDialogController mAddLabelDialogController;

    @Bind(R.id.label) TextView mLabel;
    @Bind(R.id.duration) CountdownChronometer mChronometer;
    @Bind(R.id.seek_bar) SeekBar mSeekBar;
    @Bind(R.id.add_one_minute) TextView mAddOneMinute;
    @Bind(R.id.start_pause) ImageButton mStartPause;
    @Bind(R.id.stop) ImageButton mStop;
    @Bind(R.id.menu) ImageButton mMenuButton;

    public TimerViewHolder(ViewGroup parent, OnListItemInteractionListener<Timer> listener,
                           AsyncTimersTableUpdateHandler asyncTimersTableUpdateHandler) {
        super(parent, R.layout.item_timer, listener);
        Log.d(TAG, "New TimerViewHolder");
        mAsyncTimersTableUpdateHandler = asyncTimersTableUpdateHandler;
        mStartIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_start_24dp);
        mPauseIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_pause_24dp);

        // TODO: This is bad! Use a Controller/Presenter instead...
        // or simply pass in an instance of FragmentManager to the ctor.
        AppCompatActivity act = (AppCompatActivity) getContext();
        mAddLabelDialogController = new AddLabelDialogController(
                act.getSupportFragmentManager(),
                new AddLabelDialog.OnLabelSetListener() {
                    @Override
                    public void onLabelSet(String label) {
                        mController.updateLabel(label);
                    }
                });

        // The item layout is inflated in the super ctor, so we can safely reference our views.
        mPopupMenu = new PopupMenu(getContext(), mMenuButton);
        mPopupMenu.inflate(R.menu.menu_timer_viewholder);
        mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        mController.deleteTimer();
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onBind(final Timer timer) {
        super.onBind(timer);
        Log.d(TAG, "Binding TimerViewHolder");
        // TOneverDO: create before super
        mController = new TimerController(timer, mAsyncTimersTableUpdateHandler);
        // Items that are not in view will not be bound. If in one orientation the item was in view
        // and in another it is out of view, then the callback for that item will not be restored
        // for the new orientation.
        mAddLabelDialogController.tryRestoreCallback(makeTag(R.id.label));
        Log.d(TAG, "timer.label() = " + timer.label());
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
        getContext().stopService(new Intent(getContext(), TimerRingtoneService.class));
    }

    @OnClick(R.id.label)
    void openLabelEditor() {
        mAddLabelDialogController.show(mLabel.getText(), makeTag(R.id.label));
    }

    @OnClick(R.id.menu)
    void openMenu() {
        mPopupMenu.show();
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
        mStartPause.setImageDrawable(timer.isRunning() ? mPauseIcon : mStartIcon);
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

    private String makeTag(@IdRes int viewId) {
        return FragmentTagUtils.makeTag(TimerViewHolder.class, viewId, getItemId());
    }
}
