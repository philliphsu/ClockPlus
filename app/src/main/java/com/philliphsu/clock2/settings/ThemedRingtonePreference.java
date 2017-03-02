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

package com.philliphsu.clock2.settings;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Parcelable;
import android.preference.RingtonePreference;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;

import com.philliphsu.clock2.dialogs.RingtonePickerDialog;
import com.philliphsu.clock2.dialogs.RingtonePickerDialogController;

/**
 * Created by Phillip Hsu on 9/20/2016.
 *
 * <p>A modified version of the framework's {@link android.preference.RingtonePreference} that
 * uses our {@link RingtonePickerDialog} instead of the system's ringtone picker.</p>
 */
public class ThemedRingtonePreference extends RingtonePreference
        implements RingtonePickerDialog.OnRingtoneSelectedListener {
    private static final String TAG = "ThemedRingtonePreference";
    
    private RingtonePickerDialogController mController;

    @TargetApi(21)
    public ThemedRingtonePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ThemedRingtonePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ThemedRingtonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThemedRingtonePreference(Context context) {
        super(context);
    }

    @Override
    protected void onClick() {
        if (mController == null) {
            mController = newController();
        }
        mController.show(onRestoreRingtone(), TAG);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        if (mController == null) {
            mController = newController();
        }
        mController.tryRestoreCallback(TAG);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // Our picker does not show a 'Default' item, so our defaultValue
        // "content://settings/system/alarm_alert" set in XML will not show as initially selected.
        // The default ringtone and its sound file URI is different for every device,
        // so there isn't a string literal we can specify in XML.
        // This is the same as calling:
        //     `RingtoneManager.getActualDefaultRingtoneUri(
        //         getContext(), RingtoneManager.TYPE_ALARM).toString();`
        // but skips the toString().
        return Settings.System.getString(getContext().getContentResolver(), Settings.System.ALARM_ALERT);
    }

    @Override
    public void onRingtoneSelected(Uri uri) {
        if (callChangeListener(uri != null ? uri.toString() : "")) {
            onSaveRingtone(uri);
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return false;
    }

    private RingtonePickerDialogController newController() {
        // TODO: BAD!
        AppCompatActivity a = (AppCompatActivity) getContext();
        return new RingtonePickerDialogController(a.getSupportFragmentManager(), this);
    }
}
