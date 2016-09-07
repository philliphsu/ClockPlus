package com.philliphsu.clock2;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.text.format.DateFormat;
import android.util.Log;

import com.philliphsu.clock2.editalarm.BaseTimePickerDialog;
import com.philliphsu.clock2.editalarm.NumberGridTimePickerDialog;
import com.philliphsu.clock2.editalarm.NumpadTimePickerDialog;

/**
 * Created by Phillip Hsu on 9/6/2016.
 */
public final class TimePickerDialogController extends DialogFragmentController<BaseTimePickerDialog> {
    private static final String TAG = "TimePickerController";

    private final BaseTimePickerDialog.OnTimeSetListener mListener;
    private final Context mContext;

    /**
     * @param context Used to read the user's preference for the style of the time picker dialog to show.
     */
    public TimePickerDialogController(FragmentManager fragmentManager, Context context,
                                      BaseTimePickerDialog.OnTimeSetListener listener) {
        super(fragmentManager);
        mContext = context;
        mListener = listener;
    }

    public void show(int initialHourOfDay, int initialMinute, String tag) {
        BaseTimePickerDialog dialog = null;
        String numpadStyle = mContext.getString(R.string.number_pad);
        String gridStyle = mContext.getString(R.string.grid_selector);
        String prefTimePickerStyle = PreferenceManager.getDefaultSharedPreferences(mContext).getString(
                // key for the preference value to retrieve
                mContext.getString(R.string.key_time_picker_style),
                // default value
                numpadStyle);
        if (prefTimePickerStyle.equals(numpadStyle)) {
            dialog = NumpadTimePickerDialog.newInstance(mListener);
        } else if (prefTimePickerStyle.equals(gridStyle)) {
            dialog = NumberGridTimePickerDialog.newInstance(
                    mListener,
                    initialHourOfDay,
                    initialMinute,
                    DateFormat.is24HourFormat(mContext));
        }
        // We don't have a default case, because we don't need one; prefTimePickerStyle
        // will ALWAYS match one of numpadStyle or gridStyle. As such, the dialog
        // will NEVER be null.
        show(dialog, tag);
    }

    @Override
    public void tryRestoreCallback(String tag) {
        BaseTimePickerDialog picker = findDialog(tag);
        if (picker != null) {
            Log.i(TAG, "Restoring time picker callback: " + mListener);
            picker.setOnTimeSetListener(mListener);
        }
    }
}
