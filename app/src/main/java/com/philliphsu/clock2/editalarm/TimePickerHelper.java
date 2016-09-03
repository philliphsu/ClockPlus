package com.philliphsu.clock2.editalarm;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;

import com.philliphsu.clock2.R;

/**
 * Created by Phillip Hsu on 9/2/2016.
 * 
 * Helper for creating a time picker dialog.
 */
public final class TimePickerHelper {
    
    public static BaseTimePickerDialog newDialog(Context context, BaseTimePickerDialog.OnTimeSetListener l) {
        BaseTimePickerDialog dialog = null;
        String numpadStyle = context.getString(R.string.number_pad);
        String gridStyle = context.getString(R.string.grid_selector);
        String prefTimePickerStyle = PreferenceManager.getDefaultSharedPreferences(context).getString(
                // key for the preference value to retrieve
                context.getString(R.string.key_time_picker_style),
                // default value
                numpadStyle);
        if (prefTimePickerStyle.equals(numpadStyle)) {
            dialog = NumpadTimePickerDialog.newInstance(l);
        } else if (prefTimePickerStyle.equals(gridStyle)) {
            dialog = NumberGridTimePickerDialog.newInstance(
                    l, // OnTimeSetListener
                    0, // Initial hour of day
                    0, // Initial minute
                    DateFormat.is24HourFormat(context));
        }
        // We don't have a default case, because we don't need one; prefTimePickerStyle
        // will ALWAYS match one of numpadStyle or gridStyle. As such, the dialog returned
        // from here will NEVER be null.
        return dialog;
    }

    private TimePickerHelper() {}
}
