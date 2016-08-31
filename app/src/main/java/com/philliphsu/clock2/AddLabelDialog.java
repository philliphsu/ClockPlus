package com.philliphsu.clock2;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.widget.EditText;

/**
 * Created by Phillip Hsu on 8/30/2016.
 *
 * TODO: If we have any other needs for a dialog with an EditText, rename this to EditTextDialog,
 * and change the callback interface name appropriately.
 */
public class AddLabelDialog extends AppCompatDialogFragment {

    private EditText mEditText;
    private OnLabelSetListener mOnLabelSetListener;

    private CharSequence mInitialText;

    public interface OnLabelSetListener {
        void onLabelSet(CharSequence label);
    }

    /**
     * @param text the initial text
     */
    public static AddLabelDialog newInstance(CharSequence text) {
        AddLabelDialog dialog = new AddLabelDialog();
        dialog.mInitialText = text;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mEditText = new EditText(getActivity());
        mEditText.setText(mInitialText);
        mEditText.setSelection(0, mEditText.length());

        return new AlertDialog.Builder(getActivity(), getTheme())
                .setTitle(R.string.label)
                .setView(mEditText)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mOnLabelSetListener != null) {
                            mOnLabelSetListener.onLabelSet(mEditText.getText());
                        }
                        dismiss();
                    }
                })
                .create();
    }

    public void setOnLabelSetListener(OnLabelSetListener onLabelSetListener) {
        mOnLabelSetListener = onLabelSetListener;
    }
}
