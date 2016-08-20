/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.philliphsu.clock2.editalarm;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

/**
 * Created by Phillip Hsu on 8/17/2016.
 *
 * A derivative of the AOSP datetimepicker RadialPickerLayout class.
 */
public class GridSelectorLayout extends FrameLayout implements NumbersGrid.OnNumberSelectedListener {
    private static final String TAG = "GridSelectorLayout";

    // Delay before auto-advancing the page, in ms.
    // TODO: If we animate the page change, then we don't need this delay. This was
    // my own logic, not ported from AOSP timepicker.
    public static final int ADVANCE_PAGE_DELAY = 150;

    private static final int HOUR_INDEX = NumberGridTimePickerDialog.HOUR_INDEX;
    private static final int MINUTE_INDEX = NumberGridTimePickerDialog.MINUTE_INDEX;
    // TODO: Rename to HALF_DAY_INDEX?
    private static final int AMPM_INDEX = NumberGridTimePickerDialog.AMPM_INDEX;
    private static final int HALF_DAY_1 = NumberGridTimePickerDialog.HALF_DAY_1;
    private static final int HALF_DAY_2 = NumberGridTimePickerDialog.HALF_DAY_2;

    private OnValueSelectedListener mListener;
    private boolean mTimeInitialized;
    private int mCurrentHoursOfDay;
    private int mCurrentMinutes;
    private boolean mIs24HourMode;
    private int mCurrentItemShowing;

    private HoursGrid mHoursGrid = null;
    private TwentyFourHoursGrid m24HoursGrid = null;
    private MinutesGrid mMinutesGrid;
    private final Handler mHandler = new Handler();

    public interface OnValueSelectedListener {
        void onValueSelected(int pickerIndex, int newValue, boolean autoAdvance);
    }

    public GridSelectorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // TODO: Why do we need a Context param? RadialPickerLayout does it too.
    public void initialize(Context context, int initialHoursOfDay, int initialMinutes, boolean is24HourMode) {
        if (mTimeInitialized) {
            Log.e(TAG, "Time has already been initialized.");
            return;
        }

        mIs24HourMode = is24HourMode;
        if (is24HourMode) {
            m24HoursGrid = new TwentyFourHoursGrid(context);
            m24HoursGrid.initialize(this/*OnNumberSelectedListener*/);
            addView(m24HoursGrid);
        } else {
            mHoursGrid = new HoursGrid(context);
            mHoursGrid.initialize(this/*OnNumberSelectedListener*/);
            addView(mHoursGrid);
        }
        mMinutesGrid = new MinutesGrid(context);
        mMinutesGrid.initialize(this/*OnNumberSelectedListener*/);

        // Initialize the currently-selected hour and minute.
        setValueForItem(HOUR_INDEX, initialHoursOfDay);
        setValueForItem(MINUTE_INDEX, initialMinutes);

        // Record the selected values in the number grids.
        if (!is24HourMode) {
            initialHoursOfDay = initialHoursOfDay % 12;
            if (initialHoursOfDay == 0) {
                initialHoursOfDay = 12;
            }
            mHoursGrid.setSelection(initialHoursOfDay);
        } else {
            m24HoursGrid.setSelection(initialHoursOfDay);
        }
        mMinutesGrid.setSelection(initialMinutes);

        mTimeInitialized = true;
    }

    void setTheme(Context context, boolean themeDark) {
        // TODO: This logic needs to be in the Dialog class, since the am/pm view is contained there.
//        mAmPmView.setTheme(context, themeDark);

        // TODO: These aren't doing much currently, if at all.
        if (m24HoursGrid != null) {
            m24HoursGrid.setTheme(context, themeDark);
        } else if (mHoursGrid != null) {
            mHoursGrid.setTheme(context, themeDark);
        }
        mMinutesGrid.setTheme(context, themeDark);
    }

    public void setTime(int hours, int minutes) {
        setItem(HOUR_INDEX, hours);
        setItem(MINUTE_INDEX, minutes);
    }

    /**
     * Set either the hour or the minute. Will set the internal value, and set the selection.
     */
    private void setItem(int index, int value) {
        if (index == HOUR_INDEX) {
            setValueForItem(HOUR_INDEX, value);
            if (mIs24HourMode) {
                m24HoursGrid.setSelection(value);
            } else {
                mHoursGrid.setSelection(value);
            }
        } else if (index == MINUTE_INDEX) {
            setValueForItem(MINUTE_INDEX, value);
            mMinutesGrid.setSelection(value);
        }
    }

    public void setOnValueSelectedListener(OnValueSelectedListener listener) {
        mListener = listener;
    }

