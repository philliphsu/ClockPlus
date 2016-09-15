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

    // TODO: See if we can remove these? Since we save prefs in the notif. service.
    private long mStartTime;
    private long mPauseTime;

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
        mStartTime = mPrefs.getLong(KEY_START_TIME, 0);
        mPauseTime = mPrefs.getLong(KEY_PAUSE_TIME, 0);
        Log.d(TAG, "mStartTime = " + mStartTime
                + ", mPauseTime = " + mPauseTime);
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
        setMiniFabsVisible(isStopwatchRunning());
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
        // are not dereferenced here, as evidenced by mStartTime and mPauseTime
        // retaining their values.
        if (mProgressAnimator != null) {
            mProgressAnimator.removeAllListeners();
        }
        Log.d(TAG, "onDestroyView()");
        Log.d(TAG, "mStartTime = " + mStartTime
                + ", mPauseTime = " + mPauseTime);
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
                // ACTUALLY NOT A SAFETY MEASURE!
//                mSeekBar.setVisibility(View.VISIBLE);
                ProgressBarUtils.setProgress(mSeekBar, getCurrentLapProgressRatio(currentLap, previousLap));
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
        if (mStartTime == 0) {
            setMiniFabsVisible(true);
            // Handle the default action, i.e. post the notification for the first time.
            getActivity().startService(serviceIntent);
        }
        serviceIntent.setAction(StopwatchNotificationService.ACTION_START_PAUSE);
        getActivity().startService(serviceIntent);
    }

    @Override
    public void onPageSelected() {
        setMiniFabsVisible(mStartTime > 0);
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

    private void pauseStopwatch() {
        mPauseTime = SystemClock.elapsedRealtime();
        // If the app is currently not visible, then the chronometer was already stopped
        // when we left the app. The next time we resume the app, the chronometer will show
        // the elapsed time as it was when we first left the app.
        // As such, we manually update the text no matter what the app's state is in.
        mChronometer.setBase(mStartTime);
        // If the app is currently not visible, then this will not call through.
        // This is because the Chronometer's visibility changed when we left the app,
        // so updateRunning() was already called to stop the running.
        // stop() will also make a call to updateRunning(), but the running state has not
        // changed from the time we left the app.
        mChronometer.stop();
        // No issues controlling the animator here, because onLoadFinished() can't
        // call through to startNewProgressBarAnimator(), because by that point
        // the chronometer won't be running.
        if (mProgressAnimator != null) {
            // We may as well call cancel(), since our resume() call would be
            // rendered meaningless.
//                mProgressAnimator.pause();
            mProgressAnimator.cancel();
        }
        syncFabIconWithStopwatchState(false);
    }

    private void runStopwatch() {
        mStartTime += SystemClock.elapsedRealtime() - mPauseTime;
        mPauseTime = 0;
        mChronometer.setBase(mStartTime);
        mChronometer.start();
        // This animator instance will end up having end() called on it. When
        // the table update prompts us to requery, onLoadFinished will be called as a result.
        // There, it calls startNewProgressAnimator() to end this animation and starts an
        // entirely new animator instance.
//            if (mProgressAnimator != null) {
//                mProgressAnimator.resume();
//            }
        syncFabIconWithStopwatchState(true);
    }

    private void resetStopwatch() {
        mChronometer.stop();
        mChronometer.setBase(SystemClock.elapsedRealtime());
        // ----------------------------------------------------------------------
        // TOneverDO: Precede these with mProgressAnimator.end(), otherwise our
        // Animator.onAnimationEnd() callback won't hide SeekBar in time.
        mStartTime = 0;
        mPauseTime = 0;
        // ----------------------------------------------------------------------
        // No issues controlling the animator here, because onLoadFinished() can't
        // call through to startNewProgressBarAnimator(), because by that point
        // the chronometer won't be running.
        if (mProgressAnimator != null) {
            mProgressAnimator.end();
        }
        mProgressAnimator = null;
        setMiniFabsVisible(false);
        syncFabIconWithStopwatchState(false);
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
        return previousLap.elapsed() - currentLap.elapsed();
    }

    /**
     * @return the state of the stopwatch when we're in a resumed and visible state,
     * or when we're going through a rotation
     */
    private boolean isStopwatchRunning() {
        return mChronometer.isRunning() || mPrefs.getBoolean(KEY_CHRONOMETER_RUNNING, false);
    }

    private final OnSharedPreferenceChangeListener mPrefChangeListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.d(TAG, "Pref " + key + " changed");
            switch (key) {
                case KEY_CHRONOMETER_RUNNING:
                    // The value of running tells us what state the stopwatch *should be set to*.
                    if (sharedPreferences.getBoolean(key, false)) {
                        Log.d(TAG, "Running stopwatch");
                        runStopwatch();
                    } else {
                        Log.d(TAG, "Pausing stopwatch");
                        // TODO: If your claim that there is no defined order with which this
                        // callback fires is true, then you need to be wary of the following
                        // scenario:
                        // The value of KEY_START_TIME is changed to zero, and happens before the
                        // change to the value of KEY_CHRONOMETER_RUNNING. Therefore, resetStopwatch()
                        // is called, and within that call, mCurrentLap is set to null. When the
                        // value of KEY_CHRONOMETER_RUNNING changes to false sometime later in the future,
                        // pauseStopwatch() is called, and within that call, mCurrentLap.pause() is
                        // called--this will result in a NPE.
                        //
                        // However, it appears the change to the value of KEY_CHRONOMETER_RUNNING
                        // always happens first. So pauseStopwatch() is called first, and then
                        // resetStopwatch() follows shortly after. This is acceptable and produces
                        // the desired result.
                        pauseStopwatch();
                    }
                    break;
                case KEY_START_TIME:
                    // This is the best indication that the stopwatch should be stopped.
                    // We shouldn't depend on the value of KEY_CHRONOMETER_RUNNING, because
                    // the change to that value may not have occurred before this point;
                    // we cannot rely on there being a defined order with which this callback fires.
                    if (sharedPreferences.getLong(key, 0) == 0) {
                        Log.d(TAG, "Resetting stopwatch");
                        resetStopwatch();
                    }
                    break;
                case KEY_PAUSE_TIME:
                    // In terms of the UI, we don't need to react to a change to the pause time.
                    // A change in the pause time is associated with the stopwatch changing its
                    // running state; as such, the UI can be handled when we react
                    // to a change to the value of KEY_CHRONOMETER_RUNNING.
                    break;
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
