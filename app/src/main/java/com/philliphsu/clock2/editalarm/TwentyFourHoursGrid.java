package com.philliphsu.clock2.editalarm;

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
//        mSecondaryTextColor = Utils.getTextColorFromThemeAttr(context, android.R.attr.textColorSecondary);
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
        super.setIndicator(item.getPrimaryTextView());
    }

    @Override
    void setTheme(Context context, boolean themeDark) {
//        mDefaultTextColor = Utils.getTextColorFromThemeAttr(context, themeDark?
//                // You may think the order should be switched, but this is in fact the correct order.
//                // I'm guessing this is sensitive to the background color?
//                android.R.attr.textColorPrimary : android.R.attr.textColorPrimaryInverse);
        mDefaultTextColor = ContextCompat.getColor(context, themeDark?
                R.color.text_color_primary_dark : R.color.text_color_primary_light);
        // https://www.reddit.com/r/androiddev/comments/2jpeqd/dealing_with_themematerial_vs_themeholo/
        // For pre-21, textColorPrimary == textColorSecondary. Use textColorTertiary instead.
        // For 21 and above, textColorSecondary == textColorTertiary == pre-21 textColorTertiary.
        // Therefore, use textColorTertiary for both.
//        mSecondaryTextColor = Utils.getTextColorFromThemeAttr(context, themeDark?
//                // You may think the order should be switched, but this is in fact the correct order.
//                // I'm guessing this is sensitive to the background color?
//                android.R.attr.textColorSecondary : android.R.attr.textColorSecondaryInverse);
        mSecondaryTextColor = ContextCompat.getColor(context, themeDark?
                R.color.text_color_secondary_dark : R.color.text_color_secondary_light);
        for (int i = 0; i < getChildCount(); i++) {
            TwentyFourHourGridItem item = (TwentyFourHourGridItem) getChildAt(i);
            item.getPrimaryTextView().setTextColor(mDefaultTextColor);
            item.getSecondaryTextView().setTextColor(mSecondaryTextColor);
        }
        // Set the indicator again
        setIndicator(getChildAt(indexOfDefaultValue()));
    }

    public void swapTexts() {
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            ((TwentyFourHourGridItem) v).swapTexts();
        }
    }
}
