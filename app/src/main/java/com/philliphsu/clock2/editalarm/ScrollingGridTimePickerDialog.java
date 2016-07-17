package com.philliphsu.clock2.editalarm;

/**
 * Created by Phillip Hsu on 7/16/2016.
 */
public class ScrollingGridTimePickerDialog extends BaseTimePickerDialog {

    private TimePicker.OnTimeSetListener mCallback;

    public static NumpadTimePickerDialog newInstance(TimePicker.OnTimeSetListener callback) {
        NumpadTimePickerDialog ret = new NumpadTimePickerDialog();
        ret.setOnTimeSetListener(callback);
        return ret;
    }

}
