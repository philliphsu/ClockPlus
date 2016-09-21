package com.philliphsu.clock2.settings;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.preference.RingtonePreference;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;

import com.philliphsu.clock2.RingtonePickerDialog;

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
