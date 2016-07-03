package com.philliphsu.clock2.alarms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import com.philliphsu.clock2.OnListItemInteractionListener;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.editalarm.EditAlarmActivity;
import com.philliphsu.clock2.model.AlarmListLoader;
import com.philliphsu.clock2.model.DatabaseManager;
import com.philliphsu.clock2.util.AlarmUtils;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

// TODO: Use native fragments since we're targeting API >=19?
// TODO: Use native LoaderCallbacks.
public class AlarmsFragment extends Fragment implements LoaderCallbacks<List<Alarm>>,
        OnListItemInteractionListener<Alarm> {
    private static final int REQUEST_EDIT_ALARM = 0;
    public static final int REQUEST_CREATE_ALARM = 1;
    private static final String TAG = "AlarmsFragment";

    private AlarmsAdapter mAdapter;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarms, container, false);
        ButterKnife.bind(this, view);
        // Set the adapter
        Context context = view.getContext();
        mList.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = new AlarmsAdapter(Collections.<Alarm>emptyList(), this);
        mList.setAdapter(mAdapter);
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
    public Loader<List<Alarm>> onCreateLoader(int id, Bundle args) {
        return new AlarmListLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Alarm>> loader, List<Alarm> data) {
        mAdapter.replaceData(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Alarm>> loader) {
        // Can't pass in null, because replaceData() will try to add all the elements
        // from the given collection, so we would run into an NPE.
        mAdapter.replaceData(Collections.<Alarm>emptyList());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult()");
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_CREATE_ALARM:
                // TODO: notifyItemInserted?
                getLoaderManager().restartLoader(0, null, this);
            case REQUEST_EDIT_ALARM:
                Alarm deletedAlarm;
                if (data != null && (deletedAlarm = data.getParcelableExtra(
                        EditAlarmActivity.EXTRA_DELETED_ALARM)) != null) {
                    onListItemDeleted(deletedAlarm);
                }
                // TODO: notifyItemRemoved?
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
    // TODO: This needs to prompt a reload of the list.
    @Override
    public void onListItemDeleted(final Alarm item) {
        Snackbar.make(getActivity().findViewById(R.id.main_content),
                getString(R.string.snackbar_item_deleted, "Alarm"),
                Snackbar.LENGTH_LONG) // TODO: not long enough?
                .setAction(R.string.snackbar_undo_item_deleted, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseManager.getInstance(getActivity()).insertAlarm(item);
                        getLoaderManager().restartLoader(0, null, AlarmsFragment.this);
                        if (item.isEnabled()) {
                            AlarmUtils.scheduleAlarm(getActivity(), item, true);
                        }
                    }
                })
                .show();
    }
}
