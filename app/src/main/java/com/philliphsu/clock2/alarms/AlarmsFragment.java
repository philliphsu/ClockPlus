package com.philliphsu.clock2.alarms;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.OnListItemInteractionListener;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.model.AlarmsRepository;
import com.philliphsu.clock2.model.BaseRepository;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnAlarmInteractionListener}
 * interface.
 */
public class AlarmsFragment extends Fragment implements BaseRepository.DataObserver<Alarm> {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnAlarmInteractionListener mListener;
    private AlarmsRepository mRepo;

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
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarms, container, false);
        ButterKnife.bind(this, view);
        // Set the adapter
        Context context = view.getContext();
        if (mColumnCount <= 1) {
            mList.setLayoutManager(new LinearLayoutManager(context));
        } else {
            mList.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        mAdapter = new AlarmsAdapter(mRepo.getItems(), mListener);
        mList.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        AlarmsRepository.getInstance(getActivity()).saveItems();
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
        mRepo = AlarmsRepository.getInstance(context);
        mRepo.registerDataObserver(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mRepo.unregisterDataObserver();
        mRepo = null;
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
