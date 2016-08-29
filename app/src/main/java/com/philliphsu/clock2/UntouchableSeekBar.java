package com.philliphsu.clock2;

import android.content.Context;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * A SeekBar that cannot be touch controlled.
 */
public class UntouchableSeekBar extends AppCompatSeekBar {

    public UntouchableSeekBar(Context context) {
        super(context);
    }

    public UntouchableSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UntouchableSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public final boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}
