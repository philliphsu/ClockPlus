package com.philliphsu.clock2.editalarm;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.v7.widget.GridLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.philliphsu.clock2.R;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by Phillip Hsu on 7/12/2016.
 *
 * Successor to the Numpad class that was based on TableLayout.
 * Unlike Numpad, this class only manages the logic for number button clicks
 * and not the backspace button. However, we do provide an API for removing
 * digits from the input.
 */
public abstract class GridLayoutNumpad extends GridLayout {
    // TODO: change to private?
    protected static final int UNMODIFIED = -1;
    private static final int COLUMNS = 3;

    private int[] mInput;
    private int mCount = 0;
    private OnInputChangeListener mOnInputChangeListener;

    @Bind({ R.id.zero, R.id.one, R.id.two, R.id.three, R.id.four,
            R.id.five, R.id.six, R.id.seven, R.id.eight, R.id.nine })
    TextView[] mButtons;
    @Bind(R.id.backspace) ImageButton mBackspace;

    /**
     * Informs clients how to output the digits inputted into this numpad.
     */
    public interface OnInputChangeListener {
        /**
         * @param newStr the new value of the input formatted as a
         *               String after a digit insertion
         */
        void onDigitInserted(String newStr);
        /**
         * @param newStr the new value of the input formatted as a
         *               String after a digit deletion
         */
        void onDigitDeleted(String newStr);
        void onDigitsCleared();
    }

    public GridLayoutNumpad(Context context) {
        super(context);
        init();
    }

    public GridLayoutNumpad(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * @return the number of digits we can input
     */
    public abstract int capacity();

    public final void setOnInputChangeListener(OnInputChangeListener onInputChangeListener) {
        mOnInputChangeListener = onInputChangeListener;
    }

    /**
     * Provided only for subclasses so they can retrieve the registered listener
     * and fire any custom OnInputChange events they may have defined.
     */
    protected final OnInputChangeListener getOnInputChangeListener() {
        return mOnInputChangeListener;
    }

    @CallSuper
    protected void enable(int lowerLimitInclusive, int upperLimitExclusive) {
        if (lowerLimitInclusive < 0 || upperLimitExclusive > mButtons.length)
            throw new IndexOutOfBoundsException("Upper limit out of range");

        for (int i = 0; i < mButtons.length; i++)
            mButtons[i].setEnabled(i >= lowerLimitInclusive && i < upperLimitExclusive);
    }

    protected final void setBackspaceEnabled(boolean enabled) {
        mBackspace.setEnabled(enabled);
    }

    protected final int valueAt(int index) {
        return mInput[index];
    }

    /**
     * @return a defensive copy of the internal array of inputted digits
     */
    protected final int[] getDigits() {
        int[] digits = new int[mInput.length];
        System.arraycopy(mInput, 0, digits, 0, mInput.length);
        return digits;
    }

    /**
     * @return the number of digits inputted
     */
    public final int count() {
        return mCount;
    }

    /**
     * @return the integer represented by the inputted digits
     */
    protected final int getInput() {
        return Integer.parseInt(getInputString());
    }

    private String getInputString() {
        String currentInput = "";
        for (int i : mInput)
            if (i != UNMODIFIED)
                currentInput += i;
        return currentInput;
    }

    @OnClick(R.id.backspace)
    public void delete() {
        /*
        if (mCount - 1 >= 0) {
            mInput[--mCount] = UNMODIFIED;
        }
        onDigitDeleted(getInputString());
        */
        delete(mCount);
    }

    // TODO: Why do we need this?
    @Deprecated
    public void delete(int at) {
        if (at - 1 >= 0) {
            mInput[at - 1] = UNMODIFIED;
            mCount--;
            onDigitDeleted(getInputString());
        }
    }

    @OnLongClick(R.id.backspace)
    public boolean clear() {
        Arrays.fill(mInput, UNMODIFIED);
        mCount = 0;
        onDigitsCleared();
        return true;
    }

    /**
     * Forwards the provided String to the assigned
     * {@link OnInputChangeListener OnInputChangeListener}
     * after a digit insertion. By default, the String
     * forwarded is just the String value of the inserted digit.
     * @see #onClick(TextView)
     * @param newDigit the formatted String that should be displayed
     */
    @CallSuper
    protected void onDigitInserted(String newDigit) {
        if (mOnInputChangeListener != null) {
            mOnInputChangeListener.onDigitInserted(newDigit);
        }
    }

    /**
     * Forwards the provided String to the assigned
     * {@link OnInputChangeListener OnInputChangeListener}
     * after a digit deletion. By default, the String
     * forwarded is {@link #getInputString()}.
     * @param newStr the formatted String that should be displayed
     */
    @CallSuper
    protected void onDigitDeleted(String newStr) {
        if (mOnInputChangeListener != null) {
            mOnInputChangeListener.onDigitDeleted(newStr);
        }
    }

    /**
     * Forwards a {@code onDigitsCleared()} event to the assigned
     * {@link OnInputChangeListener OnInputChangeListener}.
     */
    @CallSuper
    protected void onDigitsCleared() {
        if (mOnInputChangeListener != null) {
            mOnInputChangeListener.onDigitsCleared();
        }
    }

    /**
     * Inserts as many of the digits in the given sequence
     * into the input as possible. At the end, if any digits
     * were inserted, this calls {@link #onDigitInserted(String)}
     * with the String value of those digits.
     */
    protected final void insertDigits(int... digits) {
        if (digits == null)
            return;
        String newDigits = "";
        for (int d : digits) {
            if (mCount == mInput.length)
                break;
            if (d == UNMODIFIED)
                continue;
            mInput[mCount++] = d;
            newDigits += d;
        }
        if (!newDigits.isEmpty()) {
            // By only calling this once after making
            // the insertions, we skip all of the
            // intermediate callbacks.
            onDigitInserted(newDigits);
        }
    }

    @OnClick({ R.id.zero, R.id.one, R.id.two, R.id.three, R.id.four,
            R.id.five, R.id.six, R.id.seven, R.id.eight, R.id.nine })
    final void onClick(TextView view) {
        if (mCount < mInput.length) {
            String textNum = view.getText().toString();
            insertDigits(Integer.parseInt(textNum));
        }
    }

    private void init() {
        setAlignmentMode(ALIGN_BOUNDS);
        setColumnCount(COLUMNS);
        View.inflate(getContext(), R.layout.content_grid_layout_numpad, this);
        ButterKnife.bind(this);
        // If capacity() < 0, we let the system throw the exception.
        mInput = new int[capacity()];
        Arrays.fill(mInput, UNMODIFIED);
    }
}
