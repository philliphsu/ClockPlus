package com.philliphsu.clock2.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;

import com.philliphsu.clock2.timepickers.BaseTimePickerDialog;

import java.util.Calendar;

/**
 * Created by Phillip Hsu on 9/28/2016.
 */
public class SystemTimePickerDialog extends android.support.v4.app.DialogFragment implements TimePickerDialog.OnTimeSetListener {

    public static SystemTimePickerDialog newInstance(BaseTimePickerDialog.OnTimeSetListener l,
                                                     int hourOfDay, int minute, boolean is24HourMode) {
        SystemTimePickerDialog dialog = new SystemTimePickerDialog();
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // http://stackoverflow.com/q/19452993/5055032
        // BUG! This is also called when the dialog is dismissed, so clicking
        // the 'Done' button will end up calling this twice!
        if (view.isShown()) {
            Log.d("dfljsdlfkj", "Calling onTimeSet");
        }
    }
}
