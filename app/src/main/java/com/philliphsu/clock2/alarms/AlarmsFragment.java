package com.philliphsu.clock2.alarms;

import android.app.Activity;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.AsyncAlarmsTableUpdateHandler;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.RecyclerViewFragment;
import com.philliphsu.clock2.TimePickerDialogController;
import com.philliphsu.clock2.timepickers.BaseTimePickerDialog;
import com.philliphsu.clock2.model.AlarmCursor;
import com.philliphsu.clock2.model.AlarmsListCursorLoader;
import com.philliphsu.clock2.util.AlarmController;
import com.philliphsu.clock2.util.DelayedSnackbarHandler;

import static com.philliphsu.clock2.util.FragmentTagUtils.makeTag;

public class AlarmsFragment extends RecyclerViewFragment<Alarm, BaseAlarmViewHolder, AlarmCursor,
        AlarmsCursorAdapter> implements BaseTimePickerDialog.OnTimeSetListener {
    private static final String TAG = "AlarmsFragment";

    private static final String KEY_EXPANDED_POSITION = "expanded_position";

    // TODO: Delete these constants. We no longer use EditAlarmActivity.
//    @Deprecated
//    private static final int REQUEST_EDIT_ALARM = 0;
//    // Public because MainActivity needs to use it.
//    // TODO: private because we handle fab clicks in the fragment now
//    @Deprecated
//    public static final int REQUEST_CREATE_ALARM = 1;

    // TODO: Delete this. We no longer use the system's ringtone picker.
    public static final int REQUEST_PICK_RINGTONE = 1;
    public static final String EXTRA_SCROLL_TO_ALARM_ID = "com.philliphsu.clock2.alarms.extra.SCROLL_TO_ALARM_ID";

    private AsyncAlarmsTableUpdateHandler mAsyncUpdateHandler;
    private AlarmController mAlarmController;
    // TODO: Delete this. If I recall correctly, this was just used for delaying item animations.
    private Handler mHandler = new Handler();
    private View mSnackbarAnchor;
    private TimePickerDialogController mTimePickerDialogController;

    private int mExpandedPosition = RecyclerView.NO_POSITION;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AlarmsFragment() {}

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static AlarmsFragment newInstance(int columnCount) {
        AlarmsFragment fragment = new AlarmsFragment();
        Bundle args = new Bundle();
        // TODO Put any arguments in bundle
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            // TODO Read arguments
        }

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

    // TODO: We're not using EditAlarmActivity anymore, so move this logic somewhere else.
    // We also don't need to delay the change to get animations working.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult()");
        if (resultCode != Activity.RESULT_OK || data == null)
            return;
        if (requestCode == REQUEST_PICK_RINGTONE) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            Log.d(TAG, "Retrieved ringtone URI: " + uri);
            // TODO: We'll have to create a new Alarm instance with this ringtone value
            // because we don't have a setter method. Alternatively, write an independent
            // SQL update statement updating COLUMN_RINGTONE.
        }

