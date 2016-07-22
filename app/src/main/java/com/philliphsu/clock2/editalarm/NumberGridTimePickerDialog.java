package com.philliphsu.clock2.editalarm;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayout;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.philliphsu.clock2.R;

import butterknife.Bind;

/**
 * Created by Phillip Hsu on 7/21/2016.
 */
// TODO: COnsider renaming to GridTimePickerDialog
public class NumberGridTimePickerDialog extends BaseTimePickerDialog {
    private static final String TAG = "GridTimePickerDialog"; // cannot be more than 23 chars...
    private static final int[] HOURS_12 = {1,2,3,4,5,6,7,8,9,10,11,12};
    private static final int[] HOURS_24_HALF_DAY_1 = {0,1,2,3,4,5,6,7,8,9,10,11};
    private static final int[] HOURS_24_HALF_DAY_2 = {12,13,14,15,16,17,18,19,20,21,22,23};
    private static final int[] MINUTES = {0,5,10,15,20,25,30,35,40,45,50,55};

    public static final int INDEX_HOURS = 0;
    public static final int INDEX_MINUTES = 1;

    // TODO: Private?
    // Describes both AM/PM in the 12-hour clock and the half-days of the 24-hour clock.
    public static final int HALF_DAY_1 = 1;
    public static final int HALF_DAY_2 = 2;

    private int mCurrentIndex = INDEX_HOURS;
    private boolean mIs24HourMode;
    private int mSelectedHalfDay = HALF_DAY_1;

    @Bind(R.id.grid_layout) GridLayout mGridLayout;

    /**
     * @param timeFieldIndex The index representing the time field whose values, ranging from its natural
     *                       lower and upper limits, will be presented as choices in the GridLayout
     *                       contained in this dialog's layout. Must be one of {@link #INDEX_HOURS}
     *                       or {@link #INDEX_MINUTES}.
     */
    // TODO: halfDay param
    public static NumberGridTimePickerDialog newInstance(int timeFieldIndex) {
        NumberGridTimePickerDialog dialog = new NumberGridTimePickerDialog();
        dialog.mCurrentIndex = timeFieldIndex;
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // The Activity is created at this point
        mIs24HourMode = DateFormat.is24HourFormat(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the child views into the grid
        View.inflate(getActivity(),
                mIs24HourMode ? R.layout.content_24h_number_grid : R.layout.content_number_grid,
                mGridLayout);
        // Set the appropriate texts on each view
        for (int i = 0; i < mGridLayout.getChildCount(); i++) {
            View v = mGridLayout.getChildAt(i);
            if (mIs24HourMode) {
                TwentyFourHourGridItem item = (TwentyFourHourGridItem) v;
                String s1 = String.format("%02d", HOURS_24_HALF_DAY_1[i]);
                String s2 = String.valueOf(HOURS_24_HALF_DAY_2[i]);
                if (mSelectedHalfDay == HALF_DAY_1) {
                    item.setPrimaryText(s1);
                    item.setSecondaryText(s2);
                } else if (mSelectedHalfDay == HALF_DAY_2) {
                    item.setPrimaryText(s2);
                    item.setSecondaryText(s1);
                } else {
                    Log.e(TAG, "mSelectedHalfDay = " + mSelectedHalfDay + "?");
                }
            } else {
                TextView tv = (TextView) v;
                if (mCurrentIndex == INDEX_HOURS) {
                    tv.setText(String.valueOf(HOURS_12[i]));
                } else if (mCurrentIndex == INDEX_MINUTES) {
                    tv.setText(String.format("%02d", MINUTES[i]));
                } else {
                    Log.e(TAG, "mCurrentIndex = " + mCurrentIndex + "?");
                }
            }
        }

        if (mCurrentIndex == INDEX_MINUTES) {
            // Add the minute tuner buttons as well
            View.inflate(getActivity(), R.layout.content_number_grid_minute_tuners, mGridLayout);
        }

        return view;
    }

    @Override
    protected int contentLayout() {
        return R.layout.dialog_time_picker_number_grid;
    }
}
