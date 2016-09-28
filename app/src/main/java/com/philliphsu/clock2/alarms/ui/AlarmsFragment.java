package com.philliphsu.clock2.alarms.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.philliphsu.clock2.R;
import com.philliphsu.clock2.alarms.Alarm;
import com.philliphsu.clock2.alarms.data.AlarmCursor;
import com.philliphsu.clock2.alarms.data.AlarmsListCursorLoader;
import com.philliphsu.clock2.alarms.data.AsyncAlarmsTableUpdateHandler;
import com.philliphsu.clock2.alarms.misc.AlarmController;
import com.philliphsu.clock2.dialogs.TimePickerDialogController;
import com.philliphsu.clock2.list.RecyclerViewFragment;
import com.philliphsu.clock2.timepickers.BaseTimePickerDialog;
import com.philliphsu.clock2.util.DelayedSnackbarHandler;

import static com.philliphsu.clock2.util.FragmentTagUtils.makeTag;

public class AlarmsFragment extends RecyclerViewFragment<Alarm, BaseAlarmViewHolder, AlarmCursor,
        AlarmsCursorAdapter> implements BaseTimePickerDialog.OnTimeSetListener {
    private static final String TAG = "AlarmsFragment";
    private static final String KEY_EXPANDED_POSITION = "expanded_position";
    public static final String EXTRA_SCROLL_TO_ALARM_ID = "com.philliphsu.clock2.alarms.extra.SCROLL_TO_ALARM_ID";

    private AsyncAlarmsTableUpdateHandler mAsyncUpdateHandler;
    private AlarmController mAlarmController;
    private View mSnackbarAnchor;
    private TimePickerDialogController mTimePickerDialogController;

    private int mExpandedPosition = RecyclerView.NO_POSITION;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore the value of the last expanded position here.
            // We cannot tell the adapter to expand this item until onLoadFinished()
            // is called.
            mExpandedPosition = savedInstanceState.getInt(KEY_EXPANDED_POSITION, RecyclerView.NO_POSITION);
        }
        mTimePickerDialogController = new TimePickerDialogController(
                getFragmentManager(), getActivity(), this);
        mTimePickerDialogController.tryRestoreCallback(makeTimePickerDialogTag());

        long scrollToStableId = getActivity().getIntent().getLongExtra(EXTRA_SCROLL_TO_ALARM_ID, -1);
        if (scrollToStableId != -1) {
            setScrollToStableId(scrollToStableId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mSnackbarAnchor = getActivity().findViewById(R.id.main_content);
        mAlarmController = new AlarmController(getActivity(), mSnackbarAnchor);
        mAsyncUpdateHandler = new AsyncAlarmsTableUpdateHandler(getActivity(),
                mSnackbarAnchor, this, mAlarmController);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Show the pending Snackbar, if any, that was prepared for us
        // by another app component.
        DelayedSnackbarHandler.makeAndShow(mSnackbarAnchor);
    }

    @Override
    public Loader<AlarmCursor> onCreateLoader(int id, Bundle args) {
        return new AlarmsListCursorLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<AlarmCursor> loader, AlarmCursor data) {
        super.onLoadFinished(loader, data);
        // TODO: If this was a content change due to an update, verify that
        // we scroll to the updated alarm if its sort order changes.

        // Does nothing If there is no expanded position.
        getAdapter().expand(mExpandedPosition);
        // We shouldn't continue to keep a reference to this, so clear it.
        mExpandedPosition = RecyclerView.NO_POSITION;
    }

    @Override
    public void onFabClick() {
        mTimePickerDialogController.show(0, 0, makeTimePickerDialogTag());
    }

    @Override
    protected AlarmsCursorAdapter onCreateAdapter() {
        return new AlarmsCursorAdapter(this, mAlarmController);
    }

    @Override
    protected int emptyMessage() {
        return R.string.empty_alarms_container;
    }

    @Override
    public void onListItemClick(Alarm item, int position) {
        boolean expanded = getAdapter().expand(position);
        if (!expanded) {
            getAdapter().collapse(position);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // TODO: Just like with TimersCursorAdapter, we could pass in the mAsyncUpdateHandler
    // to the AlarmsCursorAdapter and call these on the save and delete button click bindings.

    @Override
    public void onListItemDeleted(final Alarm item) {
        // The corresponding VH will be automatically removed from view following
        // the requery, so we don't have to do anything to it.
        mAsyncUpdateHandler.asyncDelete(item);
    }

    @Override
    public void onListItemUpdate(Alarm item, int position) {
        // Once we update the relevant row in the db table, the VH will still
        // be in view. While the requery will probably update the values displayed
        // by the VH, the VH remains in its expanded state from before we were
        // called. Tell the adapter reset its expanded position.
        mAsyncUpdateHandler.asyncUpdate(item.getId(), item);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onScrolledToStableId(long id, int position) {
        boolean expanded = getAdapter().expand(position);
        if (!expanded) {
            // Otherwise, it was due to an item update. The VH is expanded
            // at this point, so reset it.
            getAdapter().collapse(position);
        }
    }

    @Override
    public void onTimeSet(ViewGroup viewGroup, int hourOfDay, int minute) {
        // When we request the Builder, default values are provided for us,
        // which is why we don't have to set the ringtone, label, etc.
        Alarm alarm = Alarm.builder()
                .hour(hourOfDay)
                .minutes(minute)
                .build();
        alarm.setEnabled(true);
        mAsyncUpdateHandler.asyncInsert(alarm);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /*
         * From Fragment#onSaveInstanceState():
         *   - This is called "at any time before onDestroy()".
         *   - "This corresponds to Activity.onSaveInstanceState(Bundle) and most of the discussion
         *     there applies here as well".
         * From Activity#onSaveInstanceState():
         *   - "If called, this method will occur before {@link #onStop}
         *     [which follows onPause() in the lifecycle].  There are
         *     no guarantees about whether it will occur before or after {@link #onPause}."
         *
         * isResumed() is true "for the duration of onResume() and onPause()".
         * From the results of a few trials, this never seemed to call through, so i'm assuming
         * isResumed() returned false every time.
         */
        if (/*isResumed() && */getAdapter() != null) {
            // Normally when we scroll far enough away from this Fragment, *its view* will be
            // destroyed, i.e. the maximum point in its lifecycle is onDestroyView(). However,
            // if the configuration changes, onDestroy() is called through, and then this Fragment
            // and all of its members will be destroyed. This is not
            // a problem if the page in which the configuration changed is this page, because
            // the Fragment will be recreated from onCreate() to onResume(), and any
            // member initialization between those points occurs as usual.
            //
            // However, when the page in which the configuration changed
            // is far enough away from this Fragment, there IS a problem. The Fragment
            // *at that page* is recreated, but this Fragment will NOT be; the ViewPager's
            // adapter will not reinstantiate this Fragment because it exceeds the
            // offscreen page limit relative to the initial page in the new configuration.
            //
            // As such, we should only save state if this Fragment's members (i.e. its RecyclerView.Adapter)
            // are not destroyed
            // because that indicates the Fragment is both registered in the adapter AND is within the offscreen
            // page limit, so its members have been initialized (recall that a Fragment in a ViewPager
            // does not actually need to be visible to the user for onCreateView() to onResume() to
            // be called through).
            outState.putInt(KEY_EXPANDED_POSITION, getAdapter().getExpandedPosition());
        }
    }

    private static String makeTimePickerDialogTag() {
        return makeTag(AlarmsFragment.class, R.id.fab);
    }
}
