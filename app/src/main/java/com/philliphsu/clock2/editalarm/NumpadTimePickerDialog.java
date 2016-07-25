package com.philliphsu.clock2.editalarm;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.philliphsu.clock2.R;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnTouch;

/**
 * Created by Phillip Hsu on 7/12/2016.
 *
 */
public class NumpadTimePickerDialog extends BaseTimePickerDialog
        implements NumpadTimePicker.OnInputChangeListener {

    private static final String KEY_HOUR_OF_DAY = "hour_of_day";
    private static final String KEY_MINUTE = "minute";
    private static final String KEY_IS_24_HOUR_VIEW = "is_24_hour_view";
    private static final String KEY_DIGITS_INPUTTED = "digits_inputted";

    private int mInitialHourOfDay;
    private int mInitialMinute;
    private boolean mIs24HourMode;
    /**
     * The digits stored in the numpad from the last time onSaveInstanceState() was called.
     *
     * Why not have the NumpadTimePicker class save state itself? Because it's a lot more
     * code to do so, as you have to create your own SavedState subclass. Also, we modeled
     * this dialog class on the RadialTimePickerDialog, where the RadialPickerLayout also
     * depends on the dialog to save its state.
     */
    private int[] mInputtedDigits;

    // Don't need to keep a reference to the dismiss ImageButton
    @Bind(R.id.input_time) EditText mInputField;
    @Bind(R.id.number_grid) NumpadTimePicker mNumpad;
    @Bind(R.id.focus_grabber) View mFocusGrabber;
    @Bind({ R.id.leftAlt, R.id.rightAlt })
    Button[] mAltButtons;
    @Bind(R.id.fab) FloatingActionButton mFab;

    // TODO: We don't need to pass in an initial hour and minute for a new instance.
    // TODO: Delete is24HourMode?
    @Deprecated
    public static NumpadTimePickerDialog newInstance(OnTimeSetListener callback,
                                                     int hourOfDay, int minute, boolean is24HourMode) {
        NumpadTimePickerDialog ret = new NumpadTimePickerDialog();
        ret.initialize(callback, hourOfDay, minute, is24HourMode);
        return ret;
    }

    public static NumpadTimePickerDialog newInstance(OnTimeSetListener callback) {
        NumpadTimePickerDialog ret = new NumpadTimePickerDialog();
        ret.setOnTimeSetListener(callback);
        return ret;
    }

    @Deprecated
    public void initialize(OnTimeSetListener callback,
                           int hourOfDay, int minute, boolean is24HourMode) {
        mCallback = callback;
        mInitialHourOfDay = hourOfDay;
        mInitialMinute = minute;
        mIs24HourMode = is24HourMode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mInputtedDigits = savedInstanceState.getIntArray(KEY_DIGITS_INPUTTED);
            mIs24HourMode = savedInstanceState.getBoolean(KEY_IS_24_HOUR_VIEW);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Pass in our views so numpad can control their states for us
        mNumpad.setFab(mFab);
        mNumpad.setAltButtons(mAltButtons[0], mAltButtons[1]);

        mNumpad.setOnInputChangeListener(this);
        mNumpad.insertDigits(mInputtedDigits); // TOneverDO: before mNumpad.setOnInputChangeListener(this);
        // Show the cursor immediately
        mInputField.requestFocus();
        // TODO: Disabled color
        //updateInputText(""); // Primarily to disable 'OK'

        if (DateFormat.is24HourFormat(getActivity())) {
            mAltButtons[0].setText(R.string.left_alt_24hr);
            mAltButtons[1].setText(R.string.right_alt_24hr);
        } else {
            String[] amPmTexts = new DateFormatSymbols().getAmPmStrings();
            mAltButtons[0].setText(amPmTexts[Calendar.AM]);
            mAltButtons[1].setText(amPmTexts[Calendar.PM]);
        }

        return view;
    }

    @Override
    protected int contentLayout() {
        return R.layout.dialog_time_picker_numpad;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mNumpad != null) {
            outState.putIntArray(KEY_DIGITS_INPUTTED, mNumpad.getDigits());
            outState.putBoolean(KEY_IS_24_HOUR_VIEW, mIs24HourMode);
            //outState.putBoolean(KEY_DARK_THEME, mThemeDark);
        }
    }

    @Override
    public void onDigitInserted(String newStr) {
        updateInputText(newStr);
    }

    @Override
    public void onDigitDeleted(String newStr) {
        updateInputText(newStr);
    }

    @Override
    public void onDigitsCleared() {
        updateInputText("");
    }

    @Override
    public void onInputDisabled() {
        // Steals the focus from the EditText
        mFocusGrabber.requestFocus();
    }

    @OnTouch(R.id.input_time)
    boolean captureTouchOnEditText() {
        // Capture touch events on the EditText field, because we want it to do nothing.
        return true;
    }

    @OnClick(R.id.fab)
    void confirmSelection() {
        if (!mNumpad.checkTimeValid())
            return;
        mCallback.onTimeSet(mNumpad, mNumpad.hourOfDay(), mNumpad.minute());
        dismiss();
    }

    private void updateInputText(String inputText) {
        TimeTextUtils.setText(inputText, mInputField);
        // Move the cursor
        mInputField.setSelection(mInputField.length());
        if (mFocusGrabber.isFocused()) {
            // Return focus to the EditText
            mInputField.requestFocus();
        }
    }
}
