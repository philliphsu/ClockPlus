package com.philliphsu.clock2.dialogs;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.format.DateFormat;

import com.philliphsu.clock2.R;
import com.philliphsu.clock2.timepickers.BaseTimePickerDialog;
import com.philliphsu.clock2.timepickers.NumberGridTimePickerDialog;
import com.philliphsu.clock2.timepickers.NumpadTimePickerDialog;

/**
 * Created by Phillip Hsu on 9/6/2016.
 */
public final class TimePickerDialogController extends DialogFragmentController<BaseTimePickerDialog> {
    private static final String TAG = "TimePickerController";

    private final BaseTimePickerDialog.OnTimeSetListener mListener;
    private final Context mContext;
    private final FragmentManager mFragmentManager;

    /**
     * @param context Used to read the user's preference for the style of the time picker dialog to show.
     */
    public TimePickerDialogController(FragmentManager fragmentManager, Context context,
                                      BaseTimePickerDialog.OnTimeSetListener listener) {
        super(fragmentManager);
        mFragmentManager = fragmentManager;
        mContext = context;
        mListener = listener;
    }

    public void show(int initialHourOfDay, int initialMinute, String tag) {
        BaseTimePickerDialog dialog = null;
        final String numpadStyle = mContext.getString(R.string.number_pad);
        final String gridStyle = mContext.getString(R.string.grid_selector);
        String prefTimePickerStyle = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString(mContext.getString(R.string.key_time_picker_style), numpadStyle);
        if (prefTimePickerStyle.equals(numpadStyle)) {
            dialog = NumpadTimePickerDialog.newInstance(mListener);
        } else if (prefTimePickerStyle.equals(gridStyle)) {
            dialog = NumberGridTimePickerDialog.newInstance(
                    mListener,
                    initialHourOfDay,
                    initialMinute,
                    DateFormat.is24HourFormat(mContext));
        } else {
            SystemTimePickerDialog timepicker = SystemTimePickerDialog.newInstance(
                    mListener, initialHourOfDay, initialMinute, DateFormat.is24HourFormat(mContext));
            timepicker.show(mFragmentManager, tag);
            return;
        }
        show(dialog, tag);
    }

    @Override
    public void tryRestoreCallback(String tag) {
        // Can't use #findDialog()!
        DialogFragment picker = (DialogFragment) mFragmentManager.findFragmentByTag(tag);
        if (picker instanceof BaseTimePickerDialog) {
            ((BaseTimePickerDialog) picker).setOnTimeSetListener(mListener);
        } else if (picker instanceof SystemTimePickerDialog) {
            ((SystemTimePickerDialog) picker).setOnTimeSetListener(mListener);
        }
    }
}
