package com.philliphsu.clock2.editalarm;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;

import com.philliphsu.clock2.R;

/**
 * Created by Phillip Hsu on 8/17/2016.
 */
public class MinutesGrid extends NumbersGrid {
    private static final String TAG = "MinutesGrid";

    private final ImageButton mMinusButton;
    private final ImageButton mPlusButton;

    public MinutesGrid(Context context) {
        super(context);
        mMinusButton = (ImageButton) getChildAt(getChildCount() - 2);
        mPlusButton = (ImageButton) getChildAt(getChildCount() - 1);
        // We're not doing method binding because we don't have IDs set on these buttons.
        mMinusButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = getSelection() - 1;
                if (value < 0)
                    value = 59;
                setIndicator(value);
                setSelection(value);
                mSelectionListener.onNumberSelected(value);
            }
        });
        mPlusButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = getSelection() + 1;
                if (value == 60)
                    value = 0;
                setIndicator(value);
                setSelection(value);
                mSelectionListener.onNumberSelected(value);
            }
        });
    }

    @Override
    protected int contentLayout() {
        return R.layout.content_minutes_grid;
    }

    @Override
    void setTheme(Context context, boolean themeDark) {
        super.setTheme(context, themeDark);
        mMinusButton.setImageResource(themeDark? R.drawable.ic_minus_circle_dark_24dp : R.drawable.ic_minus_circle_24dp);
        mPlusButton.setImageResource(themeDark? R.drawable.ic_add_circle_dark_24dp : R.drawable.ic_add_circle_24dp);
    }

    /**
     * Helper method for minute tuners to set the indicator.
     * @param value the new value set by the minute tuners
     */
    private void setIndicator(int value) {
        clearIndicator();
        if (value % 5 == 0) {
            // The new value is one of the predetermined minute values
            int positionOfValue = value / 5;
            setIndicator(getChildAt(positionOfValue));
        }
    }
}
