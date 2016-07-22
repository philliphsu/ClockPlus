package com.philliphsu.clock2.editalarm;

import android.content.Context;
import android.support.v7.widget.GridLayout;
import android.text.format.DateFormat;
import android.util.AttributeSet;

import com.philliphsu.clock2.R;

/**
 * Created by Phillip Hsu on 7/21/2016.
 */
public class NumberGrid extends GridLayout {
    private static final String TAG = "NumberGrid";
    private static final int COLUMNS = 3;

//    private CircularIndicatorSetter mIndicatorSetter;
    private OnNumberSelectedListener mSelectionListener;
    // TODO: Since we plan to dynamically clear and inflate children into this
    // parent to represent "pages", this seems useless? Since each page has at
    // most one selection, and at any given time, this GridLayout can only show
    // one page at a time. Instead, when the onNumberSelected() is fired, the
    // hosting dialog should keep a reference to the returned number. It is up
    // to the dialog to deduce what time field the number represents, probably
    // with an int flag that indicates what "page" this GridLayout is displaying.
    private int mSelection;

    public interface OnNumberSelectedListener {
        void onNumberSelected(int number);
    }

    public NumberGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NumberGrid(Context context) {
        super(context);
        init();
    }

    public void setOnNumberSelectedListener(OnNumberSelectedListener onNumberSelectedListener) {
        mSelectionListener = onNumberSelectedListener;
    }

//    /*package*/ void setTheme(Context context, boolean themeDark) {
//        for (int i = 0; i < getChildCount(); i++) {
//            View view = getChildAt(i);
//            if (view instanceof TextViewWithCircularIndicator) {
//                TextViewWithCircularIndicator text = (TextViewWithCircularIndicator) getChildAt(i);
//                text.setTheme(context, themeDark);
//            }
//        }
//    }

    public int getSelection() {
        return mSelection;
    }

    public void setSelection(int value) {
        mSelection = value;
//        for (int i = 0; i < getChildCount(); i++) {
//            View v = getChildAt(i);
//            if (v instanceof TextViewWithCircularIndicator) {
//                TextViewWithCircularIndicator text = (TextViewWithCircularIndicator) v;
//                // parseInt() strips out leading zeroes
//                int num = Integer.parseInt(text.getText().toString());
//                if (value == num) {
//                    mIndicatorSetter.setIndicator(text);
//                    break;
//                }
//            } else {
//                // We have reached a non-numeric button, i.e. the minute tuners, unless you have
//                // other non-numeric buttons as well. This means we iterated through all numeric
//                // buttons, but this value is not one of the preset values.
//                // Clear the indicator from the default selection.
//                mIndicatorSetter.setIndicator(null);
//                break;
//            }
//        }
    }

    /**
     * Final because this is implemented for the grid of numbers. If subclasses need their own
     * click listeners for non-numeric buttons, they should set new OnClickListeners on those buttons.
     */
//    @Override
//    public final void onClick(View v) {
//        TextViewWithCircularIndicator view = (TextViewWithCircularIndicator) v;
//        String text = view.getText().toString();
//        int number = Integer.parseInt(text);
//        mSelection = number;
//        fireOnNumberSelectedEvent(number);
//        mIndicatorSetter.setIndicator(view);
//    }

    protected void fireOnNumberSelectedEvent(int number) {
        if (mSelectionListener != null)
            mSelectionListener.onNumberSelected(number);
    }

    private void init() {
//        setAlignmentMode(ALIGN_BOUNDS);
        setColumnCount(COLUMNS);
        // When we initialize, display the hour values "page".
        int layout = DateFormat.is24HourFormat(getContext())
                ? R.layout.content_24h_number_grid
                : R.layout.content_12h_number_grid;
        inflate(getContext(), layout, this);
//        ButterKnife.bind(this);
    }
}
