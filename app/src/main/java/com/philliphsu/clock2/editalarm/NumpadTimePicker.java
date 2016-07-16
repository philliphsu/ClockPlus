package com.philliphsu.clock2.editalarm;

import android.content.Context;
import android.support.annotation.IntDef;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.Button;

import com.philliphsu.clock2.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.DateFormatSymbols;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by Phillip Hsu on 7/12/2016.
 */
public class NumpadTimePicker extends GridLayoutNumpad implements TimePicker {
    // Time can be represented with maximum of 4 digits
    private static final int MAX_DIGITS = 4;

    // Formatted time string has a maximum of 8 characters
    // in the 12-hour clock, e.g 12:59 AM. Although the 24-hour
    // clock should be capped at 5 characters, the difference
    // is not significant enough to deal with the separate cases.
    private static final int MAX_CHARS = 8;

    // Constant for converting text digits to numeric digits in base-10.
    private static final int BASE_10 = 10;

    // AmPmStates
    private static final int UNSPECIFIED = -1;
    private static final int AM = 0;
    private static final int PM = 1;
    private static final int HRS_24 = 2;

    @IntDef({ UNSPECIFIED, AM, PM, HRS_24 }) // Specifies the accepted constants
    @Retention(RetentionPolicy.SOURCE) // Usages do not need to be recorded in .class files
    private @interface AmPmState {}

    @AmPmState
    private int mAmPmState = UNSPECIFIED;
    private final StringBuilder mFormattedInput = new StringBuilder(MAX_CHARS);

    @Bind({ R.id.leftAlt, R.id.rightAlt })
    Button[] mAltButtons;

    public NumpadTimePicker(Context context) {
        super(context);
        init();
    }

    public NumpadTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    @Override
    protected int capacity() {
        return MAX_DIGITS;
    }

    @Override
    protected int contentLayout() {
        return R.layout.content_numpad_time_picker;
    }

    @Override
    protected void onDigitInserted(String newDigit) {
        // Append the new digit(s) to the formatter
        updateFormattedInputOnDigitInserted(newDigit);
        super.onDigitInserted(mFormattedInput.toString());
        updateNumpadStates();
    }

    @Override
    protected void onDigitDeleted(String newStr) {
        updateFormattedInputOnDigitDeleted();
        super.onDigitDeleted(mFormattedInput.toString());
        updateNumpadStates();
    }

    @Override
    protected void onDigitsCleared() {
        mFormattedInput.delete(0, mFormattedInput.length());
        updateNumpadStates();
        mAmPmState = UNSPECIFIED;
        super.onDigitsCleared();
    }

    @Override
    public void delete() {
        int len = mFormattedInput.length();
        if (!is24HourFormat() && mAmPmState != UNSPECIFIED) {
            mAmPmState = UNSPECIFIED;
            // Delete starting from index of space to end
            mFormattedInput.delete(mFormattedInput.indexOf(" "), len);
            // No digit was actually deleted, but we have to notify the
            // listener to update its output.
/*TOneverDO: remove super*/super.onDigitDeleted(mFormattedInput.toString());
            updateNumpadStates();
        } else {
            super.delete();
        }
    }

    /** Returns the hour of day (0-23) regardless of clock system */
    @Override
    public int hourOfDay() {
        if (!checkTimeValid())
            throw new IllegalStateException("Cannot call hourOfDay() until legal time inputted");
        int hours = count() < 4 ? valueAt(0) : valueAt(0) * 10 + valueAt(1);
        if (hours == 12) {
            switch (mAmPmState) {
                case AM:
                    return 0;
                case PM:
                case HRS_24:
                    return 12;
                default:
                    break;
            }
        }

        // AM/PM clock needs value offset
        return hours + (mAmPmState == PM ? 12 : 0);
    }

    @Override
    public int minute() {
        if (!checkTimeValid())
            throw new IllegalStateException("Cannot call minute() until legal time inputted");
        return count() < 4 ? valueAt(1) * 10 + valueAt(2) : valueAt(2) * 10 + valueAt(3);
    }

    /**
     * Checks if the input stored so far qualifies as a valid time.
     * For this to return {@code true}, the hours, minutes AND AM/PM
     * state must be set.
     */
    public boolean checkTimeValid() {
        if (mAmPmState == UNSPECIFIED || mAmPmState == HRS_24 && count() < 3)
            return false;
        // AM or PM can only be set if the time was already valid previously, so we don't need
        // to check for them.
        return true;
    }

