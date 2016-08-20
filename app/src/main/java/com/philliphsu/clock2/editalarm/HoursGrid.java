package com.philliphsu.clock2.editalarm;

import android.content.Context;

import com.philliphsu.clock2.R;

/**
 * Created by Phillip Hsu on 8/17/2016.
 */
public class HoursGrid extends NumbersGrid {

    public HoursGrid(Context context) {
        super(context);
    }

    @Override
    protected int contentLayout() {
        return R.layout.content_hours_grid;
    }

    @Override
    protected int indexOfDefaultValue() {
        // This is the index of number 12.
        return getChildCount() - 1;
    }
}
