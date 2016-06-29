package com.philliphsu.clock2.alarms;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.OnListItemInteractionListener;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.model.AlarmsListCursorLoader;
import com.philliphsu.clock2.model.BaseRepository;
import com.philliphsu.clock2.model.DatabaseManager;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnAlarmInteractionListener}
 * interface.
 */
// TODO: Use native fragments since we're targeting API >=19?
// TODO: Use native LoaderCallbacks.
public class AlarmsFragment extends Fragment implements
        BaseRepository.DataObserver<Alarm>, LoaderCallbacks<Cursor> {

    private OnAlarmInteractionListener mListener;
    @Deprecated
    private AlarmsAdapter mAdapter;
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
        // TODO: Create a new adapter subclass with constructor that
        // has no dataset param. The Loader will set the Cursor after it
        // has finished loading it.
        mAdapter = new AlarmsAdapter(mDatabaseManager.getAlarms(), mListener);
        mCursorAdapter = new AlarmsCursorAdapter(mListener);
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
        // TODO: Need to refresh the list's adapter for any item changes. Consider doing this in
        // onNewActivity().
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this); // Only for fragments!
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAlarmInteractionListener) {
            mListener = (OnAlarmInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAlarmInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemAdded(Alarm item) {
        mList.smoothScrollToPosition(mAdapter.addItem(item));
    }

    @Override
    public void onItemDeleted(Alarm item) {
        mAdapter.removeItem(item);
        mListener.onListItemDeleted(item);
    }

    @Override
    public void onItemUpdated(Alarm oldItem, Alarm newItem) {
        mAdapter.updateItem(oldItem, newItem);
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnAlarmInteractionListener extends OnListItemInteractionListener<Alarm> {}
}
