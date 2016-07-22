package com.philliphsu.clock2.editalarm;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.philliphsu.clock2.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Phillip Hsu on 7/21/2016.
 */
public class TwentyFourHourGridItem extends LinearLayout {

    @Bind(R.id.primary) TextView mPrimaryText;
    @Bind(R.id.secondary) TextView mSecondaryText;

    public TwentyFourHourGridItem(Context context) {
        super(context);
        init();
    }

    public TwentyFourHourGridItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.TwentyFourHourGridItem, 0, 0);
        try {
            setPrimaryText(a.getString(R.styleable.TwentyFourHourGridItem_primaryText));
            setSecondaryText(a.getString(R.styleable.TwentyFourHourGridItem_secondaryText));
        } finally {
            a.recycle();
        }
    }

    public void setPrimaryText(CharSequence text) {
        mPrimaryText.setText(text);
    }

    public void setSecondaryText(CharSequence text) {
        mSecondaryText.setText(text);
    }

    public void swapTexts() {
        CharSequence primary = mPrimaryText.getText();
        setPrimaryText(mSecondaryText.getText());
        setSecondaryText(primary);
    }

    private void init() {
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        inflate(getContext(), R.layout.element_24h_number_grid, this);
        ButterKnife.bind(this);
    }
}
