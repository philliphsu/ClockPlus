package com.philliphsu.clock2.settings;

import android.os.Bundle;

import com.philliphsu.clock2.BaseActivity;
import com.philliphsu.clock2.R;

/**
 * Created by Phillip Hsu on 6/6/2016.
 */
public class SettingsActivity extends BaseActivity {
    /*
     * TODO: Define these keys as string resources instead and then delete these.
     * TODO: Move the existing string resources for the preferences below from strings.xml to prefs.xml
     */
    // World Clock preference keys
    public static final String KEY_PREF_SHOW_TIME_OFFSETS_FROM = "pref_show_time_offsets_from";
    // Alarms preference keys
    public static final String KEY_PREF_TIME_PICKER_STYLE = "pref_time_picker_style";
    public static final String KEY_PREF_SNOOZE_DURATION = "pref_snooze_duration";
    public static final String KEY_PREF_FIRST_DAY_OF_WEEK = "pref_first_day_of_week";
    // Timers preference keys
    public static final String KEY_PREF_TIMER_RINGTONE = "pref_timer_ringtone";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int layoutResId() {
        return R.layout.activity_settings;
    }

    @Override
    protected int menuResId() {
        return 0;
    }

    @Override
    protected boolean isDisplayShowTitleEnabled() {
        return true;
    }
}