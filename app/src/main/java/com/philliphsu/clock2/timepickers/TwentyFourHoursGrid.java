package com.philliphsu.clock2.timepickers;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.philliphsu.clock2.R;

/**
 * Created by Phillip Hsu on 8/17/2016.
 */
public class TwentyFourHoursGrid extends NumbersGrid implements View.OnLongClickListener {
    private static final String TAG = "TwentyFourHoursGrid";

    private int mSecondaryTextColor;

    public TwentyFourHoursGrid(Context context) {
        super(context);
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setOnLongClickListener(this);
        }
        mSecondaryTextColor = ContextCompat.getColor(context, R.color.text_color_secondary_light);
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
        final int newVal = valueOf(v);
        setSelection(newVal);
        mSelectionListener.onNumberSelected(newVal);
    }

    @Override
    public boolean onLongClick(View v) {
        TwentyFourHourGridItem item = (TwentyFourHourGridItem) v;
        // Unfortunately, we can't use #valueOf() for this because we want the secondary value.
        int newVal = Integer.parseInt(item.getSecondaryText().toString());
        mSelectionListener.onNumberSelected(newVal);
        // TOneverDO: Call before firing the onNumberSelected() callback, because we want the
        // dialog to advance to the next index WITHOUT seeing the text swapping.
        swapTexts();
        // TOneverDO: Call before swapping texts, because setIndicator() uses the primary TextView.
        setSelection(newVal);
        return true; // Consume the long click
    }

    @Override
    public void setSelection(int value) {
        super.setSelection(value);
        // The value is within [0, 23], but we have only 12 buttons.
        setIndicator(getChildAt(value % 12));
    }

    @Override
    protected void setIndicator(View view) {
        TwentyFourHourGridItem item = (TwentyFourHourGridItem) view;
        super.setIndicator(item.getPrimaryTextView());
    }

    @Override
    void setTheme(Context context, boolean themeDark) {
        mDefaultTextColor = ContextCompat.getColor(context, themeDark?
                R.color.text_color_primary_dark : R.color.text_color_primary_light);
        mSecondaryTextColor = ContextCompat.getColor(context, themeDark?
                R.color.text_color_secondary_dark : R.color.text_color_secondary_light);
        for (int i = 0; i < getChildCount(); i++) {
            TwentyFourHourGridItem item = (TwentyFourHourGridItem) getChildAt(i);
            // TODO: We could move this to the ctor, in the superclass. If so, then this class
            // doesn't need to worry about setting the highlight.
            Utils.setColorControlHighlight(item, mSelectedTextColor/*colorAccent*/);
            // Filter out the current selection.
            if (getSelection() != valueOf(item)) {
                item.getPrimaryTextView().setTextColor(mDefaultTextColor);
                // The indicator can only be set on the primary text, which is why we don't need
                // the secondary text here.
            }
            item.getSecondaryTextView().setTextColor(mSecondaryTextColor);
        }
    }

    public void swapTexts() {
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            ((TwentyFourHourGridItem) v).swapTexts();
        }
    }

    @Override
    protected int valueOf(View button) {
        return Integer.parseInt(((TwentyFourHourGridItem) button).getPrimaryText().toString());
    }
}