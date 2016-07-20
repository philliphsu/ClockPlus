package com.philliphsu.clock2.editalarm;

import android.view.View;

/**
 * Created by Phillip Hsu on 7/20/2016.
 *
 * TODO: This class has NOT been properly written yet. Consider moving all button state code
 * from the Numpad classes to here.
 * TODO: We might need to write a setAmPmState() method in NumpadTimePicker.
 *
 * NumpadTimePickerDialog would store a reference to this controller and use this to
 * update the states of the various buttons in its layout. Currently, this class has
 * ported over updateNumpadStates() and its related methods from NumpadTimePicker.
 */
public class NumpadTimePickerController {
    
    private NumpadTimePicker mPicker;
    private View mConfirmSelectionButton;
    private View mLeftAltButton;
    private View mRightAltButton;
    private boolean mIs24HourMode;

/*
    public NumpadTimePickerController(NumpadTimePicker picker, View confirmSelectionButton, 
                                      View leftAltButton, View rightAltButton, boolean is24HourMode) {
        mPicker = picker;
        mConfirmSelectionButton = confirmSelectionButton;
        mLeftAltButton = leftAltButton;
        mRightAltButton = rightAltButton;
        mIs24HourMode = is24HourMode;
    }

    public void updateNumpadStates() {
        // TOneverDO: after updateNumberKeysStates(), esp. if clock is 12-hour,
        // because it calls mPicker.enable(0, 0), which checks if the alt buttons have been
        // disabled as well before firing the onInputDisabled().
        updateAltButtonStates();

        updateBackspaceState();
        updateNumberKeysStates();
        updateFabState();
    }

    public void updateFabState() {
        mConfirmSelectionButton.setEnabled(mPicker.checkTimeValid());
    }

    public void updateBackspaceState() {
        mPicker.setBackspaceEnabled(mPicker.count() > 0);
    }

    public void updateAltButtonStates() {
        if (mPicker.count() == 0) {
            // No input, no access!
            mLeftAltButton.setEnabled(false);
            mRightAltButton.setEnabled(false);
        } else if (mPicker.count() == 1) {
            // Any of 0-9 inputted, always have access in either clock.
            mLeftAltButton.setEnabled(true);
            mRightAltButton.setEnabled(true);
        } else if (mPicker.count() == 2) {
            // Any 2 digits that make a valid hour for either clock are eligible for access
            int time = mPicker.getInput();
            boolean validTwoDigitHour = mIs24HourMode ? time <= 23 : time >= 10 && time <= 12;
            mLeftAltButton.setEnabled(validTwoDigitHour);
            mRightAltButton.setEnabled(validTwoDigitHour);
        } else if (mPicker.count() == 3) {
            if (mIs24HourMode) {
                // For the 24-hour clock, no access at all because
                // two more digits (00 or 30) cannot be added to 3 digits.
                mLeftAltButton.setEnabled(false);
                mRightAltButton.setEnabled(false);
            } else {
                // True for any 3 digits, if AM/PM not already entered
                boolean enabled = mAmPmState == UNSPECIFIED;
                mLeftAltButton.setEnabled(enabled);
                mRightAltButton.setEnabled(enabled);
            }
        } else if (mPicker.count() == mPicker.capacity()) {
            // If all 4 digits are filled in, the 24-hour clock has absolutely
            // no need for the alt buttons. However, The 12-hour clock has
            // complete need of them, if not already used.
            boolean enabled = !mIs24HourMode && mAmPmState == UNSPECIFIED;
            mLeftAltButton.setEnabled(enabled);
            mRightAltButton.setEnabled(enabled);
        }
    }

    public void updateNumberKeysStates() {
        int cap = 10; // number of buttons
        boolean is24hours = mIs24HourMode;

        if (mPicker.count() == 0) {
            mPicker.enable(is24hours ? 0 : 1, cap);
            return;
        } else if (mPicker.count() == mPicker.capacity()) {
            mPicker.enable(0, 0);
            return;
        }

        int time = mPicker.getInput();
        if (is24hours) {
            if (mPicker.count() == 1) {
                mPicker.enable(0, time < 2 ? cap : 6);
            } else if (mPicker.count() == 2) {
                mPicker.enable(0, time % 10 >= 0 && time % 10 <= 5 ? cap : 6);
            } else if (mPicker.count() == 3) {
                if (time >= 236) {
                    mPicker.enable(0, 0);
                } else {
                    mPicker.enable(0, time % 10 >= 0 && time % 10 <= 5 ? cap : 0);
                }
            }
        } else {
            if (mPicker.count() == 1) {
                if (time == 0) {
                    throw new IllegalStateException("12-hr format, zeroth digit = 0?");
                } else {
                    mPicker.enable(0, 6);
                }
            } else if (mPicker.count() == 2 || mPicker.count() == 3) {
                if (time >= 126) {
                    mPicker.enable(0, 0);
                } else {
                    if (time >= 100 && time <= 125 && mAmPmState != UNSPECIFIED) {
                        // Could legally input fourth digit, if not for the am/pm state already set
                        mPicker.enable(0, 0);
                    } else {
                        mPicker.enable(0, time % 10 >= 0 && time % 10 <= 5 ? cap : 0);
                    }
                }
            }
        }
    }
*/

}
