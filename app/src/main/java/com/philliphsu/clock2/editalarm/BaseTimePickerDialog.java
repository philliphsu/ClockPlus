package com.philliphsu.clock2.editalarm;

import android.support.v4.app.DialogFragment;

/**
 * Created by Phillip Hsu on 7/16/2016.
 */
public abstract class BaseTimePickerDialog extends DialogFragment {

    /*package*/ TimePicker.OnTimeSetListener mCallback;

    /**
     * Empty constructor required for dialog fragment.
     * Subclasses do not need to write their own.
     */
    public BaseTimePickerDialog() {}

    public final void setOnTimeSetListener(TimePicker.OnTimeSetListener callback) {
        mCallback = callback;
    }
}
