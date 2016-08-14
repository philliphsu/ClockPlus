package com.philliphsu.clock2.stopwatch;

import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.philliphsu.clock2.R;
import com.philliphsu.clock2.RecyclerViewFragment;
import com.philliphsu.clock2.util.ProgressBarUtils;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by Phillip Hsu on 8/8/2016.
 */
public class StopwatchFragment extends RecyclerViewFragment<
        Lap,
        LapViewHolder,
        LapCursor,
        LapsAdapter> {
    private static final String TAG = "StopwatchFragment";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_PAUSE_TIME = "pause_time";
    private static final String KEY_CHRONOMETER_RUNNING = "chronometer_running";

    private long mStartTime;
    private long mPauseTime;
    private Lap mCurrentLap;
    private Lap mPreviousLap;

    private AsyncLapsTableUpdateHandler mUpdateHandler;
    private ObjectAnimator mProgressAnimator;
    private SharedPreferences mPrefs;
    private WeakReference<FloatingActionButton> mActivityFab;

    @Bind(R.id.chronometer) ChronometerWithMillis mChronometer;
    @Bind(R.id.new_lap) FloatingActionButton mNewLapButton;
    @Bind(R.id.stop) FloatingActionButton mStopButton;
    @Bind(R.id.progress_bar) ProgressBar mProgressBar;

    /**
     * This is called only when a new instance of this Fragment is being created,
     * especially if the user is navigating to this tab for the first time in
     * this app session.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUpdateHandler = new AsyncLapsTableUpdateHandler(getActivity(), null/*we shouldn't need a scroll handler*/);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mStartTime = mPrefs.getLong(KEY_START_TIME, 0);
        mPauseTime = mPrefs.getLong(KEY_PAUSE_TIME, 0);
        // TODO: Any better solutions?
        mActivityFab = new WeakReference<>((FloatingActionButton) getActivity().findViewById(R.id.fab));
        Log.d(TAG, "mStartTime = " + mStartTime
                + ", mPauseTime = " + mPauseTime);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // TODO: Apply size span on chronom
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (mStartTime > 0) {
            long base = mStartTime;
            if (mPauseTime > 0) {
                base += SystemClock.elapsedRealtime() - mPauseTime;
                // We're not done pausing yet, so don't reset mPauseTime.
            }
            mChronometer.setBase(base);
        }
        if (mPrefs.getBoolean(KEY_CHRONOMETER_RUNNING, false)) {
            mChronometer.start();
        }
        // Hides the mini fabs prematurely, so when we actually select this tab
        // they don't show at all before hiding.
        updateButtonControls();
        return view;
    }

    /**
     * If the user navigates away, this is the furthest point in the lifecycle
     * this Fragment gets to. Here, the view hierarchy returned from onCreateView()
     * is destroyed--the Fragment itself is NOT destroyed. If the user navigates back
     * to this tab, this Fragment goes through its lifecycle beginning from onCreateView().
     *
     * TODO: Verify that members are not reset.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView()");
        Log.d(TAG, "mStartTime = " + mStartTime
                + ", mPauseTime = " + mPauseTime);
    }

    @Override
    public Loader<LapCursor> onCreateLoader(int id, Bundle args) {
        return new LapsCursorLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<LapCursor> loader, LapCursor data) {
        super.onLoadFinished(loader, data);
        // TODO: Will manipulating the cursor's position here affect the current
        // position in the adapter? Should we make a defensive copy and manipulate
        // that copy instead?
        if (data.moveToFirst()) {
            mCurrentLap = data.getItem();
            Log.d(TAG, "Current lap ID = " + mCurrentLap.getId());
        }
        if (data.moveToNext()) {
            mPreviousLap = data.getItem();
            Log.d(TAG, "Previous lap ID = " + mPreviousLap.getId());
        }
        if (mChronometer.isRunning() && mCurrentLap != null && mPreviousLap != null) {
            // We really only want to start a new animator when the NEWLY RETRIEVED current
            // and previous laps are different (i.e. different laps, NOT merely different instances)
            // from the CURRENT current and previous laps, as referenced by mCurrentLap and mPreviousLap.
            // However, both equals() and == are insufficient. Our cursor's getItem() will always
            // create new instances of Lap representing the underlying data, so an '== test' will
            // always fail to convey our intention. Also, equals() would fail especially when the
            // physical lap is paused/resumed, because the two instances in comparison
            // (the retrieved and current) would obviously
            // have different values for, e.g., t1 and pauseTime.
            //
            // Therefore, we'll just always end the previous animator and start a new one.
            // TODO: We may as well move the contents of this method here, since we're the only caller.
            startNewProgressBarAnimator();
        }
    }

    @Override
    public void onFabClick() {
        if (mChronometer.isRunning()) {
            mPauseTime = SystemClock.elapsedRealtime();
            mChronometer.stop();
            mCurrentLap.pause();
            mUpdateHandler.asyncUpdate(mCurrentLap.getId(), mCurrentLap);
            // No issues controlling the animator here, because onLoadFinished() can't
            // call through to startNewProgressBarAnimator(), because by that point
            // the chronometer won't be running.
            if (mProgressAnimator != null) {
                // We may as well call cancel(), since our resume() call would be
                // rendered meaningless.
//                mProgressAnimator.pause();
                mProgressAnimator.cancel();
            }
        } else {
            if (mStartTime == 0) {
                // TODO: I'm strongly considering inserting the very first lap alone.
                // We'll need to tell the adapter to just hide the corresponding VH
                // until a second lap is added.
                // addNewLap() won't call through unless chronometer is running, which
                // we can't start until we compute mStartTime
                mCurrentLap = new Lap();
                mUpdateHandler.asyncInsert(mCurrentLap);
            }
            mStartTime += SystemClock.elapsedRealtime() - mPauseTime;
            mPauseTime = 0;
            mChronometer.setBase(mStartTime);
            mChronometer.start();
            if (!mCurrentLap.isRunning()) {
                mCurrentLap.resume();
                mUpdateHandler.asyncUpdate(mCurrentLap.getId(), mCurrentLap);
            }
            // This animator instance will end up having end() called on it. When
            // the table update prompts us to requery, onLoadFinished will be called as a result.
            // There, it calls startNewProgressAnimator() to end this animation and starts an
            // entirely new animator instance.
//            if (mProgressAnimator != null) {
//                mProgressAnimator.resume();
//            }
        }
        updateButtonControls();
        savePrefs();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        // We get called multiple times, even when we're not yet visible.
        // This can be called before onCreateView() so widgets could be null, esp. if you had
        // navigated more than one page away before returning here. That means onDestroyView()
        // was called previously.
        // We will get called again when we actually have this page selected, and by that time
        // onCreateView() will have been called. Wait until we're resumed to call through.
        if (isVisibleToUser && isResumed()) {
            // At this point, the only thing this does is change the fab icon
            // TODO: allow duplicate code and manipulate the fab icon directly?
            // TODO: There is noticeable latency between showing this tab and
            // changing the icon. Consider writing a callback for this Fragment
            // that MainActivity can call in its onPageChangeListener. We don't merely
            // want to call such a callback in onPageSelected, because that is fired
            // when we reach an idle state, so we'd experience the same latency issue.
            // Rather, we should animate the icon change during onPageScrolled.
            updateButtonControls();
        }
    }

    @Nullable
    @Override
    protected LapsAdapter getAdapter() {
        if (super.getAdapter() != null)
            return super.getAdapter();
        return new LapsAdapter();
    }

    @Override
    protected int contentLayout() {
        return R.layout.fragment_stopwatch;
    }

    @OnClick(R.id.new_lap)
    void addNewLap() {
        if (!mChronometer.isRunning()) {
            Log.d(TAG, "Cannot add new lap");
            return;
        }
        if (mCurrentLap != null) {
            mCurrentLap.end(mChronometer.getText().toString());
        }
        mPreviousLap = mCurrentLap;
        mCurrentLap = new Lap();
        if (mPreviousLap != null) {
//            if (getAdapter().getItemCount() == 0) {
//                mUpdateHandler.asyncInsert(mPreviousLap);
//            } else {
                mUpdateHandler.asyncUpdate(mPreviousLap.getId(), mPreviousLap);
//            }
        }
        mUpdateHandler.asyncInsert(mCurrentLap);
        // This would end up being called twice: here, and in onLoadFinished(), because the
        // table updates will prompt us to requery.
//        startNewProgressBarAnimator();
    }

    @OnClick(R.id.stop)
    void stop() {
        mChronometer.stop();
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mStartTime = 0;
        mPauseTime = 0;
        mCurrentLap = null;
        mPreviousLap = null;
        mUpdateHandler.asyncClear(); // Clear laps
        // No issues controlling the animator here, because onLoadFinished() can't
        // call through to startNewProgressBarAnimator(), because by that point
        // the chronometer won't be running.
        if (mProgressAnimator != null) {
            mProgressAnimator.end();
        }
        mProgressAnimator = null;
        updateButtonControls();
        savePrefs();
    }

    private void updateButtonControls() {
        boolean started = mStartTime > 0;
        int vis = started ? View.VISIBLE : View.INVISIBLE;
        mNewLapButton.setVisibility(vis);
        mStopButton.setVisibility(vis);
        if (isVisible()) { // avoid changing the icon prematurely, esp. when we're not on this tab
            // TODO: pause and start icon, resp.
            mActivityFab.get().setImageResource(mChronometer.isRunning() ? 0 : 0);
        }
    }

    private void startNewProgressBarAnimator() {
        if (mProgressAnimator != null) {
            mProgressAnimator.end();
        }
        long timeRemaining = mPreviousLap.elapsed() - mCurrentLap.elapsed();
        if (timeRemaining <= 0)
            return;
        // The cast is necessary, or else we'd have integer division between two longs and we'd
        // always get zero since the numerator will always be less than the denominator.
        double ratioTimeRemaining = timeRemaining / (double) mPreviousLap.elapsed();
        mProgressAnimator = ProgressBarUtils.startNewAnimator(
                mProgressBar, ratioTimeRemaining, timeRemaining);
    }

    private void savePrefs() {
        mPrefs.edit().putLong(KEY_START_TIME, mStartTime)
                .putLong(KEY_PAUSE_TIME, mPauseTime)
                .putBoolean(KEY_CHRONOMETER_RUNNING, mChronometer.isRunning())
                .apply();
    }

    // ======================= DO NOT IMPLEMENT ============================

    @Override
    protected void onScrolledToStableId(long id, int position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onListItemClick(Lap item, int position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onListItemDeleted(Lap item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onListItemUpdate(Lap item, int position) {
        throw new UnsupportedOperationException();
    }
}
