package com.philliphsu.clock2.editalarm;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.design.widget.FloatingActionButton;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.philliphsu.clock2.Numpad;
import com.philliphsu.clock2.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Phillip Hsu on 6/2/2016.
 */
public class AlarmNumpad extends Numpad {
    private static final String TAG = "AlarmNumpad";

    // Time can be represented with maximum of 4 digits
    private static final int MAX_DIGITS = 4;

    // Formatted time string has a maximum of 8 characters
    // in the 12-hour clock, e.g 12:59 AM. Although the 24-hour
    // clock should be capped at 5 characters, the difference
    // is not significant enough to deal with the separate cases.
    private static final int MAX_CHARS = 8;

    private static final int UNSPECIFIED = -1;
    private static final int AM = 0;
    private static final int PM = 1;
    private static final int HRS_24 = 2;

    @IntDef({ UNSPECIFIED, AM, PM, HRS_24 }) // Specifies the accepted constants
    @Retention(RetentionPolicy.SOURCE) // Usages do not need to be recorded in .class files
    private @interface AmPmState {}

    private Button leftAlt;  // AM or :00
    private Button rightAlt; // PM or :30
    private FloatingActionButton fab;
    private final StringBuilder mFormattedInput = new StringBuilder(MAX_CHARS);

    @AmPmState
    private int mAmPmState = UNSPECIFIED;

    public AlarmNumpad(Context context) {
        this(context, null);
    }

