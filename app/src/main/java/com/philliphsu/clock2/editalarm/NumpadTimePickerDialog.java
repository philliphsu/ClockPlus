package com.philliphsu.clock2.editalarm;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.philliphsu.clock2.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by Phillip Hsu on 7/12/2016.
 *
 * Note this extends the framework's DialogFragment, NOT the support version's!
 */
public class NumpadTimePickerDialog extends DialogFragment
        implements NumpadTimePicker.OnInputChangeListener {

    private static final String KEY_HOUR_OF_DAY = "hour_of_day";
    private static final String KEY_MINUTE = "minute";
    private static final String KEY_IS_24_HOUR_VIEW = "is_24_hour_view";

    private OnTimeSetListener mCallback;

    private int mInitialHourOfDay;
    private int mInitialMinute;
    private boolean mIs24HourMode;

    @Bind(R.id.backspace) ImageButton mBackspace;
    @Bind(R.id.input) EditText mInputField;
    @Bind(R.id.cancel) Button mCancelButton;
    @Bind(R.id.ok) Button mOkButton;
    @Bind(R.id.number_grid) NumpadTimePicker mNumpad;

    public NumpadTimePickerDialog() {
        // Empty constructor required for dialog fragment.
    }

    // TODO: We don't need to pass in an initial hour and minute for a new instance.
    public static NumpadTimePickerDialog newInstance(OnTimeSetListener callback,
                                                     int hourOfDay, int minute, boolean is24HourMode) {
        NumpadTimePickerDialog ret = new NumpadTimePickerDialog();
        ret.initialize(callback, hourOfDay, minute, is24HourMode);
        return ret;
    }

    public void initialize(OnTimeSetListener callback,
                           int hourOfDay, int minute, boolean is24HourMode) {
        mCallback = callback;
        mInitialHourOfDay = hourOfDay;
        mInitialMinute = minute;
        mIs24HourMode = is24HourMode;
    }

    public void setOnTimeSetListener(OnTimeSetListener callback) {
        mCallback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_HOUR_OF_DAY)
                && savedInstanceState.containsKey(KEY_MINUTE)
                && savedInstanceState.containsKey(KEY_IS_24_HOUR_VIEW)) {
            mInitialHourOfDay = savedInstanceState.getInt(KEY_HOUR_OF_DAY);
            mInitialMinute = savedInstanceState.getInt(KEY_MINUTE);
            mIs24HourMode = savedInstanceState.getBoolean(KEY_IS_24_HOUR_VIEW);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.dialog_time_picker_numpad, container, false);
        ButterKnife.bind(this, view);
        mNumpad.setOnInputChangeListener(this);

        //mNumpad.setTime(mInitialHourOfDay, mInitialMinute);
        // TODO: Write numpad method set24HourMode() and use mIs24HourMode

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mNumpad != null) {
            outState.putInt(KEY_HOUR_OF_DAY, mNumpad.getHours());
            outState.putInt(KEY_MINUTE, mNumpad.getMinutes());
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

    @OnClick(R.id.cancel)
    void myCancel() {
        dismiss();
    }

    @OnClick(R.id.ok)
    void ok() {
        if (!mNumpad.checkTimeValid())
            return;
        mCallback.onTimeSet(mNumpad, mNumpad.getHours(), mNumpad.getMinutes());
        dismiss();
    }

    @OnClick(R.id.backspace)
    void backspace() {
        mNumpad.delete();
    }

    @OnLongClick(R.id.backspace)
    boolean longBackspace() {
        mNumpad.clear();
        return true;
    }

    private void updateInputText(String inputText) {
        mInputField.setText(inputText);
    }
}
