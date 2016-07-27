package com.philliphsu.clock2.timers;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.philliphsu.clock2.R;
import com.philliphsu.clock2.timers.dummy.DummyContent;

import butterknife.ButterKnife;

/**
 * TODO: Extend from RecyclerViewFragment.
 */
public class TimersFragment extends Fragment {

    public TimersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarms, container, false);
        ButterKnife.bind(this, view);

        RecyclerView rv = ButterKnife.findById(view, R.id.list);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.setAdapter(new TimerAdapter(DummyContent.ITEMS, null));

        return view;
    }

}