    public void setTime(int hours, int minutes) {
        if (hours < 0 || hours > 23)
            throw new IllegalArgumentException("Illegal hours: " + hours);
        if (minutes < 0 || minutes > 59)
            throw new IllegalArgumentException("Illegal minutes: " + minutes);

        // Internal representation of the time has been checked for legality.
        // Now we need to format it depending on the user's clock system.
        // If 12-hour clock, can't set mAmPmState yet or else this interferes
        // with the button state update mechanism. Instead, cache the state
        // the hour would resolve to in a local variable and set it after
        // all digits are inputted.
        int amPmState;
        if (!is24HourFormat()) {
            // Convert 24-hour times into 12-hour compatible times.
            if (hours == 0) {
                hours = 12;
                amPmState = AM;
            } else if (hours == 12) {
                amPmState = PM;
            } else if (hours > 12) {
                hours -= 12;
                amPmState = PM;
            } else {
                amPmState = AM;
            }
        } else {
            amPmState = HRS_24;
        }

        /*
        // Convert the hour and minutes into text form, so that
        // we can read each digit individually.
        // Only if on 24-hour clock, zero-pad single digit hours.
        // Zero cannot be the first digit of any time in the 12-hour clock.
        String textDigits = is24HourFormat()
                ? String.format("%02d", hours)
                : String.valueOf(hours);
        textDigits += String.format("%02d", minutes);

        int[] digits = new int[textDigits.length()];
        for (int i = 0; i < textDigits.length(); i++) {
            digits[i] = Character.digit(textDigits.charAt(i), BASE_10);
        }
        insertDigits(digits);
        */

        if (is24HourFormat() || hours > 9) {
            insertDigits(hours / 10, hours % 10, minutes / 10, minutes % 10);
        } else {
            insertDigits(hours, minutes / 10, minutes % 10);
        }

        mAmPmState = amPmState;
        if (mAmPmState != HRS_24) {
            onAltButtonClick(mAmPmState == AM ? mAltButtons[0] : mAltButtons[1]);
        }
    }

    public String getTime() {
        return mFormattedInput.toString();
    }

    private void init() {
        if (is24HourFormat()) {
            mAltButtons[0].setText(R.string.left_alt_24hr);
            mAltButtons[1].setText(R.string.right_alt_24hr);
        } else {
            String[] amPmTexts = new DateFormatSymbols().getAmPmStrings();
            mAltButtons[0].setText(amPmTexts[Calendar.AM]);
            mAltButtons[1].setText(amPmTexts[Calendar.PM]);
        }
        updateNumpadStates();
    }

    @OnClick({ R.id.leftAlt, R.id.rightAlt })
    void onAltButtonClick(Button altBtn) {
        if (mAltButtons[0] != altBtn && mAltButtons[1] != altBtn)
            throw new IllegalArgumentException("Not called with one of the alt buttons");

        // Manually insert special characters for 12-hour clock
        if (!is24HourFormat()) {
            if (count() <= 2) {
                // The colon is inserted for you
                insertDigits(0, 0);
            }
            // text is AM or PM, so include space before
            mFormattedInput.append(' ').append(altBtn.getText());
            mAmPmState = mAltButtons[0] == altBtn ? AM : PM;
            // Digits will be shown for you on insert, but not AM/PM
/*TOneverDO: remove super*/super.onDigitInserted(mFormattedInput.toString());
        } else {
            CharSequence text = altBtn.getText();
            int[] digits = new int[text.length() - 1];
            // charAt(0) is the colon, so skip i = 0.
            // We are only interested in storing the digits.
            for (int i = 1; i < text.length(); i++) {
                // The array and the text do not have the same lengths,
                // so the iterator value does not correspond to the
                // array index directly
                digits[i - 1] = Character.digit(text.charAt(i), BASE_10);
            }
            // Colon is added for you
            insertDigits(digits);
            mAmPmState = HRS_24;
        }

        updateNumpadStates();
    }

    private boolean is24HourFormat() {
        return DateFormat.is24HourFormat(getContext());
    }

    private void updateFormattedInputOnDigitInserted(String newDigits) {
        mFormattedInput.append(newDigits);
        // Add colon if necessary, depending on how many digits entered so far
        if (count() == 3) {
            // Insert a colon
            int digits = getInput();
            if (digits >= 60 && digits < 100 || digits >= 160 && digits < 200) {
                // From 060-099 (really only to 095, but might as well go up to 100)
                // From 160-199 (really only to 195, but might as well go up to 200),
                // time does not exist if colon goes at pos. 1
                mFormattedInput.insert(2, ':');
                // These times only apply to the 24-hour clock, and if we're here,
                // the time is not legal yet. So we can't set mAmPmState here for
                // either clock.
                // The 12-hour clock can only have mAmPmState set when AM/PM are clicked.
            } else {
                // A valid time exists if colon is at pos. 1
                mFormattedInput.insert(1, ':');
                // We can set mAmPmState here (and not in the above case) because
                // the time here is legal in 24-hour clock
                if (is24HourFormat()) {
                    mAmPmState = HRS_24;
                }
            }
        } else if (count() == MAX_DIGITS) {
            int colonAt = mFormattedInput.indexOf(":");
            // Since we now batch updating the formatted input whenever
            // digits are inserted, the colon may legitimately not be
            // present in the formatted input when this is initialized.
            if (colonAt != -1) {
                // Colon needs to move, so remove the colon previously added
                mFormattedInput.deleteCharAt(colonAt);
            }
            mFormattedInput.insert(2, ':');

            // Time is legal in 24-hour clock
            if (is24HourFormat()) {
                mAmPmState = HRS_24;
            }
        }
    }

