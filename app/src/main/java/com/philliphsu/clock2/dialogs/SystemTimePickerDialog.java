/*
 * Copyright 2017 Phillip Hsu
 *
 * This file is part of ClockPlus.
 *
 * ClockPlus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ClockPlus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ClockPlus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.philliphsu.clock2.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

import com.philliphsu.bottomsheetpickers.time.BottomSheetTimePickerDialog;

/**
 * Created by Phillip Hsu on 9/28/2016.
 */
public class SystemTimePickerDialog extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private BottomSheetTimePickerDialog.OnTimeSetListener mListener;
    private int mInitialHourOfDay;
    private int mInitialMinute;
    private boolean mIs24HourMode;

    public static SystemTimePickerDialog newInstance(BottomSheetTimePickerDialog.OnTimeSetListener l,
                                                     int hourOfDay, int minute, boolean is24HourMode) {
        SystemTimePickerDialog dialog = new SystemTimePickerDialog();
        dialog.mListener = l;
        dialog.mInitialHourOfDay = hourOfDay;
        dialog.mInitialMinute = minute;
        dialog.mIs24HourMode = is24HourMode;
        return dialog;
    }

    public void setOnTimeSetListener(BottomSheetTimePickerDialog.OnTimeSetListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new TimePickerDialog(getActivity(), this, mInitialHourOfDay, mInitialMinute, mIs24HourMode);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // http://stackoverflow.com/q/19452993/5055032
        // BUG PRE-LOLLIPOP! This is also called when the dialog is dismissed, so clicking
        // the 'Done' button will end up calling this twice!
        if (view.isShown() && mListener != null) {
            mListener.onTimeSet(view, hourOfDay, minute);
        }
    }
}
