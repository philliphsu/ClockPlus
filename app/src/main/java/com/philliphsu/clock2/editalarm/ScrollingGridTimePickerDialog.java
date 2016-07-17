package com.philliphsu.clock2.editalarm;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.philliphsu.clock2.R;

import butterknife.Bind;

/**
 * Created by Phillip Hsu on 7/16/2016.
 */
public class ScrollingGridTimePickerDialog extends BaseTimePickerDialog {
    private static final int COLUMNS = 3;

    private ScrollingGridAdapter mAdapter;
    private String[] mValues;
    private boolean mIs24HourMode;

    @Nullable
    @Bind(R.id.main_content) RecyclerView mGrid;

    public static ScrollingGridTimePickerDialog newInstance(TimePicker.OnTimeSetListener callback, boolean is24HourMode) {
        ScrollingGridTimePickerDialog ret = new ScrollingGridTimePickerDialog();
        ret.setOnTimeSetListener(callback);
        ret.initialize(is24HourMode);
        return ret;
    }

    public void initialize(boolean is24HourMode) {
        mIs24HourMode = is24HourMode;
        if (mIs24HourMode) {
            mValues = new String[] {
                    "00", "01", "02", "03", "04", "05", "06", "07",
                    "08", "09", "10", "11", "12", "13", "14", "15",
                    "16", "17", "18", "19", "20", "21", "22", "23"};
        } else {
            mValues = new String[] {
                    "1", "2", "3", "4", "5", "6",
                    "7", "8", "9", "10", "11", "12"};
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        if (mGrid != null) {
            mGrid.setLayoutManager(new GridLayoutManager(view.getContext(), COLUMNS));
            mAdapter = new ScrollingGridAdapter(mValues, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: If on hours, switch dataset to minutes values. Else, do nothing.
                    mAdapter.notifyDataSetChanged();
                }
            });
            mGrid.setAdapter(mAdapter);
        }
        return view;
    }

    @Override
    protected int contentLayout() {
        return R.layout.dialog_time_picker_scrolling_grid;
    }
}
