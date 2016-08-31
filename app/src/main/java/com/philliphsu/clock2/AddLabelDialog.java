package com.philliphsu.clock2;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import static com.philliphsu.clock2.util.KeyboardUtils.showKeyboard;

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
        void onLabelSet(String label);
    }

    /**
     * @param text the initial text
     */
    public static AddLabelDialog newInstance(OnLabelSetListener l, CharSequence text) {
        AddLabelDialog dialog = new AddLabelDialog();
        dialog.mOnLabelSetListener = l;
        dialog.mInitialText = text;
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mEditText = new EditText(getActivity());
        mEditText.setText(mInitialText);
        mEditText.setInputType(
                EditorInfo.TYPE_CLASS_TEXT // Needed or else we won't get automatic spacing between words
                | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES);

        // TODO: We can use the same value for both directions.
        int spacingLeft = getResources().getDimensionPixelSize(R.dimen.item_padding_start);
        int spacingRight = getResources().getDimensionPixelSize(R.dimen.item_padding_end);

        final AlertDialog alert = new AlertDialog.Builder(getActivity(), getTheme())
                .setTitle(R.string.label)
                .setView(mEditText, spacingLeft, 0, spacingRight, 0)
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
                            // If we passed the text back as an Editable (subtype of CharSequence
                            // used in EditText), then there may be text formatting left in there,
                            // which we don't want.
                            mOnLabelSetListener.onLabelSet(mEditText.getText().toString());
                        }
                        dismiss();
                    }
                })
                .create();

        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                showKeyboard(getActivity(), mEditText);
                mEditText.setSelection(0, mEditText.length());
            }
        });
        return alert;
    }
}
