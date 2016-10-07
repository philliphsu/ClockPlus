/*
 * Copyright (C) 2016 Phillip Hsu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.philliphsu.clock2.dialogs;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.format.DateFormat;

import com.philliphsu.bottomsheetpickers.time.BottomSheetTimePickerDialog;
import com.philliphsu.bottomsheetpickers.time.grid.GridTimePickerDialog;
import com.philliphsu.bottomsheetpickers.time.numberpad.NumberPadTimePickerDialog;
import com.philliphsu.clock2.R;

/**
 * Created by Phillip Hsu on 9/6/2016.
 */
public final class TimePickerDialogController extends DialogFragmentController<BottomSheetTimePickerDialog> {
    private static final String TAG = "TimePickerController";

    private final BottomSheetTimePickerDialog.OnTimeSetListener mListener;
    private final Context mContext;
    private final FragmentManager mFragmentManager;

    /**
     * @param context Used to read the user's preference for the style of the time picker dialog to show.
     */
    public TimePickerDialogController(FragmentManager fragmentManager, Context context,
                                      BottomSheetTimePickerDialog.OnTimeSetListener listener) {
        super(fragmentManager);
        mFragmentManager = fragmentManager;
        mContext = context;
        mListener = listener;
    }

    public void show(int initialHourOfDay, int initialMinute, String tag) {
        BottomSheetTimePickerDialog dialog = null;
        final String numpadStyle = mContext.getString(R.string.number_pad);
        final String gridStyle = mContext.getString(R.string.grid_selector);
        String prefTimePickerStyle = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString(mContext.getString(R.string.key_time_picker_style), numpadStyle);
        if (prefTimePickerStyle.equals(numpadStyle)) {
            dialog = NumberPadTimePickerDialog.newInstance(mListener);
        } else if (prefTimePickerStyle.equals(gridStyle)) {
            dialog = GridTimePickerDialog.newInstance(
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
        if (picker instanceof BottomSheetTimePickerDialog) {
            ((BottomSheetTimePickerDialog) picker).setOnTimeSetListener(mListener);
        } else if (picker instanceof SystemTimePickerDialog) {
            ((SystemTimePickerDialog) picker).setOnTimeSetListener(mListener);
        }
    }
}
