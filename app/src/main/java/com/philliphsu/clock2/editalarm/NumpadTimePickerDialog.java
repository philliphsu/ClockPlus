package com.philliphsu.clock2.editalarm;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.philliphsu.clock2.R;
import com.philliphsu.clock2.aospdatetimepicker.Utils;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by Phillip Hsu on 7/12/2016.
 *
 */
public class NumpadTimePickerDialog extends BaseTimePickerDialog
        implements NumpadTimePicker.OnInputChangeListener {
    private static final String TAG = "NumpadTimePickerDialog";

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
    private boolean mThemeDark;

    // Don't need to keep a reference to the dismiss ImageButton
    @Bind(R.id.input_time) TextView mInputField;
    @Bind(R.id.number_grid) NumpadTimePicker mNumpad;
//    @Bind(R.id.focus_grabber) View mFocusGrabber;

    // TODO: We don't need to pass in an initial hour and minute for a new instance.
    @Deprecated
    public static NumpadTimePickerDialog newInstance(OnTimeSetListener callback,
                                                     int hourOfDay, int minute, boolean is24HourMode) {
        NumpadTimePickerDialog ret = new NumpadTimePickerDialog();
        ret.initialize(callback, hourOfDay, minute, is24HourMode);
        return ret;
    }

    // TODO: is24HourMode param
    public static NumpadTimePickerDialog newInstance(OnTimeSetListener callback) {
        NumpadTimePickerDialog ret = new NumpadTimePickerDialog();
        // TODO: Do these in initialize()
        ret.setOnTimeSetListener(callback);
        ret.mThemeDark = false;
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

    /**
     * Set a dark or light theme. NOTE: this will only take effect for the next onCreateView.
     */
    public void setThemeDark(boolean dark) {
        mThemeDark = dark;
    }

    public boolean isThemeDark() {
        return mThemeDark;
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
        mNumpad.setOnInputChangeListener(this);
        mNumpad.insertDigits(mInputtedDigits); // TOneverDO: before mNumpad.setOnInputChangeListener(this);
        // Show the cursor immediately
//        mInputField.requestFocus();
        //updateInputText(""); // Primarily to disable 'OK'

        // Prepare colors
        int accentColor = Utils.getThemeAccentColor(getContext());
        int lightGray = ContextCompat.getColor(getContext(), R.color.light_gray);
        int darkGray = ContextCompat.getColor(getContext(), R.color.dark_gray);
        int white = ContextCompat.getColor(getContext(), android.R.color.white);

        // Set background color of entire view
        view.setBackgroundColor(mThemeDark? darkGray : white);

        TextView inputTime = (TextView) view.findViewById(R.id.input_time);
        inputTime.setBackgroundColor(mThemeDark? lightGray : accentColor);
        inputTime.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));

        mNumpad.setTheme(getContext()/*DO NOT GIVE THE APPLICATION CONTEXT, OR ELSE THE NUMPAD
        CAN'T GET THE CORRECT ACCENT COLOR*/, mThemeDark);

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
//        mFocusGrabber.requestFocus();
    }

//    @OnTouch(R.id.input_time)
//    boolean captureTouchOnEditText() {
//        // Capture touch events on the EditText field, because we want it to do nothing.
//        return true;
//    }

    // The FAB is not defined directly in this dialog's layout, but rather in the layout
    // of the NumpadTimePicker. We can always reference a child of a ViewGroup that is
    // part of our layout.
    @OnClick(R.id.fab)
    void confirmSelection() {
        if (!mNumpad.checkTimeValid())
            return;
        mCallback.onTimeSet(mNumpad, mNumpad.getHour(), mNumpad.getMinute());
        dismiss();
    }

    private void updateInputText(String inputText) {
        TimeTextUtils.setText(inputText, mInputField);
//        // Move the cursor
//        mInputField.setSelection(mInputField.length());
//        if (mFocusGrabber.isFocused()) {
//            // Return focus to the EditText
//            mInputField.requestFocus();
//        }
    }
}
