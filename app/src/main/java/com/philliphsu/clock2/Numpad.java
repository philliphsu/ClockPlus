package com.philliphsu.clock2;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Phillip Hsu on 6/2/2016.
 */
public abstract class Numpad extends TableLayout {
    private static final String TAG = "Numpad";
    private static final int NUM_COLUMNS = 3;
    private static final int RADIX_10 = 10;
    protected static final int UNMODIFIED = -1;

    // Derived classes need to build this themselves via buildBackspace().
    private ImageButton mBackspace;
    private ImageButton mCollapse;
    private int[] mInput;
    private int mCount = 0;
    private KeyListener mKeyListener;

    @Bind({ R.id.zero, R.id.one, R.id.two, R.id.three, R.id.four,
            R.id.five, R.id.six, R.id.seven, R.id.eight, R.id.nine })
    Button[] mButtons;

    public interface KeyListener {
        void onNumberInput(String number);
        void onCollapse();
        void onBackspace(String newStr);
        void onLongBackspace();
    }

    public Numpad(Context context) {
        super(context);
        init();
    }

    public Numpad(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setKeyListener(@NonNull KeyListener listener) {
        mKeyListener = listener;
    }

    protected KeyListener getKeyListener() {
        return mKeyListener;
    }
    
    protected abstract int capacity();

    protected final void enable(int lowerLimitInclusive, int upperLimitExclusive) {
        if (lowerLimitInclusive < 0 || upperLimitExclusive > mButtons.length)
            throw new IndexOutOfBoundsException("Upper limit out of range");

        for (int i = 0; i < mButtons.length; i++)
            mButtons[i].setEnabled(i >= lowerLimitInclusive && i < upperLimitExclusive);
    }

    protected final int valueAt(int index) {
        checkIndexWithinBounds(index);
        return mInput[index];
    }

    protected final int count() {
        return mCount;
    }

    protected final int getInput() {
        String currentInput = "";
        for (int i : mInput) 
            if (i != UNMODIFIED) 
                currentInput += i;
        return Integer.parseInt(currentInput);
    }

    protected final void buildBackspace(int r, int c) {
        mBackspace = (ImageButton) buildButton(R.layout.numpad_backspace, r, c);

        mBackspace.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                backspace();
            }
        });

        mBackspace.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return longBackspace();
            }
        });
    }

    protected final void buildCollapse(int r, int c) {
        mCollapse = (ImageButton) buildButton(R.layout.numpad_collapse, r, c);

        mCollapse.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkKeyListenerSet();
                mKeyListener.onCollapse();
            }
        });
    }

    protected final void newRow() {
        TableRow newRow = new TableRow(getContext());
        newRow.setLayoutParams(new TableRow.LayoutParams());
        addView(newRow);
        for (int i = 0; i < NUM_COLUMNS; i++) {
            Space s = new Space(getContext());
            setButtonLayoutParams(s);
            newRow.addView(s);
        }
    }

    protected final View buildButton(@LayoutRes int buttonRes, int r, int c) {
        View button = View.inflate(getContext(), buttonRes, null);
        setButtonLayoutParams(button);
        replace(r, c, button);
        return button;
    }

    @OnClick({ R.id.zero, R.id.one, R.id.two, R.id.three, R.id.four,
            R.id.five, R.id.six, R.id.seven, R.id.eight, R.id.nine })
    protected void onClick(Button button) {
        onClick(button, mCount);
    }

    public void onClick(Button button, int at) {
        if (mCount < mInput.length) {
            checkIndexWithinBounds(at);
            mInput[at] = Integer.parseInt(button.getText().toString());
            mCount++;
        }
    }

    /** Performs an artificial click on the Button with the specified digit. */
    protected final void performClick(int digit) {
        if (digit < 0 || digit >= mButtons.length)
            throw new ArrayIndexOutOfBoundsException("No Button with digit " + digit);
        if (!mButtons[digit].isEnabled())
            throw new IllegalArgumentException("Button " + digit + " is disabled. " +
                    "Did you call AlarmNumpad.setInput(String) with an invalid time?");
        onClick(mButtons[digit]);
    }

    /** Performs an artificial click on the Button with the specified digit. */
    protected final void performClick(char charDigit) {
        performClick(asDigit(charDigit));
    }

    protected void setInput(int... digits) {
        if (digits.length != mInput.length)
            throw new IllegalArgumentException("Input arrays not the same length");
        for (int i = 0; i < digits.length; i++) {
            if (digits[i] < 0 || digits[i] > 9)
                throw new IllegalArgumentException("Element in input out of range");
            if (!mButtons[i].isEnabled())
                throw new IllegalStateException("Button with digit " + digits[i] + " is disabled");
            mInput[i] = digits[i];
        }
    }

    protected void backspace() {
        if (mCount - 1 >= 0) {
            mInput[--mCount] = UNMODIFIED;
        }
    }

    // public to allow hosts of this numpad to modify its contents
    public void backspace(int at) {
        if (at < 0 || at > mInput.length /* at == mInput.length is valid */)
            throw new IndexOutOfBoundsException("Cannot backspace on index " + at);
        if (at - 1 >= 0) {
            mInput[--at] = UNMODIFIED;
            mCount--;
        }
    }

    protected boolean longBackspace() {
        Arrays.fill(mInput, UNMODIFIED);
        mCount = 0;
        return true;
    }

    protected void setBackspaceEnabled(boolean enabled) {
        mBackspace.setEnabled(enabled);
    }

    protected final void notifyOnNumberInputListener(String number) {
        checkKeyListenerSet();
        mKeyListener.onNumberInput(number);
    }

    protected final void notifyOnBackspaceListener(String newStr) {
        checkKeyListenerSet();
        mKeyListener.onBackspace(newStr);
    }

    protected final void notifyOnLongBackspaceListener() {
        checkKeyListenerSet();
        mKeyListener.onLongBackspace();
    }

    private void setButtonLayoutParams(View target) {
        target.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1));
    }

    private void replace(int r, int c, View newView) {
        checkLocation(r, c);
        TableRow row = (TableRow) getChildAt(r);
        row.removeViewAt(c);
        row.addView(newView, c);
    }

    // Checks if the specified location in the View hierarchy exists.
    private void checkLocation(int r, int c) {
        if (r < 0 || r >= getChildCount())
            throw new IndexOutOfBoundsException("No TableRow at row " + r);
        if (c < 0 || c >= NUM_COLUMNS)
            throw new IndexOutOfBoundsException("No column " + c + " at row " + r);
    }

    private void checkIndexWithinBounds(int i) {
        if (i < 0 || i >= mInput.length) {
            throw new ArrayIndexOutOfBoundsException("Index " + i + "out of bounds");
        }
    }

    private void checkKeyListenerSet() {
        if (null == mKeyListener)
            throw new NullPointerException("Numpad Key listener not set");
    }

    private int asDigit(char charDigit) {
        if (!Character.isDigit(charDigit))
            throw new IllegalArgumentException("Character is not a digit");
        return Character.digit(charDigit, RADIX_10);
    }

    private void init() {
        View.inflate(getContext(), R.layout.content_numpad, this);
        ButterKnife.bind(this);
        if (capacity() < 0)
            throw new IllegalArgumentException("Negative capacity");
        mInput = new int[capacity()];
        Arrays.fill(mInput, UNMODIFIED);
    }
}
