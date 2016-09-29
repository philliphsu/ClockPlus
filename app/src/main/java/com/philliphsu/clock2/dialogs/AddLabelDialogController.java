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

import android.support.v4.app.FragmentManager;
import android.util.Log;

/**
 * Created by Phillip Hsu on 9/6/2016.
 */
public final class AddLabelDialogController extends DialogFragmentController<AddLabelDialog> {
    private static final String TAG = "AddLabelController";

    private final AddLabelDialog.OnLabelSetListener mListener;

    public AddLabelDialogController(FragmentManager fragmentManager, AddLabelDialog.OnLabelSetListener listener) {
        super(fragmentManager);
        mListener = listener;
    }

    public void show(CharSequence initialText, String tag) {
        AddLabelDialog dialog = AddLabelDialog.newInstance(mListener, initialText);
        show(dialog, tag);
    }

    @Override
    public void tryRestoreCallback(String tag) {
        AddLabelDialog labelDialog = findDialog(tag);
        if (labelDialog != null) {
            Log.i(TAG, "Restoring add label callback");
            labelDialog.setOnLabelSetListener(mListener);
        }
    }
}