    /**
     * Get the item (hours or minutes) that is currently showing.
     */
    public int getCurrentItemShowing() {
        if (mCurrentItemShowing != HOUR_INDEX && mCurrentItemShowing != MINUTE_INDEX) {
            Log.e(TAG, "Current item showing was unfortunately set to "+mCurrentItemShowing);
            return -1;
        }
        return mCurrentItemShowing;
    }

    /**
     * Set either minutes or hours as showing.
     * @param animate True to animate the transition, false to show with no animation.
     */
    public void setCurrentItemShowing(int index, boolean animate) {
        if (index != HOUR_INDEX && index != MINUTE_INDEX) {
            Log.e(TAG, "TimePicker does not support view at index "+index);
            return;
        }

        int lastIndex = getCurrentItemShowing();
        mCurrentItemShowing = index;

        if (index != lastIndex) {
            removeViewAt(0); // We could also call removeAllViews(), since we only have one child.
            // We already verified that the index is either HOUR_INDEX or MINUTE_INDEX
            addView(index == HOUR_INDEX ?
                    (mIs24HourMode ? m24HoursGrid : mHoursGrid)
                    : mMinutesGrid);
        }
    }

    // TODO: The Dialog should be telling us that AM/PM was selected, via setAmOrPm().
//    @Override
//    public void onAmPmSelected(int amOrPm) {
//        setValueForItem(AMPM_INDEX, amOrPm);
//        mListener.onValueSelected(AMPM_INDEX, amOrPm, false);
//    }

    @Override
    public void onNumberSelected(int number) {
        if (getCurrentItemShowing() == HOUR_INDEX && !mIs24HourMode) {
            // Change the value before passing it through the callback
            int amOrPm = getIsCurrentlyAmOrPm();
            if (amOrPm == HALF_DAY_1 && number == 12) {
                number = 0;
            } else if (amOrPm == HALF_DAY_2 && number != 12) {
                number += 12;
            }
        }

//            final int value = number;
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mListener.onValueSelected(HOUR_INDEX, value, true);
//                }
//            }, ADVANCE_PAGE_DELAY);
//            mListener.onValueSelected(HOUR_INDEX, value, true);
//        } else {
//            mListener.onValueSelected(getCurrentItemShowing(), number, false);
//        }
        setValueForItem(getCurrentItemShowing(), number);
        mListener.onValueSelected(getCurrentItemShowing(), number,
                true/*autoAdvance, not considered for MINUTE_INDEX*/);
    }

    public int getHours() {
        return mCurrentHoursOfDay;
    }

    public int getMinutes() {
        return mCurrentMinutes;
    }

    /**
     * If the hours are showing, return the current hour. If the minutes are showing, return the
     * current minute.
     */
    private int getCurrentlyShowingValue() {
        int currentIndex = getCurrentItemShowing();
        if (currentIndex == HOUR_INDEX) {
            return mCurrentHoursOfDay;
        } else if (currentIndex == MINUTE_INDEX) {
            return mCurrentMinutes;
        } else {
            return -1;
        }
    }

    public int getIsCurrentlyAmOrPm() {
        if (mCurrentHoursOfDay < 12) {
            return HALF_DAY_1;
        } else if (mCurrentHoursOfDay < 24) {
            return HALF_DAY_2;
        }
        return -1;
    }

    /**
     * Set the internal as either AM or PM.
     */
    // TODO: Rename to setHalfDay
    public void setAmOrPm(int amOrPm) {
        final int initialHalfDay = getIsCurrentlyAmOrPm();
        setValueForItem(AMPM_INDEX, amOrPm);
        if (amOrPm != initialHalfDay
                && mIs24HourMode
//                && getCurrentItemShowing() == HOUR_INDEX
                && m24HoursGrid != null) {
            m24HoursGrid.swapTexts();
            mListener.onValueSelected(HOUR_INDEX, mCurrentHoursOfDay, false);
        }
    }

    /**
     * Set the internal value for the hour, minute, or AM/PM.
     */
    private void setValueForItem(int index, int value) {
        if (index == HOUR_INDEX) {
            mCurrentHoursOfDay = value;
        } else if (index == MINUTE_INDEX){
            mCurrentMinutes = value;
        } else if (index == AMPM_INDEX) {
            if (value == HALF_DAY_1) {
                mCurrentHoursOfDay = mCurrentHoursOfDay % 12;
            } else if (value == HALF_DAY_2) {
                mCurrentHoursOfDay = (mCurrentHoursOfDay % 12) + 12;
            }
        }
    }
}
