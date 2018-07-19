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

package com.philliphsu.clock2.timers;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayout;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.philliphsu.clock2.dialogs.AddLabelDialog;
import com.philliphsu.clock2.dialogs.AddLabelDialogController;
import com.philliphsu.clock2.BaseActivity;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.util.FragmentTagUtils;


import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.OnTouch;

// TODO: Rename to CreateTimerActivity
public class EditTimerActivity extends BaseActivity implements AddLabelDialog.OnLabelSetListener {
    private static final int FIELD_LENGTH = 2;
    private static final String KEY_LABEL = "key_label";

    public static final String EXTRA_HOUR = "com.philliphsu.clock2.edittimer.extra.HOUR";
    public static final String EXTRA_MINUTE = "com.philliphsu.clock2.edittimer.extra.MINUTE";
    public static final String EXTRA_SECOND = "com.philliphsu.clock2.edittimer.extra.SECOND";
    public static final String EXTRA_LABEL = "com.philliphsu.clock2.edittimer.extra.LABEL";
    public static final String EXTRA_START_TIMER = "com.philliphsu.clock2.edittimer.extra.START_TIMER";

    private AddLabelDialogController mAddLabelDialogController;

    @BindView(R.id.edit_fields_layout) ViewGroup mEditFieldsLayout;
    @BindView(R.id.label) TextView mLabel;
    @BindView(R.id.hour) EditText mHour;
    @BindView(R.id.minute) EditText mMinute;
    @BindView(R.id.second) EditText mSecond;
    @BindView(R.id.hour_label) TextView mHourLabel;
    @BindView(R.id.minute_label) TextView mMinuteLabel;
    @BindView(R.id.second_label) TextView mSecondLabel;
    @BindView(R.id.focus_grabber) View mFocusGrabber;
    @BindView(R.id.fab) FloatingActionButton mFab;
    // Intentionally not using a (subclass of) GridLayoutNumpad, because
    // it is expedient to not adapt it for timers.
    @BindView(R.id.numpad) GridLayout mNumpad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mLabel.setText(savedInstanceState.getCharSequence(KEY_LABEL));
        }
        mAddLabelDialogController = new AddLabelDialogController(getSupportFragmentManager(), this);
        mAddLabelDialogController.tryRestoreCallback(makeTag(R.id.label));
    }

    @Override
    protected int layoutResId() {
        return R.layout.activity_edit_timer;
    }

    @Override
    protected int menuResId() {
        // TODO: Define a menu res with a save item
        return 0;
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onLabelSet(String label) {
        mLabel.setText(label);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(KEY_LABEL, mLabel.getText());
    }

    @OnClick({ R.id.zero, R.id.one, R.id.two, R.id.three, R.id.four,
            R.id.five, R.id.six, R.id.seven, R.id.eight, R.id.nine })
    void onClick(TextView view) {
        if (mFocusGrabber.isFocused())
            return;
        EditText field = getFocusedField();
        int at = field.getSelectionStart();
        field.getText().replace(at, at + 1, view.getText());
        field.setSelection(at + 1);
//        updateStartButtonVisibility();
        if (field.getSelectionStart() == FIELD_LENGTH) {
            // At the end of the current field, so try to focus to the next field.
            // The search will return null if no view can be focused next.
            View next = field.focusSearch(View.FOCUS_RIGHT);
            if (next != null) {
                next.requestFocus();
                if (next instanceof EditText) {
                    // Should always start off at the beginning of the field
                    ((EditText) next).setSelection(0);
                }
            }
        }
    }

    @OnTouch({ R.id.hour, R.id.minute, R.id.second })
    boolean switchField(EditText field, MotionEvent event) {
        int inType = field.getInputType(); // backup the input type
        field.setInputType(InputType.TYPE_NULL); // disable soft input
        boolean result = field.onTouchEvent(event); // call native handler
        field.setInputType(inType); // restore input type (to show cursor)
        return result;
    }

    @OnClick(R.id.backspace)
    void backspace() {
        if (mFocusGrabber.isFocused()) {
            mEditFieldsLayout.focusSearch(mFocusGrabber, View.FOCUS_LEFT).requestFocus();
        }
        EditText field = getFocusedField();
        if (field == null)
            return;
        int at = field.getSelectionStart();
        if (at == 0) {
            // At the beginning of current field, so move focus
            // to the preceding field
            View prev = field.focusSearch(View.FOCUS_LEFT);
            if (null == prev) {
                // Reached the beginning of the hours field
                return;
            }
            if (prev.requestFocus()) {
                if (prev instanceof EditText) {
                    // Always move the cursor to the end when moving focus back
                    ((EditText) prev).setSelection(FIELD_LENGTH);
                }
                // Recursively backspace on the newly focused field
                backspace();
            }
        } else {
            field.getText().replace(at - 1, at, "0");
            field.setSelection(at - 1);
//            updateStartButtonVisibility();
        }
    }
    
    @OnLongClick(R.id.backspace)
    boolean clear() {
        mHour.setText("00");
        mMinute.setText("00");
        mSecond.setText("00");
        mHour.requestFocus(); // TOneverDO: call after setSelection(0), or else the cursor returns to the end of the text
        mHour.setSelection(0); // always move the cursor WHILE the field is focused, NEVER focus after!
        mMinute.setSelection(0);
        mSecond.setSelection(0);
//        mFab.hide(); // instead of updateStartButtonVisibility() because we know everything's zero
        return true;
    }

    @OnClick(R.id.label)
    void openEditLabelDialog() {
        mAddLabelDialogController.show(mLabel.getText(), makeTag(R.id.label));
    }

    @OnClick(R.id.fab)
    void startTimer() {
        int hour = Integer.parseInt(mHour.getText().toString());
        int minute = Integer.parseInt(mMinute.getText().toString());
        int second = Integer.parseInt(mSecond.getText().toString());
        if (hour == 0 && minute == 0 && second == 0)
            return; // TODO: we could show a toast instead if we cared
        // TODO: Consider overriding finish() and doing this there.
        // TODO: Timer's group?
        Intent data = new Intent()
                .putExtra(EXTRA_HOUR, hour)
                .putExtra(EXTRA_MINUTE, minute)
                .putExtra(EXTRA_SECOND, second)
                .putExtra(EXTRA_LABEL, mLabel.getText().toString())
                .putExtra(EXTRA_START_TIMER, true);
        setResult(RESULT_OK, data);
        finish();
    }

    private EditText getFocusedField() {
        return (EditText) mEditFieldsLayout.findFocus();
    }

    private static String makeTag(@IdRes int viewId) {
        return FragmentTagUtils.makeTag(EditTimerActivity.class, viewId);
    }
}
