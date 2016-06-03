package com.philliphsu.clock2.editalarm;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

/**
 * Couldn't figure out how to draw the numpad over the system keyboard, so
 * the callback interface provided here is of no use to us.
 */
@Deprecated
public class AlarmEditText extends android.support.v7.widget.AppCompatEditText {

    private OnBackPressListener mOnBackPressListener;

    public AlarmEditText(Context context) {
        super(context);
    }

    public AlarmEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlarmEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_UP
                && mOnBackPressListener != null) {
            mOnBackPressListener.onBackPress();
        }
        return super.dispatchKeyEvent(event); // See http://stackoverflow.com/a/5993196/5055032
    }

    public void setOnBackPressListener(OnBackPressListener onBackPressListener) {
        mOnBackPressListener = onBackPressListener;
    }

    public interface OnBackPressListener {
        void onBackPress();
    }
}