//        final Alarm alarm = data.getParcelableExtra(EditAlarmActivity.EXTRA_MODIFIED_ALARM);
//        if (alarm == null)
//            return;
//
//        // http://stackoverflow.com/a/27055512/5055032
//        // "RecyclerView does not run animations in the first layout
//        // pass after being attached." A workaround is to postpone
//        // the CRUD operation to the next frame. A delay of 300ms is
//        // short enough to not be noticeable, and long enough to
//        // give us the animation *most of the time*.
//        switch (requestCode) {
//            case REQUEST_CREATE_ALARM:
//                mHandler.postDelayed(
//                        new AsyncAddItemRunnable(mAsyncUpdateHandler, alarm),
//                        300);
//                break;
//            case REQUEST_EDIT_ALARM:
//                if (data.getBooleanExtra(EditAlarmActivity.EXTRA_IS_DELETING, false)) {
//                    // TODO: Should we delay this too? It seems animations run
//                    // some of the time.
//                    mAsyncUpdateHandler.asyncDelete(alarm);
//                } else {
//                    // TODO: Increase the delay, because update animation is
//                    // more elusive than insert.
//                    mHandler.postDelayed(
//                            new AsyncUpdateItemRunnable(mAsyncUpdateHandler, alarm),
//                            300);
//                }
//                break;
//            default:
//                Log.i(TAG, "Could not handle request code " + requestCode);
//                break;
//        }
    }

    @Override
    public void onListItemClick(Alarm item, int position) {
//        Intent intent = new Intent(getActivity(), EditAlarmActivity.class);
//        intent.putExtra(EditAlarmActivity.EXTRA_ALARM_ID, item.id());
//        startActivityForResult(intent, REQUEST_EDIT_ALARM);
        boolean expanded = getAdapter().expand(position);
        if (!expanded) {
            getAdapter().collapse(position);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // TODO: Just like with TimersCursorAdapter, we could pass in the mAsyncUpdateHandler
    // to the AlarmsCursorAdapter and call these on the save and delete button click bindings.

    @Override
    // TODO: Rename to onListItem***Delete*** because the item hasn't been deleted from our db yet
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
        // TODO: Implement editing in the expanded VH. Then verify that changes
        // while in that VH are saved and updated after the requery.
//        getAdapter().collapse(position);
        mAsyncUpdateHandler.asyncUpdate(item.getId(), item);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onScrolledToStableId(long id, int position) {
        // We were called because of a requery. If it was due to an insertion,
        // expand the newly added alarm.
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

    /////////////////////////////////////////////////////////////////////////////////////
    // TODO: We won't need these anymore, since we won't handle the db
    // update in onActivityResult() anymore.

    @Deprecated
    private static abstract class BaseAsyncItemChangeRunnable {
        // TODO: Will holding onto this cause a memory leak?
        private final AsyncAlarmsTableUpdateHandler mAsyncAlarmsTableUpdateHandler;
        private final Alarm mAlarm;

        BaseAsyncItemChangeRunnable(AsyncAlarmsTableUpdateHandler asyncAlarmsTableUpdateHandler, Alarm alarm) {
            mAsyncAlarmsTableUpdateHandler = asyncAlarmsTableUpdateHandler;
            mAlarm = alarm;
        }

        void asyncAddAlarm() {
            mAsyncAlarmsTableUpdateHandler.asyncInsert(mAlarm);
        }

        void asyncUpdateAlarm() {
            mAsyncAlarmsTableUpdateHandler.asyncUpdate(mAlarm.getId(), mAlarm);
        }

        void asyncRemoveAlarm() {
            mAsyncAlarmsTableUpdateHandler.asyncDelete(mAlarm);
        }
    }

    private static class AsyncAddItemRunnable extends BaseAsyncItemChangeRunnable implements Runnable {
        AsyncAddItemRunnable(AsyncAlarmsTableUpdateHandler asyncAlarmsTableUpdateHandler, Alarm alarm) {
            super(asyncAlarmsTableUpdateHandler, alarm);
        }

        @Override
        public void run() {
            asyncAddAlarm();
        }
    }

    private static class AsyncUpdateItemRunnable extends BaseAsyncItemChangeRunnable implements Runnable {
        AsyncUpdateItemRunnable(AsyncAlarmsTableUpdateHandler asyncAlarmsTableUpdateHandler, Alarm alarm) {
            super(asyncAlarmsTableUpdateHandler, alarm);
        }

        @Override
        public void run() {
            asyncUpdateAlarm();
        }
    }

    private static class AsyncRemoveItemRunnable extends BaseAsyncItemChangeRunnable implements Runnable {
        AsyncRemoveItemRunnable(AsyncAlarmsTableUpdateHandler asyncAlarmsTableUpdateHandler, Alarm alarm) {
            super(asyncAlarmsTableUpdateHandler, alarm);
        }

        @Override
        public void run() {
            asyncRemoveAlarm();
        }
    }
}
