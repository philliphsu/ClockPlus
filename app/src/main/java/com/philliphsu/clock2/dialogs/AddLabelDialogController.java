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
