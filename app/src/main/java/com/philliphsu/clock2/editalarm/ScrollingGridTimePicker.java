package com.philliphsu.clock2.editalarm;

import android.content.Context;
import android.support.v7.widget.GridLayout;
import android.util.AttributeSet;

/**
 * Created by Phillip Hsu on 7/16/2016.
 */
public class ScrollingGridTimePicker extends GridLayout implements TimePicker {

    public ScrollingGridTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollingGridTimePicker(Context context) {
        super(context);
    }

    @Override
    public int hourOfDay() {
        return 0;
    }

    @Override
    public int minute() {
        return 0;
    }
}
