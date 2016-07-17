package com.philliphsu.clock2.editalarm;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import butterknife.ButterKnife;

/**
 * Created by Phillip Hsu on 7/16/2016.
 */
public abstract class BaseTimePickerDialog extends DialogFragment {

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(contentLayout(), container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
