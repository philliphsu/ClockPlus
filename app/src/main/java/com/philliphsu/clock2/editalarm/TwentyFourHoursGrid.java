package com.philliphsu.clock2.editalarm;

import android.content.Context;
import android.view.View;

import com.philliphsu.clock2.R;

/**
 * Created by Phillip Hsu on 8/17/2016.
 */
public class TwentyFourHoursGrid extends NumbersGrid implements View.OnLongClickListener {
    private static final String TAG = "TwentyFourHoursGrid";

    public TwentyFourHoursGrid(Context context) {
        super(context);
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setOnLongClickListener(this);
        }
    }

    @Override
    protected int contentLayout() {
        return R.layout.content_24h_number_grid;
    }

    @Override
    protected boolean canRegisterClickListener(View view) {
        return view instanceof TwentyFourHourGridItem;
    }

    @Override
    public void onClick(View v) {
        // We already verified that this view can have a click listener registered on.
        // See canRegisterClickListener().
        TwentyFourHourGridItem item = (TwentyFourHourGridItem) v;
        clearIndicator();
        setIndicator(v);
        int newVal = Integer.parseInt(item.getPrimaryText().toString());
        setSelection(newVal);
        mSelectionListener.onNumberSelected(newVal);
    }

    @Override
    public boolean onLongClick(View v) {
        TwentyFourHourGridItem item = (TwentyFourHourGridItem) v;
        int newVal = Integer.parseInt(item.getSecondaryText().toString());
        setSelection(newVal);
        mSelectionListener.onNumberSelected(newVal);
        clearIndicator();
        swapTexts();
        // TOneverDO: Call before swapping texts, because setIndicator() uses the primary TextView.
        setIndicator(v);
        return true; // Consume the long click
    }

    @Override
    protected void setIndicator(View view) {
        TwentyFourHourGridItem item = (TwentyFourHourGridItem) view;
        // Set indicator on the primary TextView
        super.setIndicator(item.getChildAt(0));
    }

    public void swapTexts() {
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            ((TwentyFourHourGridItem) v).swapTexts();
        }
    }
}
