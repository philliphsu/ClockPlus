package com.philliphsu.clock2.stopwatch;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

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
    private Drawable mStartDrawable;
    private Drawable mPauseDrawable;

    @Bind(R.id.chronometer) ChronometerWithMillis mChronometer;
    @Bind(R.id.new_lap) FloatingActionButton mNewLapButton;
    @Bind(R.id.stop) FloatingActionButton mStopButton;
    @Bind(R.id.seek_bar) SeekBar mSeekBar;

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
        Log.d(TAG, "mStartTime = " + mStartTime
                + ", mPauseTime = " + mPauseTime);
        // TODO: Will these be kept alive after onDestroyView()? If not, we should move these to
        // onCreateView() or any other callback that is guaranteed to be called.
        mStartDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.ic_start_24dp);
        mPauseDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.ic_pause_24dp);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mChronometer.setApplySizeSpan(true);
        if (mStartTime > 0) {
            long base = mStartTime;
            if (mPauseTime > 0) {
                base += SystemClock.elapsedRealtime() - mPauseTime;
                // We're not done pausing yet, so don't reset mPauseTime.
            }
            mChronometer.setBase(base);
        }
        if (isStopwatchRunning()) {
            mChronometer.start();
            // Note: mChronometer.isRunning() will return false at this point and
            // in other upcoming lifecycle methods because it is not yet visible
            // (i.e. mVisible == false).
        }
        // The primary reason we call this is to show the mini FABs after rotate,
        // if the stopwatch is running. If the stopwatch is stopped, then this
        // would have hidden the mini FABs, if not for us already setting its
        // visibility to invisible in XML. We haven't initialized the WeakReference to
        // our Activity's FAB yet, so this call does nothing with the FAB.
        updateMiniFabs();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TOneverDO: Move to onCreate(). When the device rotates, onCreate() _is_ called,
        // but trying to find the FAB in the Activity's layout will fail, and we would get back
        // a null reference. This is probably because this Fragment's onCreate() is called
        // BEFORE the Activity's onCreate.
        // TODO: Any better alternatives to control the Activity's FAB from here?
        mActivityFab = new WeakReference<>((FloatingActionButton) getActivity().findViewById(R.id.fab));
        // There is no documentation for isMenuVisible(), so what exactly does it do?
        // My guess is it checks for the Fragment's options menu. But we never initialize this
        // Fragment with setHasOptionsMenu(), let alone we don't actually inflate a menu in here.
        // My guess is when this Fragment becomes actually visible, it "hooks" onto the menu
        // options "internal API" and inflates its menu in there if it has one.
        //
        // To us, this just makes for a very good visibility check.
        if (savedInstanceState != null && isMenuVisible()) {
            // This is a pretty good indication that we just rotated.
            // isMenuVisible() filters out the case when you rotate on page 1 and scroll
            // to page 2, the icon will prematurely change; that happens because at page 2,
            // this Fragment will be instantiated for the first time for the current configuration,
            // and so the lifecycle from onCreate() to onActivityCreated() occurs. As such,
            // we will have a non-null savedInstanceState and this would call through.
            //
            // The reason when you open up the app for the first time and scrolling to page 2
            // doesn't prematurely change the icon is the savedInstanceState is null, and so
            // this call would be filtered out sufficiently just from the first check.
            updateFab();
        }
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
        // Every view that was in our tree is dereferenced for us.
        // The reason we can control the animator here is because members
        // are not dereferenced here, as evidenced by mStartTime and mPauseTime
        // retaining their values.
        if (mProgressAnimator != null) {
            mProgressAnimator.removeAllListeners();
        }
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
        Log.d(TAG, "onLoadFinished()");
        super.onLoadFinished(loader, data);
        // TODO: Will manipulating the cursor's position here affect the current
        // position in the adapter? Should we make a defensive copy and manipulate
        // that copy instead?
        if (data.moveToFirst()) {
            mCurrentLap = data.getItem();
//            Log.d(TAG, "Current lap ID = " + mCurrentLap.getId());
        }
        if (data.moveToNext()) {
            mPreviousLap = data.getItem();
//            Log.d(TAG, "Previous lap ID = " + mPreviousLap.getId());
        }
        if (mCurrentLap != null && mPreviousLap != null) {
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
            //
            // NOTE: If we just recreated ourselves due to rotation, mChronometer.isRunning() == false,
            // because it is not yet visible (i.e. mVisible == false).
            if (isStopwatchRunning()) {
                startNewProgressBarAnimator();
            } else {
                // I verified the bar was visible already without this, so we probably don't need this,
                // but it's just a safety measure..
                mSeekBar.setVisibility(View.VISIBLE);
                ProgressBarUtils.setProgress(mSeekBar, getCurrentLapProgressRatio());
            }
        } else {
            mSeekBar.setVisibility(View.INVISIBLE);
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
        savePrefs();
        // TOneverDO: Precede savePrefs(), or else we don't save false to KEY_CHRONOMETER_RUNNING
        /// and updateFab will update the wrong icon.
        updateAllFabs();
    }

    @Override
    public void onPageSelected() {
        updateAllFabs();
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
        // ----------------------------------------------------------------------
        // TOneverDO: Precede these with mProgressAnimator.end(), otherwise our
        // Animator.onAnimationEnd() callback won't hide SeekBar in time.
        mStartTime = 0;
        mPauseTime = 0;
        // ----------------------------------------------------------------------
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
        savePrefs();
        // TOneverDO: Precede savePrefs(), or else we don't save false to KEY_CHRONOMETER_RUNNING
        /// and updateFab will update the wrong icon.
        updateAllFabs();
    }

    private void updateAllFabs() {
        updateMiniFabs();
        // TODO: If we're calling this method, then chances are we are visible.
        // You can verify this yourself by finding all usages.
        // isVisible() is good for filtering out calls to this method when this Fragment
        // isn't actually visible to the user; however, a side effect is it also filters
        // out calls to this method when this Fragment is rotated. Fortunately, we don't
        // make any calls to this method after a rotation.
        if (isVisible()) {
            updateFab();
        }
    }

    private void updateMiniFabs() {
        boolean started = mStartTime > 0;
        int vis = started ? View.VISIBLE : View.INVISIBLE;
        mNewLapButton.setVisibility(vis);
        mStopButton.setVisibility(vis);
    }

    private void updateFab() {
        mActivityFab.get().setImageDrawable(isStopwatchRunning() ? mPauseDrawable : mStartDrawable);
    }

    private void startNewProgressBarAnimator() {
        final long timeRemaining = remainingTimeBetweenLaps();
        if (timeRemaining <= 0) {
            mSeekBar.setVisibility(View.INVISIBLE);
            return;
        }
        if (mProgressAnimator != null) {
            mProgressAnimator.end();
        }
        // This can't go in the onAnimationStart() callback because the listener is added
        // AFTER ProgressBarUtils.startNewAnimator() starts the animation.
        mSeekBar.setVisibility(View.VISIBLE);
        mProgressAnimator = ProgressBarUtils.startNewAnimator(
                mSeekBar, getCurrentLapProgressRatio(), timeRemaining);
        mProgressAnimator.addListener(new Animator.AnimatorListener() {
            private boolean cancelled;

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // Pausing the stopwatch (and the current lap) uses Animator.cancel(), which will
                // not only fire onAnimationCancel(Animator), but also onAnimationEnd(Animator).
                // We should only let this call through when actually Animator.end() was called,
                // and that happens when we stop() the stopwatch.
                // If we didn't have this check, we'd be hiding the SeekBar every time we pause
                // a lap.
                if (!cancelled) {
                    mSeekBar.setVisibility(View.INVISIBLE);
                }
                cancelled = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private double getCurrentLapProgressRatio() {
        if (mPreviousLap == null)
            return 0;
        // The cast is necessary, or else we'd have integer division between two longs and we'd
        // always get zero since the numerator will always be less than the denominator.
        return remainingTimeBetweenLaps() / (double) mPreviousLap.elapsed();
    }

    private long remainingTimeBetweenLaps() {
        if (mCurrentLap == null || mPreviousLap == null)
            return 0;
        return mPreviousLap.elapsed() - mCurrentLap.elapsed();
    }

    private void savePrefs() {
        mPrefs.edit().putLong(KEY_START_TIME, mStartTime)
                .putLong(KEY_PAUSE_TIME, mPauseTime)
                .putBoolean(KEY_CHRONOMETER_RUNNING, mChronometer.isRunning())
                .apply();
    }

    /**
     * @return the state of the stopwatch when we're in a resumed and visible state,
     * or when we're going through a rotation
     */
    private boolean isStopwatchRunning() {
        return mChronometer.isRunning() || mPrefs.getBoolean(KEY_CHRONOMETER_RUNNING, false);
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