    public AlarmNumpad(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /** Returns the hour of day (0-23) regardless of clock system */
    public int getHours() {
        if (!checkTimeValid())
            throw new IllegalStateException("Cannot call getHours() until legal time inputted");
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

    public int getMinutes() {
        if (!checkTimeValid())
            throw new IllegalStateException("Cannot call getMinutes() until legal time inputted");
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

    @Override
    protected int capacity() {
        return MAX_DIGITS;
    }

    @Override
    protected void onClick(Button button) {
        super.onClick(button);
        // Format and store the input as text
        inputNumber(button.getText());
        notifyOnNumberInputListener(mFormattedInput.toString());
        updateNumpadStates();
    }

    @Override
    protected void backspace() {
        int len = mFormattedInput.length();
        if (!is24HourFormat() && mAmPmState != UNSPECIFIED) {
            mAmPmState = UNSPECIFIED;
            // Delete starting from index of space to end
            mFormattedInput.delete(mFormattedInput.indexOf(" "), len);
        } else {
            super.backspace();
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

        notifyOnBackspaceListener(mFormattedInput.toString());
        updateNumpadStates();
    }

    @Override
    protected boolean longBackspace() {
        boolean consumed = super.longBackspace();
        mFormattedInput.delete(0, mFormattedInput.length());
        notifyOnLongBackspaceListener();
        updateNumpadStates();
        mAmPmState = UNSPECIFIED;
        return consumed;
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

        // Only if on 24-hour clock, zero-pad single digit hours.
        // Zero cannot be the first digit of any time in the 12-hour clock.
        String strHrs = is24HourFormat()
                ? String.format("%02d", hours)
                : String.valueOf(hours);
        String strMins = String.format("%02d", minutes);  // Zero-pad single digit minutes

        // TODO: Do this off the main thread
        for (int i = 0; i < strHrs.length(); i++)
            performClick(strHrs.charAt(i));
        for (int i = 0; i < strMins.length(); i++)
            performClick(strMins.charAt(i));

        mAmPmState = amPmState;
        if (mAmPmState != HRS_24) {
            onAltButtonClick(mAmPmState == AM ? leftAlt : rightAlt);
        }
    }

    public String getTime() {
        return mFormattedInput.toString();
    }

    private void updateAltButtonStates() {
        if (count() == 0) {
            // No input, no access!
            leftAlt.setEnabled(false);
            rightAlt.setEnabled(false);
        } else if (count() == 1) {
            // Any of 0-9 inputted, always have access in either clock.
            leftAlt.setEnabled(true);
            rightAlt.setEnabled(true);
        } else if (count() == 2) {
            // Any 2 digits that make a valid hour for either clock are eligible for access
            int time = getInput();
            boolean validTwoDigitHour = is24HourFormat() ? time <= 23 : time >= 10 && time <= 12;
            leftAlt.setEnabled(validTwoDigitHour);
            rightAlt.setEnabled(validTwoDigitHour);
        } else if (count() == 3) {
            if (is24HourFormat()) {
                // For the 24-hour clock, no access at all because
                // two more digits (00 or 30) cannot be added to 3 digits.
                leftAlt.setEnabled(false);
                rightAlt.setEnabled(false);
            } else {
                // True for any 3 digits, if AM/PM not already entered
                boolean enabled = mAmPmState == UNSPECIFIED;
                leftAlt.setEnabled(enabled);
                rightAlt.setEnabled(enabled);
            }
        } else if (count() == MAX_DIGITS) {
            // If all 4 digits are filled in, the 24-hour clock has absolutely
            // no need for the alt buttons. However, The 12-hour clock has
            // complete need of them, if not already used.
            boolean enabled = !is24HourFormat() && mAmPmState == UNSPECIFIED;
            leftAlt.setEnabled(enabled);
            rightAlt.setEnabled(enabled);
        }
    }

    private void updateBackspaceState() {
        setBackspaceEnabled(count() > 0);
    }

    private void updateFabState() {
        // Special case of 0 digits is legal (that is the default hint time)
        if (count() == 0) {
            fab.setEnabled(true);
            return;
        }
        // Minimum of 3 digits is required
        if (count() < 3) {
            fab.setEnabled(false);
            return;
        }

        if (is24HourFormat()) {
            int time = getInput();
            fab.setEnabled(time % 100 <= 59);
        } else {
            // If on 12-hour clock, FAB will never be enabled
            // until AM or PM is explicitly clicked.
            fab.setEnabled(mAmPmState != UNSPECIFIED);
        }
    }

    private void updateNumpadStates() {
        updateAltButtonStates();
        updateFabState();
        updateBackspaceState();
        updateNumberKeysStates();
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

    // Helper method for inputting each character of an altBtn.
    private void onAltButtonClick(Button altBtn) {
        if (leftAlt != altBtn && rightAlt != altBtn)
            throw new IllegalArgumentException("Not called with one of the alt buttons");

        // Manually insert special characters for 12-hour clock
        if (!is24HourFormat()) {
            if (count() <= 2) {
                // The colon is inserted for you
                performClick(0);
                performClick(0);
            }
            // text is AM or PM, so include space before
            mFormattedInput.append(' ').append(altBtn.getText());
            mAmPmState = leftAlt == altBtn ? AM : PM;
            // Digits will be shown for you on click, but not AM/PM
            notifyOnNumberInputListener(mFormattedInput.toString());
        } else {
            // Need to consider each character individually,
            // as we are only interested in storing the digits
            CharSequence text = altBtn.getText();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (Character.isDigit(c)) {
                    // Convert char to digit and perform click
                    // Colon is added for you
                    performClick(c);
                }
            }
            mAmPmState = HRS_24;
        }

        updateNumpadStates();
    }

    private boolean is24HourFormat() {
        return DateFormat.is24HourFormat(getContext());
    }

    private void inputNumber(CharSequence number) {
        mFormattedInput.append(number);
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
            // Colon needs to move, so remove the colon previously added
            mFormattedInput.deleteCharAt(mFormattedInput.indexOf(":"));
            mFormattedInput.insert(2, ':');

            // Time is legal in 24-hour clock
            if (is24HourFormat()) {
                mAmPmState = HRS_24;
            }
        }

        // Moved to onClick()
        //notifyOnNumberInputListener(mFormattedInput.toString());
    }

    public interface KeyListener extends Numpad.KeyListener {
        void onAcceptChanges();
    }

    private void init() {
        // Build alternative action buttons
        leftAlt = (Button) buildButton(R.layout.numpad_alt_button, 3, 0);
        rightAlt = (Button) buildButton(R.layout.numpad_alt_button, 3, 2);
        leftAlt.setText(is24HourFormat() ? R.string.left_alt_24hr : R.string.am);
        rightAlt.setText(is24HourFormat() ? R.string.right_alt_24hr : R.string.pm);

        leftAlt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onAltButtonClick(leftAlt);
            }
        });

        rightAlt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onAltButtonClick(rightAlt);
            }
        });

        newRow();
        buildBackspace(getChildCount() - 1, 2);
        buildCollapse(getChildCount() - 1, 0);
        // The FAB is wrapped in a FrameLayout
        FrameLayout frame = (FrameLayout)
                buildButton(R.layout.numpad_fab, getChildCount() - 1, 1);
        fab = (FloatingActionButton) frame.getChildAt(0);
        fab.setBackgroundTintList(getResources().getColorStateList(R.color.on_enabled_change_fab));

        fab.setEnabled(false);

        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (count() == 0) {
                    // Default time
                    setTime(0, 0);
                }
                KeyListener kl;
                try {
                    kl = (KeyListener) getKeyListener();
                } catch (ClassCastException e) {
                    throw new ClassCastException("Using AlarmNumpad with Numpad.KeyListener instead of AlarmNumpad.KeyListener");
                } catch (NullPointerException e) {
                    throw new NullPointerException("Numpad's KeyListener is not set");
                }
                kl.onAcceptChanges();
            }
        });

        updateNumpadStates();
    }
}
