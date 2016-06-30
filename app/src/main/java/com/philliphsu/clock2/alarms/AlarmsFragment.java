package com.philliphsu.clock2.alarms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.OnListItemInteractionListener;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.editalarm.EditAlarmActivity;
import com.philliphsu.clock2.model.AlarmsListCursorLoader;
import com.philliphsu.clock2.model.DatabaseManager;
import com.philliphsu.clock2.util.AlarmUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

// TODO: Use native fragments since we're targeting API >=19?
// TODO: Use native LoaderCallbacks.
public class AlarmsFragment extends Fragment implements LoaderCallbacks<Cursor>,
        OnListItemInteractionListener<Alarm> {
    private static final int REQUEST_EDIT_ALARM = 0;
    public static final int REQUEST_CREATE_ALARM = 1;
    private static final String TAG = "AlarmsFragment";

    private AlarmsCursorAdapter mCursorAdapter;
    private DatabaseManager mDatabaseManager;

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

        // Initialize the loader to load the list of runs
        getLoaderManager().initLoader(0, null, this);
        mDatabaseManager = DatabaseManager.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarms, container, false);
        ButterKnife.bind(this, view);
        // Set the adapter
        Context context = view.getContext();
        mList.setLayoutManager(new LinearLayoutManager(context));
        mCursorAdapter = new AlarmsCursorAdapter(this);
        mList.setAdapter(mCursorAdapter);
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
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AlarmsListCursorLoader(getActivity());
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        // Called on the main thread after loading is complete
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        // The adapter's current cursor should not be used anymore
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult()");
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_CREATE_ALARM:
                getLoaderManager().restartLoader(0, null, this);
            case REQUEST_EDIT_ALARM:
                if (data != null && data.getBooleanExtra(
                        EditAlarmActivity.EXTRA_ALARM_DELETED, false)) {
                    // TODO: Pass in the old alarm into the intent and access it here?
                    onListItemDeleted(null);
                }
                getLoaderManager().restartLoader(0, null, this);
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

    // TODO: This doesn't need to be defined in the interface.
    // TODO: Rename to showDeletedSnackbar() or something
    @Override
    public void onListItemDeleted(final Alarm item) {
        Snackbar.make(getActivity().findViewById(R.id.main_content),
                getString(R.string.snackbar_item_deleted, "Alarm"),
                Snackbar.LENGTH_LONG) // TODO: not long enough?
                .setAction(R.string.snackbar_undo_item_deleted, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseManager.getInstance(getActivity()).insertAlarm(item);
                        if (item.isEnabled()) {
                            AlarmUtils.scheduleAlarm(getActivity(), item, true);
                        }
                    }
                })
                .show();
    }
}
