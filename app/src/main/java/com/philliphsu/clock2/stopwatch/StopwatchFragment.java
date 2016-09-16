package com.philliphsu.clock2.stopwatch;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
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

    // Exposed for StopwatchNotificationService
    static final String KEY_START_TIME = "start_time";
    static final String KEY_PAUSE_TIME = "pause_time";
    static final String KEY_CHRONOMETER_RUNNING = "chronometer_running";

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
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        // TODO: Will these be kept alive after onDestroyView()? If not, we should move these to
        // onCreateView() or any other callback that is guaranteed to be called.
        mStartDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.ic_start_24dp);
        mPauseDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.ic_pause_24dp);

        // TODO: Load the current lap here
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mChronometer.setShowCentiseconds(true, true);
        long startTime = getLongFromPref(KEY_START_TIME);
        long pauseTime = getLongFromPref(KEY_PAUSE_TIME);
        // If we have a nonzero startTime from a previous session, restore it as
        // the chronometer's base. Otherwise, leave the default base.
        if (startTime > 0) {
            if (pauseTime > 0) {
                startTime += SystemClock.elapsedRealtime() - pauseTime;
            }
            mChronometer.setBase(startTime);
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
        setMiniFabsVisible(startTime > 0);
        mPrefs.registerOnSharedPreferenceChangeListener(mPrefChangeListener);
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
            syncFabIconWithStopwatchState(isStopwatchRunning());
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
        // are not dereferenced here.
        if (mProgressAnimator != null) {
            mProgressAnimator.removeAllListeners();
        }
        Log.d(TAG, "onDestroyView()");
        mPrefs.unregisterOnSharedPreferenceChangeListener(mPrefChangeListener);
    }

    @Override
    protected boolean hasEmptyView() {
        return false;
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
        Lap currentLap = null;
        Lap previousLap = null;
        if (data.moveToFirst()) {
            currentLap = data.getItem();
//            Log.d(TAG, "Current lap ID = " + mCurrentLap.getId());
        }
        if (data.moveToNext()) {
            previousLap = data.getItem();
//            Log.d(TAG, "Previous lap ID = " + mPreviousLap.getId());
        }
        if (currentLap != null && previousLap != null) {
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
                startNewProgressBarAnimator(currentLap, previousLap);
            } else {
                // I verified the bar was visible already without this, so we probably don't need this,
                // but it's just a safety measure..
                // ACTUALLY NOT A SAFETY MEASURE! TODO: Why was this not acceptable?
//                mSeekBar.setVisibility(View.VISIBLE);
                double ratio = getCurrentLapProgressRatio(currentLap, previousLap);
                if (ratio > 0d) {
                    // TODO: To be consistent with the else case, we could set the visibility
                    // to VISIBLE if we cared.
                    ProgressBarUtils.setProgress(mSeekBar, ratio);
                } else {
                    mSeekBar.setVisibility(View.INVISIBLE);
                }
            }
        } else {
            mSeekBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onFabClick() {
        final boolean running = mChronometer.isRunning();
        syncFabIconWithStopwatchState(!running/*invert the current state*/);

        final Intent serviceIntent = new Intent(getActivity(), StopwatchNotificationService.class);
        if (getLongFromPref(KEY_START_TIME) == 0) {
            setMiniFabsVisible(true);
            // Handle the default action, i.e. post the notification for the first time.
            getActivity().startService(serviceIntent);
        }
        serviceIntent.setAction(StopwatchNotificationService.ACTION_START_PAUSE);
        getActivity().startService(serviceIntent);
    }

    @Override
    public void onPageSelected() {
        setMiniFabsVisible(getLongFromPref(KEY_START_TIME) > 0);
        syncFabIconWithStopwatchState(isStopwatchRunning());
    }

    @Override
    protected LapsAdapter onCreateAdapter() {
        return new LapsAdapter();
    }

    @Override
    protected int contentLayout() {
        return R.layout.fragment_stopwatch;
    }

    @OnClick(R.id.new_lap)
    void addNewLap() {
        Intent serviceIntent = new Intent(getActivity(), StopwatchNotificationService.class)
                .setAction(StopwatchNotificationService.ACTION_ADD_LAP);
        getActivity().startService(serviceIntent);
    }

    @OnClick(R.id.stop)
    void stop() {
        // Remove the notification. This will also write to prefs and clear the laps table.
        Intent stop = new Intent(getActivity(), StopwatchNotificationService.class)
                .setAction(StopwatchNotificationService.ACTION_STOP);
        getActivity().startService(stop);
    }

    private void setMiniFabsVisible(boolean visible) {
        int vis = visible ? View.VISIBLE : View.INVISIBLE;
        mNewLapButton.setVisibility(vis);
        mStopButton.setVisibility(vis);
    }

    private void syncFabIconWithStopwatchState(boolean running) {
        mActivityFab.get().setImageDrawable(running ? mPauseDrawable : mStartDrawable);
    }

    private void startNewProgressBarAnimator(Lap currentLap, Lap previousLap) {
        final long timeRemaining = remainingTimeBetweenLaps(currentLap, previousLap);
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
        mProgressAnimator = ProgressBarUtils.startNewAnimator(mSeekBar,
                getCurrentLapProgressRatio(currentLap, previousLap), timeRemaining);
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

    private double getCurrentLapProgressRatio(Lap currentLap, Lap previousLap) {
        if (previousLap == null)
            return 0;
        // The cast is necessary, or else we'd have integer division between two longs and we'd
        // always get zero since the numerator will always be less than the denominator.
        return remainingTimeBetweenLaps(currentLap, previousLap) / (double) previousLap.elapsed();
    }

    private long remainingTimeBetweenLaps(Lap currentLap, Lap previousLap) {
        if (currentLap == null || previousLap == null)
            return 0;
        // TODO: Should we check if the subtraction results in negative number, and return 0?
        return previousLap.elapsed() - currentLap.elapsed();
    }

    /**
     * @return the state of the stopwatch when we're in a resumed and visible state,
     * or when we're going through a rotation
     */
    private boolean isStopwatchRunning() {
        return mChronometer.isRunning() || mPrefs.getBoolean(KEY_CHRONOMETER_RUNNING, false);
    }

    private long getLongFromPref(String key) {
        return mPrefs.getLong(key, 0);
    }

    private final OnSharedPreferenceChangeListener mPrefChangeListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            // We don't care what key-value pair actually changed, just configure all the views again.
            long startTime = sharedPreferences.getLong(KEY_START_TIME, 0);
            long pauseTime = sharedPreferences.getLong(KEY_PAUSE_TIME, 0);
            boolean running = sharedPreferences.getBoolean(KEY_CHRONOMETER_RUNNING, false);
            setMiniFabsVisible(startTime > 0);
            syncFabIconWithStopwatchState(running);
            // ==================================================
            // TOneverDO: Precede setMiniFabsVisible()
            if (startTime == 0) {
                startTime = SystemClock.elapsedRealtime();
            }
            // ==================================================

            // If we're resuming, the pause duration is already added to the startTime.
            // If we're pausing, then the chronometer will be stopped and we can use
            // the startTime that was originally set the last time we were running.
            //
            // We don't need to add the pause duration if we're pausing because it's going to
            // be negligible at this point.
//            if (pauseTime > 0) {
//                startTime += SystemClock.elapsedRealtime() - pauseTime;
//            }
            mChronometer.setBase(startTime);
            mChronometer.setStarted(running);
            // Starting an instance of Animator is not the responsibility of this method,
            // but is of onLoadFinished().
            if (mProgressAnimator != null && !running) {
                // Wait until both values have been notified of being reset.
                if (startTime == 0 && pauseTime == 0) {
                    mProgressAnimator.end();
                } else {
                    mProgressAnimator.cancel();
                }
            }
        }
    };

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
