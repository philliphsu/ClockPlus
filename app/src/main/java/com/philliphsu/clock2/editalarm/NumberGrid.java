package com.philliphsu.clock2.editalarm;

import android.content.Context;
import android.support.v7.widget.GridLayout;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.philliphsu.clock2.R;

/**
 * Created by Phillip Hsu on 7/21/2016.
 */
@Deprecated
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
     * Set the numbers to be displayed in this grid.
     */
    public void setNumbers(int[] numbers) {
        // TODO: This method isn't applicable to the 24 Hour grid.. consider creating a subclass
        // just for 24 hour values? Or just use a regular GridLayout in the dialog's layout
        // as the container for whatever arbitrary children you want?
        // TODO: Depending on the user's clock system, there will be different logic to toggle
        // between "pages". If the user uses 12-hour time, then the same NumberGrid can be reused
        // for both pages, and you can use this method to replace the texts. Otherwise, you have to
        // remove all of the 24-hour value items from this grid and inflate the minutes layout
        // into this grid. Find an elegant solution to implement this logic.
        setNumbers(numbers, false);
    }

    public void setNumbers(int[] numbers, boolean zeroPadSingleDigits) {
        if (numbers != null) {
            int i = 0;
            View child;
            while ((child = getChildAt(i)) instanceof TextView/*TODO: TextViewWithCircularIndicator*/) {
                String s = zeroPadSingleDigits
                        ? String.format("%02d", numbers[i])
                        : String.valueOf(numbers[i]);
                ((TextView) child).setText(s);
                child.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setNumbers(new int[] {0,5,10,15,20,25,30,35,40,45,50,55}, true);
                        inflate(getContext(), R.layout.content_number_grid_minute_tuners, NumberGrid.this);
                    }
                });
                i++;
            }
        }
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
        boolean is24HourMode = DateFormat.is24HourFormat(getContext());
        int layout = is24HourMode
                ? R.layout.content_24h_number_grid
                : R.layout.content_number_grid;
        inflate(getContext(), layout, this);
        if (!is24HourMode) {
            setNumbers(new int[] {1,2,3,4,5,6,7,8,9,10,11,12});
        }
//        ButterKnife.bind(this);
    }
}