    private void updateFormattedInputOnDigitDeleted() {
        int len = mFormattedInput.length();
        mFormattedInput.delete(len - 1, len);
        if (count() == 3) {
            // Move the colon from its 4-digit position to its 3-digit position,
            // unless doing so gives an invalid time.
            // e.g. 17:55 becomes 1:75, which is invalid.
            // All 3-digit times in the 12-hour clock at this point should be
            // valid. The limits <=155 and (>=200 && <=235) are really only
            // imposed on the 24-hour clock, and were chosen because 4-digit times
            // in the 24-hour clock can only go up to 15:5[0-9] or be within the range
            // [20:00, 23:59] if they are to remain valid when they become three digits.
            // The is24HourFormat() check is therefore unnecessary.
            int value = getInput();
            if (value <= 155 || value >= 200 && value <= 235) {
                mFormattedInput.deleteCharAt(mFormattedInput.indexOf(":"));
                mFormattedInput.insert(1, ":");
            }
        } else if (count() == 2) {
            // Remove the colon
            mFormattedInput.deleteCharAt(mFormattedInput.indexOf(":"));
        }
    }

    private void updateNumpadStates() {
        updateAltButtonStates();
        //updateBackspaceState(); // Backspace is not part of the numpad
        updateNumberKeysStates();
    }

    private void updateAltButtonStates() {
        if (count() == 0) {
            // No input, no access!
            mAltButtons[0].setEnabled(false);
            mAltButtons[1].setEnabled(false);
        } else if (count() == 1) {
            // Any of 0-9 inputted, always have access in either clock.
            mAltButtons[0].setEnabled(true);
            mAltButtons[1].setEnabled(true);
        } else if (count() == 2) {
            // Any 2 digits that make a valid hour for either clock are eligible for access
            int time = getInput();
            boolean validTwoDigitHour = is24HourFormat() ? time <= 23 : time >= 10 && time <= 12;
            mAltButtons[0].setEnabled(validTwoDigitHour);
            mAltButtons[1].setEnabled(validTwoDigitHour);
        } else if (count() == 3) {
            if (is24HourFormat()) {
                // For the 24-hour clock, no access at all because
                // two more digits (00 or 30) cannot be added to 3 digits.
                mAltButtons[0].setEnabled(false);
                mAltButtons[1].setEnabled(false);
            } else {
                // True for any 3 digits, if AM/PM not already entered
                boolean enabled = mAmPmState == UNSPECIFIED;
                mAltButtons[0].setEnabled(enabled);
                mAltButtons[1].setEnabled(enabled);
            }
        } else if (count() == MAX_DIGITS) {
            // If all 4 digits are filled in, the 24-hour clock has absolutely
            // no need for the alt buttons. However, The 12-hour clock has
            // complete need of them, if not already used.
            boolean enabled = !is24HourFormat() && mAmPmState == UNSPECIFIED;
            mAltButtons[0].setEnabled(enabled);
            mAltButtons[1].setEnabled(enabled);
        }
    }

    private void updateNumberKeysStates() {
        int cap = 10; // number of buttons
        boolean is24hours = is24HourFormat();

        if (count() == 0) {
            enable(is24hours ? 0 : 1, cap);
            return;
        } else if (count() == MAX_DIGITS) {
            enable(0, 0);
            return;
        }

        int time = getInput();
        if (is24hours) {
            if (count() == 1) {
                enable(0, time < 2 ? cap : 6);
            } else if (count() == 2) {
                enable(0, time % 10 >= 0 && time % 10 <= 5 ? cap : 6);
            } else if (count() == 3) {
                if (time >= 236) {
                    enable(0, 0);
                } else {
                    enable(0, time % 10 >= 0 && time % 10 <= 5 ? cap : 0);
                }
            }
        } else {
            if (count() == 1) {
                if (time == 0) {
                    throw new IllegalStateException("12-hr format, zeroth digit = 0?");
                } else {
                    enable(0, 6);
                }
            } else if (count() == 2 || count() == 3) {
                if (time >= 126) {
                    enable(0, 0);
                } else {
                    if (time >= 100 && time <= 125 && mAmPmState != UNSPECIFIED) {
                        // Could legally input fourth digit, if not for the am/pm state already set
                        enable(0, 0);
                    } else {
                        enable(0, time % 10 >= 0 && time % 10 <= 5 ? cap : 0);
                    }
                }
            }
        }
    }
}
