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

import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

/**
 * Created by Phillip Hsu on 9/6/2016.
 */
public abstract class DialogFragmentController<T extends DialogFragment> {
    private static final String TAG = "DialogController";

    private final FragmentManager mFragmentManager;

    // TODO: Rename to onConfigurationChange()?
    public abstract void tryRestoreCallback(String tag);

    public DialogFragmentController(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
    }

    /**
     * Shows the dialog with the given tag.
     */
    protected final void show(T dialog, String tag) {
        Log.d(TAG, "Showing dialog " + dialog + "with tag " + tag);
        dialog.show(mFragmentManager, tag);
    }

    /**
     * Tries to find the dialog in our {@code FragmentManager} with the provided tag.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    protected final T findDialog(String tag) {
        // https://docs.oracle.com/javase/tutorial/java/generics/restrictions.html#cannotCast
        // Typically, we can't cast to a generic type. However, I've written non-generic code that
        // blindly casts the result to an arbitrary type that I expect is correct, so this is
        // pretty much the same thing.
        Log.d(TAG, "Finding dialog by tag " + tag);
        return (T) mFragmentManager.findFragmentByTag(tag);
    }
}
