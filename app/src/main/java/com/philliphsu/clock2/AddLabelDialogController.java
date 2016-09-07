package com.philliphsu.clock2;

import android.support.v4.app.FragmentManager;
import android.util.Log;

/**
 * Created by Phillip Hsu on 9/6/2016.
 */
public final class AddLabelDialogController {
    private static final String TAG = "add_label_dialog";

    private final FragmentManager mFragmentManager;
    private final AddLabelDialog.OnLabelSetListener mListener;

    public AddLabelDialogController(FragmentManager fragmentManager, AddLabelDialog.OnLabelSetListener listener) {
        mFragmentManager = fragmentManager;
        mListener = listener;
    }

    public void show(CharSequence initialText) {
        AddLabelDialog dialog = AddLabelDialog.newInstance(mListener, initialText);
        dialog.show(mFragmentManager, TAG);
//        show(dialog, TAG);
    }

    // TODO: Rename to onConfigurationChange()?
    public void tryRestoreCallback() {
        AddLabelDialog labelDialog = (AddLabelDialog) mFragmentManager.findFragmentByTag(TAG);
        if (labelDialog != null) {
            Log.i(TAG, "Restoring add label callback");
            labelDialog.setOnLabelSetListener(mListener);
        }
    }
}
