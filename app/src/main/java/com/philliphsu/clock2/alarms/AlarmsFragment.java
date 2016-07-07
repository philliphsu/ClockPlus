package com.philliphsu.clock2.alarms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.AsyncItemChangeHandler;
import com.philliphsu.clock2.OnListItemInteractionListener;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.editalarm.EditAlarmActivity;
import com.philliphsu.clock2.model.AlarmsListCursorLoader;

import butterknife.Bind;
import butterknife.ButterKnife;

// TODO: Use native fragments since we're targeting API >=19?
// TODO: Use native LoaderCallbacks.
public class AlarmsFragment extends Fragment implements LoaderCallbacks<Cursor>,
        OnListItemInteractionListener<Alarm>, ScrollHandler {
    private static final String TAG = "AlarmsFragment";
    private static final int REQUEST_EDIT_ALARM = 0;
    // Public because MainActivity needs to use it.
    public static final int REQUEST_CREATE_ALARM = 1;

    private AlarmsCursorAdapter mAdapter;
    private AsyncItemChangeHandler mAsyncItemChangeHandler;
    private Handler mHandler = new Handler();
    private long mScrollToStableId = RecyclerView.NO_ID;

    @Bind(R.id.list) RecyclerView mList;

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

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarms, container, false);
        ButterKnife.bind(this, view);
        // Set the adapter
        Context context = view.getContext();
        mList.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = new AlarmsCursorAdapter(this);
        mList.setAdapter(mAdapter);

        mAsyncItemChangeHandler = new AsyncItemChangeHandler(getActivity(),
                getActivity().findViewById(R.id.main_content), this);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        // TODO: Do we need to save anything?
//        AlarmsRepository.getInstance(getActivity()).saveItems();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this); // Only for fragments!
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AlarmsListCursorLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        // Scroll to the last modified alarm
        performScrollToStableId();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

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
                        new AsyncAddItemRunnable(mAsyncItemChangeHandler, alarm),
                        300);
                break;
            case REQUEST_EDIT_ALARM:
                if (data.getBooleanExtra(EditAlarmActivity.EXTRA_IS_DELETING, false)) {
                    // TODO: Should we delay this too? It seems animations run
                    // some of the time.
                    mAsyncItemChangeHandler.asyncRemoveAlarm(alarm);
                } else {
                    // TODO: Increase the delay, because update animation is
                    // more elusive than insert.
                    mHandler.postDelayed(
                            new AsyncUpdateItemRunnable(mAsyncItemChangeHandler, alarm),
                            300);
                }
                break;
            default:
                Log.i(TAG, "Could not handle request code " + requestCode);
                break;
        }
    }

    @Override
    public void onListItemClick(Alarm item) {
        Intent intent = new Intent(getActivity(), EditAlarmActivity.class);
        intent.putExtra(EditAlarmActivity.EXTRA_ALARM_ID, item.id());
        startActivityForResult(intent, REQUEST_EDIT_ALARM);
    }

    @Override
    public void setScrollToStableId(long id) {
        mScrollToStableId = id;
    }

    @Override
    public void scrollToPosition(int position) {
        mList.smoothScrollToPosition(position);
    }

    private void performScrollToStableId() {
        if (mScrollToStableId != RecyclerView.NO_ID) {
            int position = -1;
            for (int i = 0; i < mAdapter.getItemCount(); i++) {
                if (mAdapter.getItemId(i) == mScrollToStableId) {
                    position = i;
                    break;
                }
            }
            if (position >= 0) {
                scrollToPosition(position);
            }
        }
        // Reset
        mScrollToStableId = RecyclerView.NO_ID;
    }


    @Deprecated
    @Override
    public void onListItemDeleted(final Alarm item) {
        // TODO: This doesn't need to be defined in the interface.
        // TODO: Delete this method.
    }

    private static abstract class BaseAsyncItemChangeRunnable {
        // TODO: Will holding onto this cause a memory leak?
        private final AsyncItemChangeHandler mAsyncItemChangeHandler;
        private final Alarm mAlarm;

        BaseAsyncItemChangeRunnable(AsyncItemChangeHandler asyncItemChangeHandler, Alarm alarm) {
            mAsyncItemChangeHandler = asyncItemChangeHandler;
            mAlarm = alarm;
        }

        void asyncAddAlarm() {
            mAsyncItemChangeHandler.asyncAddAlarm(mAlarm);
        }

        void asyncUpdateAlarm() {
            mAsyncItemChangeHandler.asyncUpdateAlarm(mAlarm);
        }

        void asyncRemoveAlarm() {
            mAsyncItemChangeHandler.asyncRemoveAlarm(mAlarm);
        }
    }

    private static class AsyncAddItemRunnable extends BaseAsyncItemChangeRunnable implements Runnable {
        AsyncAddItemRunnable(AsyncItemChangeHandler asyncItemChangeHandler, Alarm alarm) {
            super(asyncItemChangeHandler, alarm);
        }

        @Override
        public void run() {
            asyncAddAlarm();
        }
    }

    private static class AsyncUpdateItemRunnable extends BaseAsyncItemChangeRunnable implements Runnable {
        AsyncUpdateItemRunnable(AsyncItemChangeHandler asyncItemChangeHandler, Alarm alarm) {
            super(asyncItemChangeHandler, alarm);
        }

        @Override
        public void run() {
            asyncUpdateAlarm();
        }
    }

    private static class AsyncRemoveItemRunnable extends BaseAsyncItemChangeRunnable implements Runnable {
        AsyncRemoveItemRunnable(AsyncItemChangeHandler asyncItemChangeHandler, Alarm alarm) {
            super(asyncItemChangeHandler, alarm);
        }

        @Override
        public void run() {
            asyncRemoveAlarm();
        }
    }
}
