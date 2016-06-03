package com.philliphsu.clock2.editalarm;

import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
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
import butterknife.OnTouch;

import static android.text.format.DateFormat.getTimeFormat;
import static com.philliphsu.clock2.DaysOfWeek.NUM_DAYS;
import static com.philliphsu.clock2.DaysOfWeek.SATURDAY;
import static com.philliphsu.clock2.DaysOfWeek.SUNDAY;

public class EditAlarmActivity extends BaseActivity implements AlarmNumpad.KeyListener {
    private static final String TAG = "EditAlarmActivity";
    public static final String EXTRA_ALARM_ID = "com.philliphsu.clock2.editalarm.extra.ALARM_ID";

    private static final int REQUEST_PICK_RINGTONE = 0;
    private static final int ID_MENU_ITEM = 0;

    @Nullable private Alarm mAlarm;
    private Uri mSelectedRingtoneUri;

    @Bind(R.id.save) Button mSave;
    @Bind(R.id.delete) Button mDelete;
    @Bind(R.id.on_off) SwitchCompat mSwitch;
    @Bind(R.id.input_time) EditText mTimeText;
    @Bind({R.id.day0, R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6})
    ToggleButton[] mDays;
    @Bind(R.id.label) EditText mLabel;
    @Bind(R.id.ringtone) Button mRingtone;
    @Bind(R.id.vibrate) CheckBox mVibrate;
    @Bind(R.id.numpad) AlarmNumpad mNumpad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWeekDaysText();
        mNumpad.setKeyListener(this);
        long alarmId = getIntent().getLongExtra(EXTRA_ALARM_ID, -1);
        if (alarmId > -1) {
            mAlarm = AlarmsRepository.getInstance(this).getItem(alarmId);
            if (mAlarm != null) {
                mNumpad.setTime(mAlarm.hour(), mAlarm.minutes());
                mSwitch.setChecked(mAlarm.isEnabled());
                //mTimeText.setText(getTimeFormat(this).format(new Date(mAlarm.ringsAt())));
                for (int i = SUNDAY; i <= SATURDAY; i++) {
                    // What position in the week is this day located at?
                    int at = DaysOfWeek.getInstance(this).positionOf(i);
                    // Toggle the button that corresponds to this day
                    mDays[at].setChecked(mAlarm.isRecurring(i));
                }
                mLabel.setText(mAlarm.label());
                mSelectedRingtoneUri = Uri.parse(mAlarm.ringtone());
                mVibrate.setChecked(mAlarm.vibrates());
                if (mAlarm.isSnoozed()) {
                    String title = getString(R.string.title_snoozing_until,
                            getTimeFormat(this).format(new Date(mAlarm.snoozingUntil()))
                    );
                    ActionBar ab = getSupportActionBar();
                    ab.setDisplayShowTitleEnabled(true);
                    ab.setTitle(title);
                }
                // Editing alarm so don't show
                mNumpad.setVisibility(View.GONE);
            } else {
                // TODO default values
                // No alarm retrieved with this id
                mNumpad.setVisibility(View.VISIBLE);
            }
        } else {
            // Initializing to Settings.System.DEFAULT_ALARM_ALERT_URI will show
            // "Default ringtone (Name)" on the button text, and won't show the
            // selection on the dialog when first opened. (unless you choose to show
            // the default item in the intent extra?)
            // Compare with getDefaultUri(int), which returns the symbolic URI instead of the
            // actual sound URI. For TYPE_ALARM, this actually returns the same constant.
            mSelectedRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
            mNumpad.setVisibility(View.VISIBLE);
        }
        updateRingtoneButtonText();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Since this Activity doesn't host fragments, not necessary?
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_RINGTONE && resultCode == RESULT_OK) {
            mSelectedRingtoneUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            updateRingtoneButtonText();
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
    public void onBackPressed() {
        if (mNumpad.getVisibility() == View.VISIBLE) {
            mNumpad.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onAcceptChanges() {
        mNumpad.setVisibility(View.GONE);
        mSwitch.setChecked(true);
        mSwitch.requestFocus();
    }

    @Override
    public void onNumberInput(String formattedInput) {
        mTimeText.setText(formattedInput);
        mTimeText.setSelection(mTimeText.length());
    }

    @Override
    public void onCollapse() {
        mNumpad.setVisibility(View.GONE);
        mSwitch.requestFocus();
    }

    @Override
    public void onBackspace(String newStr) {
        mTimeText.setText(newStr);
        mTimeText.setSelection(mTimeText.length());
        if (!mNumpad.checkTimeValid() && mSwitch.isChecked()) {
            mSwitch.setChecked(false);
        }
    }

    @Override
    public void onLongBackspace() {
        mTimeText.setText("");
        mSwitch.setChecked(false);
        mTimeText.setSelection(0);
    }

    @OnTouch(R.id.input_time)
    boolean touch(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP && mNumpad.getVisibility() != View.VISIBLE) {
            mNumpad.setVisibility(View.VISIBLE);
        }
        return true;
    }

    @OnClick(R.id.ringtone)
    void ringtone() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                // The ringtone to show as selected when the dialog is opened
                .putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, mSelectedRingtoneUri)
                // Whether to show "Default" item in the list
                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false); // TODO: false?
                // The ringtone that plays when default option is selected
                //.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, DEFAULT_TONE);
        startActivityForResult(intent, REQUEST_PICK_RINGTONE);
    }

    @OnClick(R.id.save)
    void save() {
        boolean[] days = new boolean[NUM_DAYS];
        for (int i = SUNDAY; i <= SATURDAY; i++) {
            // What position in the week is this day located at?
            int pos = DaysOfWeek.getInstance(this).positionOf(i);
            // Set the state of this day according to its corresponding button
            days[i] = mDays[pos].isChecked();
        }
        Alarm a = Alarm.builder()
                // TODO: set hour and minute
                .ringtone(mSelectedRingtoneUri.toString())
                .recurringDays(days) // TODO: See https://github.com/google/auto/blob/master/value/userguide/howto.md#mutable_property
                .label(mLabel.getText().toString())
                .vibrates(mVibrate.isChecked())
                .build();
        a.setEnabled(mSwitch.isChecked());
        if (mAlarm != null) {
            // TODO: Cancel any alarm scheduled with the old alarm's ID
            // TODO: Schedule the new alarm
            AlarmsRepository.getInstance(this).updateItem(mAlarm, a);
        } else {
            // TODO: Schedule the new alarm
            AlarmsRepository.getInstance(this).addItem(a);
        }
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

    private void updateRingtoneButtonText() {
        mRingtone.setText(RingtoneManager.getRingtone(this, mSelectedRingtoneUri).getTitle(this));
    }
}
