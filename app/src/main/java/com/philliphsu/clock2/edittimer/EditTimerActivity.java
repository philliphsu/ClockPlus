package com.philliphsu.clock2.edittimer;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayout;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.philliphsu.clock2.BaseActivity;
import com.philliphsu.clock2.R;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.OnTouch;

// TODO: Rename to CreateTimerActivity
public class EditTimerActivity extends BaseActivity {
    private static final int FIELD_LENGTH = 2;

    @Bind(R.id.appbar) ViewGroup mAppBar;
    @Bind(R.id.label) TextView mLabel;
    @Bind(R.id.hour) EditText mHour;
    @Bind(R.id.minute) EditText mMinute;
    @Bind(R.id.second) EditText mSecond;
    @Bind(R.id.hour_label) TextView mHourLabel;
    @Bind(R.id.minute_label) TextView mMinuteLabel;
    @Bind(R.id.second_label) TextView mSecondLabel;
    @Bind(R.id.focus_grabber) View mFocusGrabber;
    @Bind(R.id.fab) FloatingActionButton mFab;
    // Intentionally not using a (subclass of) GridLayoutNumpad, because
    // it is expedient to refrain from adapting it for timers.
    @Bind(R.id.numpad) GridLayout mNumpad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        updateStartButtonVisibility();
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
            mAppBar.focusSearch(mFocusGrabber, View.FOCUS_LEFT).requestFocus();
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
        // TODO: Show the edit label alert dialog.
    }

    @OnClick(R.id.fab)
    void startTimer() {
        int hour = Integer.parseInt(mHour.getText().toString());
        int minute = Integer.parseInt(mMinute.getText().toString());
        int second = Integer.parseInt(mSecond.getText().toString());
        if (hour == 0 && minute == 0 && second == 0)
            return; // TODO: we could show a toast instead if we cared
        // TODO: do something with the label
        mLabel.getText();
        // TODO: Pass back an intent with the data, or make Timer parcelable
        // and pass back an instance of Timer. Consider overriding finish()
        // and doing it there.
        finish();
    }

    private EditText getFocusedField() {
        return (EditText) mAppBar.findFocus();
    }

//    private void updateStartButtonVisibility() {
//        // TODO: parse the field's text to an integer and check > 0 instead?
//        if (TextUtils.equals(mHour.getText(), "00")
//                && TextUtils.equals(mMinute.getText(), "00")
//                && TextUtils.equals(mSecond.getText(), "00")) {
//            mFab.hide();
//        } else {
//            mFab.show();
//        }
//    }
}
