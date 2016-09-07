package com.philliphsu.clock2;

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
        Log.d(TAG, "Finding dialog with tag " + tag);
        return (T) mFragmentManager.findFragmentByTag(tag);
    }
}
