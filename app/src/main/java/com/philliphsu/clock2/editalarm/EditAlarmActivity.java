package com.philliphsu.clock2.editalarm;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
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

import static android.text.format.DateFormat.getTimeFormat;
import static com.philliphsu.clock2.DaysOfWeek.SATURDAY;
import static com.philliphsu.clock2.DaysOfWeek.SUNDAY;

public class EditAlarmActivity extends BaseActivity {
    public static final String EXTRA_ALARM_ID = "com.philliphsu.clock2.editalarm.extra.ALARM_ID";

    private static final int ID_MENU_ITEM = 0;
    @Nullable private Alarm mAlarm;

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
        long alarmId = getIntent().getLongExtra(EXTRA_ALARM_ID, -1);
        if (alarmId > -1) {
            mAlarm = AlarmsRepository.getInstance(this).getItem(alarmId);
            if (mAlarm != null) {
                mSwitch.setChecked(mAlarm.isEnabled());
                mTimeText.setText(getTimeFormat(this).format(new Date(mAlarm.ringsAt())));
                for (int i = SUNDAY; i <= SATURDAY; i++) {
                    // What position in the week is this day located at?
                    int at = DaysOfWeek.getInstance(this).positionOf(i);
                    // Toggle the button that corresponds to this day
                    mDays[at].setChecked(mAlarm.isRecurring(i));
                }
                mLabel.setText(mAlarm.label());
                Ringtone r = RingtoneManager.getRingtone(this, Uri.parse(mAlarm.ringtone()));
                mRingtone.setText(r.getTitle(this));
                mVibrate.setChecked(mAlarm.vibrates());
                if (mAlarm.isSnoozed()) {
                    String title = getString(R.string.title_snoozing_until,
                            getTimeFormat(this).format(new Date(mAlarm.snoozingUntil()))
                    );
                    ActionBar ab = getSupportActionBar();
                    ab.setDisplayShowTitleEnabled(true);
                    ab.setTitle(title);
                }
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // TODO: Read upcoming threshold preference
        if (mAlarm != null && (mAlarm.ringsWithinHours(2) || mAlarm.isSnoozed())) {
            if (menu.findItem(ID_MENU_ITEM) == null) {
                // Create dynamically because there is almost nothing we can statically define
                // in a layout resource.
                menu.add(0 /*group*/, ID_MENU_ITEM, 0 /*order*/,
                        mAlarm.isSnoozed() ? R.string.done_snoozing : R.string.dismiss_now)
                        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                        .setIcon(android.R.drawable.ic_delete);
                // TODO: Show correct icon based on which is happening
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected int layoutResId() {
        return R.layout.activity_edit_alarm;
    }

    @Override
    protected int menuResId() {
        return 0;
    }

    @OnClick(R.id.save)
    void save() {
        boolean[] days = new boolean[7];
        days[0] = true;
        days[1] = true;
        days[6] = true;
        Alarm a = Alarm.builder()
                .recurringDays(days)
                .build();
        AlarmsRepository.getInstance(this).addItem(a);
        finish();
    }

    @OnClick(R.id.delete)
    void delete() {
        if (mAlarm != null) {
            AlarmsRepository.getInstance(this).deleteItem(mAlarm);
        }
        finish();
    }

    private void setWeekDaysText() {
        for (int i = 0; i < mDays.length; i++) {
            int weekDay = DaysOfWeek.getInstance(this).weekDayAt(i);
            String label = DaysOfWeek.getLabel(weekDay);
            mDays[i].setTextOn(label);
            mDays[i].setTextOff(label);
            mDays[i].setChecked(mDays[i].isChecked()); // force update the text, otherwise it won't be shown
        }
    }
}
