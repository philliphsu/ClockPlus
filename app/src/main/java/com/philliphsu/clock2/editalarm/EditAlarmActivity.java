package com.philliphsu.clock2.editalarm;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.BaseActivity;
import com.philliphsu.clock2.DaysOfWeek;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.model.AlarmsRepository;

import java.util.Date;

import butterknife.Bind;
import butterknife.OnClick;

import static com.philliphsu.clock2.DaysOfWeek.SUNDAY;

public class EditAlarmActivity extends BaseActivity {

    public static final String EXTRA_ALARM_ID = "com.philliphsu.clock2.editalarm.extra.ALARM_ID";

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
        getSupportActionBar().setTitle("Snoozing until 12:40 PM");
        setWeekDaysText();
        long alarmId = getIntent().getLongExtra(EXTRA_ALARM_ID, -1);
        if (alarmId > -1) {
            Alarm alarm = AlarmsRepository.getInstance(this).getItem(alarmId);
            if (alarm != null) {
                mSwitch.setChecked(alarm.isEnabled());
                mTimeText.setText(DateFormat.getTimeFormat(this).format(new Date(alarm.ringsAt())));
                for (int i = 0; i < mDays.length; i++) {
                    // What day is at this position in the week?
                    int weekDay = DaysOfWeek.getInstance(this).weekDay(i);
                    // We toggle the button at this position because it represents that day
                    mDays[i].setChecked(alarm.isRecurring(weekDay));
                }
            }
        }
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
        boolean[] days = new boolean[7];
        days[SUNDAY] = true;
        Alarm a = Alarm.builder()
                .recurringDays(days)
                .build();
        AlarmsRepository.getInstance(this).addItem(a);
        finish();
    }

    @OnClick(R.id.delete)
    void delete() {
        //AlarmsRepository.getInstance(this).deleteItem();
        finish();
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
