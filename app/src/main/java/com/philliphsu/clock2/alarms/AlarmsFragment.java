package com.philliphsu.clock2.alarms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.AsyncAlarmsTableUpdateHandler;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.RecyclerViewFragment;
import com.philliphsu.clock2.editalarm.EditAlarmActivity;
import com.philliphsu.clock2.model.AlarmCursor;
import com.philliphsu.clock2.model.AlarmsListCursorLoader;
import com.philliphsu.clock2.util.AlarmController;
import com.philliphsu.clock2.util.DelayedSnackbarHandler;

public class AlarmsFragment extends RecyclerViewFragment<
        Alarm,
        BaseAlarmViewHolder,
        AlarmCursor,
        AlarmsCursorAdapter> implements ScrollHandler { // TODO: Move interface to base class
    private static final String TAG = "AlarmsFragment";
    private static final int REQUEST_EDIT_ALARM = 0;
    // Public because MainActivity needs to use it.
    // TODO: private because we handle fab clicks in the fragment now
    public static final int REQUEST_CREATE_ALARM = 1;

//    private AlarmsCursorAdapter mAdapter;
    private AsyncAlarmsTableUpdateHandler mAsyncAlarmsTableUpdateHandler;
    private AlarmController mAlarmController;
    private Handler mHandler = new Handler();
    private View mSnackbarAnchor;

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

        // Will succeed because the activity is created at this point.
        // See the Fragment lifecycle.
        mSnackbarAnchor = getActivity().findViewById(R.id.main_content);
        mAlarmController = new AlarmController(getActivity(), mSnackbarAnchor);
        mAsyncAlarmsTableUpdateHandler = new AsyncAlarmsTableUpdateHandler(getActivity(),
                mSnackbarAnchor, this, mAlarmController);
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
    }

    @Override
    public void onFabClick() {
        Intent intent = new Intent(getActivity(), EditAlarmActivity.class);
        startActivityForResult(intent, REQUEST_CREATE_ALARM);
    }

    @Nullable
    @Override
    protected AlarmsCursorAdapter getAdapter() {
        if (super.getAdapter() != null)
            return super.getAdapter();
        // Create a new adapter. This is called before we can initialize mAlarmController,
        // so right now it is null. However, after super.onCreate() returns, it is initialized, and
        // the reference variable will be pointing to an actual object. This assignment "propagates"
        // to all references to mAlarmController.
        return new AlarmsCursorAdapter(this, mAlarmController);
    }

    // TODO: We're not using EditAlarmActivity anymore, so move this logic somewhere else.
    // We also don't need to delay the change to get animations working.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult()");
        if (resultCode != Activity.RESULT_OK || data == null)
            return;
        final Alarm alarm = data.getParcelableExtra(EditAlarmActivity.EXTRA_MODIFIED_ALARM);
        if (alarm == null)
            return;

        // http://stackoverflow.com/a/27055512/5055032
        // "RecyclerView does not run animations in the first layout
        // pass after being attached." A workaround is to postpone
        // the CRUD operation to the next frame. A delay of 300ms is
        // short enough to not be noticeable, and long enough to
        // give us the animation *most of the time*.
        switch (requestCode) {
            case REQUEST_CREATE_ALARM:
                mHandler.postDelayed(
                        new AsyncAddItemRunnable(mAsyncAlarmsTableUpdateHandler, alarm),
                        300);
                break;
            case REQUEST_EDIT_ALARM:
                if (data.getBooleanExtra(EditAlarmActivity.EXTRA_IS_DELETING, false)) {
                    // TODO: Should we delay this too? It seems animations run
                    // some of the time.
                    mAsyncAlarmsTableUpdateHandler.asyncDelete(alarm);
                } else {
                    // TODO: Increase the delay, because update animation is
                    // more elusive than insert.
                    mHandler.postDelayed(
                            new AsyncUpdateItemRunnable(mAsyncAlarmsTableUpdateHandler, alarm),
                            300);
                }
                break;
            default:
                Log.i(TAG, "Could not handle request code " + requestCode);
                break;
        }
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
    // TODO: Just like with TimersCursorAdapter, we could pass in the mAsyncAlarmsTableUpdateHandler
    // to the AlarmsCursorAdapter and call these on the save and delete button click bindings.

    @Override
    // TODO: Rename to onListItem***Delete*** because the item hasn't been deleted from our db yet
    public void onListItemDeleted(final Alarm item) {
        // The corresponding VH will be automatically removed from view following
        // the requery, so we don't have to do anything to it.
        mAsyncAlarmsTableUpdateHandler.asyncDelete(item);
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
        mAsyncAlarmsTableUpdateHandler.asyncUpdate(item.getId(), item);
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
