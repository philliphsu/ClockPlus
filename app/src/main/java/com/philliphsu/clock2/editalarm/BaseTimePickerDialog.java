package com.philliphsu.clock2.editalarm;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

/**
 * Created by Phillip Hsu on 7/16/2016.
 */
public abstract class BaseTimePickerDialog extends BottomSheetDialogFragment {

    // TODO: Consider private access, and then writing package/protected API that subclasses
    // can use to interface with this field.
    /*package*/ TimePicker.OnTimeSetListener mCallback;

    /**
     * Empty constructor required for dialog fragment.
     * Subclasses do not need to write their own.
     */
    public BaseTimePickerDialog() {}

    @LayoutRes
    protected abstract int contentLayout();

    public final void setOnTimeSetListener(TimePicker.OnTimeSetListener callback) {
        mCallback = callback;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // We're past onCreate() in the lifecycle, so we can safely retrieve the host activity.
        View view = LayoutInflater.from(getActivity()).inflate(contentLayout(), null);
        /**
         * Adds our view to a ViewGroup that has a BottomSheetBehavior attached. The ViewGroup
         * itself is a child of a CoordinatorLayout.
         * @see {@link BottomSheetDialog#wrapInBottomSheet(int, View, ViewGroup.LayoutParams)}
         */
        dialog.setContentView(view);
        // Bind this fragment, not the internal dialog!
        ButterKnife.bind(this, view);
        final BottomSheetBehavior behavior = BottomSheetBehavior.from((View) view.getParent());
        // When we collapse, collapse all the way. Do not be misled by the "docs" in
        // https://android-developers.blogspot.com.au/2016/02/android-support-library-232.html
        // when it says:
        // "STATE_COLLAPSED: ... the app:behavior_peekHeight attribute (defaults to 0)"
        // While it is true by default, BottomSheetDialogs override this default height.
        // See http://stackoverflow.com/a/35634293/5055032 for an alternative solution involving
        // defining a style that overrides the attribute.
        // TODO: If the sheet is dragged out of view, then the screen remains darkened until
        // a subsequent touch on the screen. Consider doing the alt. soln.?
        behavior.setPeekHeight(0);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                // Every time we show, show at our full height.
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        return dialog;
    }

    /*
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(contentLayout(), container, false);
        ButterKnife.bind(this, view);
        return view;
    }
    */

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
