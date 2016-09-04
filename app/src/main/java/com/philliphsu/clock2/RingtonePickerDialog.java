package com.philliphsu.clock2;

import android.content.DialogInterface;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

/**
 * Created by Phillip Hsu on 9/3/2016.
 * <p></p>
 * An alternative to the system's ringtone picker dialog. The differences are:
 * (1) this dialog matches the current theme,
 * (2) the selected ringtone URI is delivered via the {@link OnRingtoneSelectedListener
 * OnRingtoneSelectedListener} callback.
 */
public class RingtonePickerDialog extends BaseAlertDialogFragment {
    private static final String TAG = "RingtonePickerDialog";

    private RingtoneManager mRingtoneManager;
    private OnRingtoneSelectedListener mOnRingtoneSelectedListener;
    private Uri mRingtoneUri;
    private Ringtone mRingtone;

    public interface OnRingtoneSelectedListener {
        void onRingtoneSelected(Uri ringtoneUri);
    }

    /**
     * @param ringtoneUri the URI of the ringtone to show as initially selected
     */
    public static RingtonePickerDialog newInstance(OnRingtoneSelectedListener l, Uri ringtoneUri) {
        RingtonePickerDialog dialog = new RingtonePickerDialog();
        dialog.mOnRingtoneSelectedListener = l;
        dialog.mRingtoneUri = ringtoneUri;
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRingtoneManager = new RingtoneManager(getActivity());
        mRingtoneManager.setType(RingtoneManager.TYPE_ALARM);
    }

    @Override
    protected AlertDialog createFrom(AlertDialog.Builder builder) {
        // TODO: We set the READ_EXTERNAL_STORAGE permission. Verify that this includes the user's 
        // custom ringtone files.
        Cursor cursor = mRingtoneManager.getCursor();
        int checkedItem = mRingtoneManager.getRingtonePosition(mRingtoneUri);
        String labelColumn = cursor.getColumnName(RingtoneManager.TITLE_COLUMN_INDEX);

        builder.setTitle(R.string.ringtones)
                .setSingleChoiceItems(cursor, checkedItem, labelColumn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Here, 'which' param refers to the position of the item clicked.
                        mRingtoneUri = mRingtoneManager.getRingtoneUri(which);
                        mRingtone = mRingtoneManager.getRingtone(which);
                        // TODO: Deprecated, but setAudioAttributes() is for 21+, so what is the
                        // pre-21 alternative?
                        mRingtone.setStreamType(AudioManager.STREAM_ALARM);
                        mRingtone.play();
                    }
                });
        return super.createFrom(builder);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mRingtone != null && mRingtone.isPlaying()) {
            mRingtone.stop();
        }
    }

    @Override
    protected void onOk() {
        if (mOnRingtoneSelectedListener != null) {
            // Here, 'which' param refers to the position of the item clicked.
            mOnRingtoneSelectedListener.onRingtoneSelected(mRingtoneUri);
        }
        dismiss();
    }

    public void setOnRingtoneSelectedListener(OnRingtoneSelectedListener onRingtoneSelectedListener) {
        mOnRingtoneSelectedListener = onRingtoneSelectedListener;
    }
}
