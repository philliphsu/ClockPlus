package com.philliphsu.clock2.editalarm;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.BaseActivity;
import com.philliphsu.clock2.DaysOfWeek;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.model.AlarmsRepository;

import butterknife.Bind;
import butterknife.OnClick;

public class EditAlarmActivity extends BaseActivity {

    @Bind(R.id.save) Button mSave;
    @Bind(R.id.delete) Button mDelete;
    @Bind(R.id.on_off) SwitchCompat mSwitch;
    @Bind(R.id.input_time) EditText mTimeText;
    @Bind({R.id.day0, R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6})
    ToggleButton[] mDays;
    @Bind(R.id.label) EditText mLabel;
    @Bind(R.id.ringtone) Button mRingtone;
    @Bind(R.id.vibrate) CheckBox mVibrate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWeekDaysText();
        getSupportActionBar().setTitle("Snoozing until 12:40 PM");
    }

    @Override
    protected int layoutResId() {
        return R.layout.activity_edit_alarm;
    }

    @Override
    protected int menuResId() {
        return 0;
    }

    @Override
    protected boolean isDisplayShowTitleEnabled() {
        return true;
    }

    @OnClick(R.id.save)
    void save() {
        AlarmsRepository.getInstance(this).addItem(Alarm.builder().build());
    }

    private void setWeekDaysText() {
        for (int i = 0; i < mDays.length; i++) {
            int weekDay = DaysOfWeek.getInstance(this).weekDay(i);
            String label = DaysOfWeek.getLabel(weekDay);
            mDays[i].setTextOn(label);
            mDays[i].setTextOff(label);
            mDays[i].setChecked(mDays[i].isChecked()); // force update the text, otherwise it won't be shown
        }
    }
}
