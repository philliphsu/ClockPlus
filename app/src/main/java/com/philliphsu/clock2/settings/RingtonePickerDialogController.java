package com.philliphsu.clock2.settings;

import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.philliphsu.clock2.DialogFragmentController;
import com.philliphsu.clock2.RingtonePickerDialog;

/**
 * Created by Phillip Hsu on 9/20/2016.
 */
public class RingtonePickerDialogController extends DialogFragmentController<RingtonePickerDialog> {
    private static final String TAG = "RingtonePickerCtrller";

    private final RingtonePickerDialog.OnRingtoneSelectedListener mListener;

    public RingtonePickerDialogController(FragmentManager fragmentManager, RingtonePickerDialog.OnRingtoneSelectedListener l) {
        super(fragmentManager);
        mListener = l;
    }

    public void show(Uri initialUri, String tag) {
        RingtonePickerDialog dialog = RingtonePickerDialog.newInstance(mListener, initialUri);
        show(dialog, tag);
    }

    @Override
    public void tryRestoreCallback(String tag) {
        RingtonePickerDialog dialog = findDialog(tag);
        if (dialog != null) {
            Log.i(TAG, "Restoring on ringtone selected callback");
            dialog.setOnRingtoneSelectedListener(mListener);
        }
    }
}
