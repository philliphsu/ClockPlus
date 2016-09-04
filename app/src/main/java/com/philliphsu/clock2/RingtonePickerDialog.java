package com.philliphsu.clock2;

import android.net.Uri;

/**
 * Created by Phillip Hsu on 9/3/2016.
 */
public class RingtonePickerDialog extends BaseAlertDialogFragment {

    private OnRingtoneSetListener mOnRingtoneSetListener;
    private Uri mInitialRingtoneUri;

    public interface OnRingtoneSetListener {
        void onRingtoneSet(Uri ringtoneUri);
    }

    /**
     * @param initialRingtoneUri the URI of the ringtone to show as initially selected
     */
    public static RingtonePickerDialog newInstance(OnRingtoneSetListener l, Uri initialRingtoneUri) {
        RingtonePickerDialog dialog = new RingtonePickerDialog();
        dialog.mOnRingtoneSetListener = l;
        dialog.mInitialRingtoneUri = initialRingtoneUri;
        return dialog;
    }

    @Override
    protected void onOk() {

    }

    public void setOnRingtoneSetListener(OnRingtoneSetListener onRingtoneSetListener) {
        mOnRingtoneSetListener = onRingtoneSetListener;
    }
}
