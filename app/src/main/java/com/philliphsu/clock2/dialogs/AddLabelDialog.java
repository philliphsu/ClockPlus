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

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.philliphsu.clock2.R;

import static com.philliphsu.clock2.util.KeyboardUtils.showKeyboard;

/**
 * Created by Phillip Hsu on 8/30/2016.
 *
 * TODO: If we have any other needs for a dialog with an EditText, rename this to EditTextDialog,
 * and change the callback interface name appropriately.
 */
public class AddLabelDialog extends BaseAlertDialogFragment {

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
    protected AlertDialog createFrom(AlertDialog.Builder builder) {
        mEditText = new AppCompatEditText(getActivity());
        // Views must have IDs set to automatically save instance state
        mEditText.setId(R.id.label);
        mEditText.setText(mInitialText);
        mEditText.setInputType(
                EditorInfo.TYPE_CLASS_TEXT // Needed or else we won't get automatic spacing between words
                | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES);

        // TODO: We can use the same value for both directions.
        int spacingLeft = getResources().getDimensionPixelSize(R.dimen.item_padding_start);
        int spacingRight = getResources().getDimensionPixelSize(R.dimen.item_padding_end);

        builder.setTitle(R.string.label)
                .setView(mEditText, spacingLeft, 0, spacingRight, 0);

        AlertDialog dialog = super.createFrom(builder);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                showKeyboard(getActivity(), mEditText);
                mEditText.setSelection(0, mEditText.length());
            }
        });
        return dialog;
    }

    @Override
    protected void onOk() {
        if (mOnLabelSetListener != null) {
            // If we passed the text back as an Editable (subtype of CharSequence
            // used in EditText), then there may be text formatting left in there,
            // which we don't want.
            mOnLabelSetListener.onLabelSet(mEditText.getText().toString());
        }
        dismiss();
    }

    public void setOnLabelSetListener(OnLabelSetListener l) {
        mOnLabelSetListener = l;
    }
}
