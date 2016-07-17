package com.philliphsu.clock2.editalarm;

import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SwitchCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.BaseActivity;
import com.philliphsu.clock2.DaysOfWeek;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.SharedPreferencesHelper;
import com.philliphsu.clock2.model.AlarmLoader;
import com.philliphsu.clock2.ringtone.RingtoneActivity;
import com.philliphsu.clock2.util.AlarmController;
import com.philliphsu.clock2.util.AlarmUtils;
import com.philliphsu.clock2.util.DateFormatUtils;
import com.philliphsu.clock2.util.LocalBroadcastHelper;

import java.util.Date;

import butterknife.Bind;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTouch;

import static android.text.format.DateFormat.getTimeFormat;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.philliphsu.clock2.DaysOfWeek.SATURDAY;
import static com.philliphsu.clock2.DaysOfWeek.SUNDAY;
import static com.philliphsu.clock2.util.Preconditions.checkNotNull;

/**
 * TODO: Consider writing an EditAlarmController that would have an
 * AlarmController member variable to manage the states of the alarm.
 * The class would have the API for editing the alarm, so move all
 * the relevant helper methods from here to there.
 */
public class EditAlarmActivity extends BaseActivity implements
        AlarmNumpad.KeyListener, // TODO: Deprecated, remove
        EditAlarmContract.View, // TODO: Remove @Override from the methods
        AlarmUtilsHelper,
        SharedPreferencesHelper,
        LoaderManager.LoaderCallbacks<Alarm>,
        NumpadTimePicker.OnTimeSetListener {
    private static final String TAG = "EditAlarmActivity";
    private static final String TAG_TIME_PICKER = "time_picker";

    public static final String EXTRA_ALARM_ID = "com.philliphsu.clock2.editalarm.extra.ALARM_ID";
    public static final String EXTRA_MODIFIED_ALARM = "com.philliphsu.clock2.editalarm.extra.MODIFIED_ALARM";
    public static final String EXTRA_IS_DELETING = "com.philliphsu.clock2.editalarm.extra.IS_DELETING";

    private static final String KEY_INPUT_TIME = "input_time";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_CHECKED_DAYS = "checked_days";
    private static final String KEY_LABEL = "label";
    private static final String KEY_RINGTONE_URI = "ringtone";
    private static final String KEY_VIBRATE = "vibrate";

    private static final int REQUEST_PICK_RINGTONE = 0;
    private static final int ID_MENU_ITEM = 0;

    private long mOldAlarmId;
    private Uri mSelectedRingtoneUri;
    private Alarm mOldAlarm;
    private int mSelectedHourOfDay;
    private int mSelectedMinute;

    // If we keep a reference to the dialog, we keep its previous state as well.
    // So the next time we call show() on this, the input field will show the
    // last inputted time. The easiest workaround is to always create a new
    // instance each time we want to show the dialog.
//    private NumpadTimePickerDialog mPicker;

    @Bind(R.id.main_content) CoordinatorLayout mMainContent;
    @Bind(R.id.save) Button mSave;
    @Bind(R.id.delete) Button mDelete;
    @Bind(R.id.on_off) SwitchCompat mSwitch;
    @Bind(R.id.input_time) TextView mTimeText;
    @Bind({R.id.day0, R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6})
    ToggleButton[] mDays;
    @Bind(R.id.label) EditText mLabel;
    @Bind(R.id.ringtone) Button mRingtone;
    @Bind(R.id.vibrate) CheckBox mVibrate;
    @Deprecated @Bind(R.id.numpad) AlarmNumpad mNumpad;

    @Override
    public void onTimeSet(ViewGroup viewGroup, int hourOfDay, int minute) {
        mSelectedHourOfDay = hourOfDay;
        mSelectedMinute = minute;
        showTimeText(DateFormatUtils.formatTime(this, hourOfDay, minute));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWeekDaysText();

        // Are we recreating this Activity because of a rotation?
        // If so, try finding the time picker in our backstack.
        NumpadTimePickerDialog picker = (NumpadTimePickerDialog)
                getSupportFragmentManager().findFragmentByTag(TAG_TIME_PICKER);
        if (picker != null) {
            // Restore the callback
            picker.setOnTimeSetListener(this);
//            mPicker = picker;
        }

        // TODO: Delete this
        mNumpad.setKeyListener(this);

        mOldAlarmId = getIntent().getLongExtra(EXTRA_ALARM_ID, -1);
        if (mOldAlarmId != -1) {
            // getLoaderManager() for support fragments by default returns the
            // support version of LoaderManager. However, since this is an Activity,
            // we have both the native getLoaderManager() and getSupportLoaderManager().
            // Use the latter to remain consistent with the rest of our current code base.
            getSupportLoaderManager().initLoader(0, null, this);
        } else {
            // Nothing to load, so show default values
            showDetails();
            // Show the time picker dialog, if it is not already showing
            // AND this is the very first time the activity is being created
            if (picker == null && savedInstanceState == null) {
                // Wait a bit so the activity and dialog don't show at the same time
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        openTimePicker();
                    }
                }, 300);
            }
        }
        setTimeTextHint(); // TODO: private access
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Write out any state we wish to save for re-initialization.
        // You can either restore this state in onCreate() or by
        // overriding onRestoreInstanceState() and restoring it there.
        super.onSaveInstanceState(outState);
        outState.putString(KEY_INPUT_TIME, mTimeText.getText().toString());
        // This is restored automatically post-rotation
        outState.putBoolean(KEY_ENABLED, mSwitch.isChecked());
        // These are restored automatically post-rotation
        outState.putBooleanArray(KEY_CHECKED_DAYS, new boolean[] {
                mDays[0].isChecked(),
                mDays[1].isChecked(),
                mDays[2].isChecked(),
                mDays[3].isChecked(),
                mDays[4].isChecked(),
                mDays[5].isChecked(),
                mDays[6].isChecked()
        });
        // This is restored automatically post-rotation
        outState.putString(KEY_LABEL, mLabel.getText().toString());
        outState.putParcelable(KEY_RINGTONE_URI, mSelectedRingtoneUri);
        // This is restored automatically post-rotation
        outState.putBoolean(KEY_VIBRATE, mVibrate.isChecked());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // For now, restore the previous state here instead of in onCreate()
        // because it's easier than refactoring the code over there.
        super.onRestoreInstanceState(savedInstanceState);
        // No null-check on the given Bundle is necessary,
        // because onSaveInstanceState() works with a non-null
        // Bundle, even in the default implementation.
        if (savedInstanceState.containsKey(KEY_INPUT_TIME)
                //&& savedInstanceState.containsKey(KEY_LABEL)
                && savedInstanceState.containsKey(KEY_RINGTONE_URI)) {
            // Make sure we actually saved something, or else we'd get
            // a null and we'll end up clearing the hints...
            mTimeText.setText(savedInstanceState.getString(KEY_INPUT_TIME));
//            mLabel.setText(savedInstanceState.getString(KEY_LABEL));
            mSelectedRingtoneUri = savedInstanceState.getParcelable(KEY_RINGTONE_URI);
            // ...this, however, would throw an NPE because
            // we'd be accessing a null Ringtone.
            updateRingtoneButtonText();
        }
        // TODO: Manually restore the states of the "auto-restoring" widgets.
        // In onCreate(), we will call showDetails().
        // You only witnessed the auto-restoring for a blank Alarm, where
        // the impl of showDetails() is pretty bare. If we have an actual
        // Alarm, showDetails() could very well change the values displayed
        // by those widgets based on that Alarm's values, but not based on
        // any unsaved changes that may have occurred to the widgets previously.
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // TODO: Snooze menu item when alarm is ringing.
        if (mOldAlarm != null && mOldAlarm.isEnabled()) {
            int hoursBeforeUpcoming = getInt(R.string.key_notify_me_of_upcoming_alarms, 2);
            // TODO: Schedule task with handler to show the menu item when it is time.
            // Handler is fine because the task only needs to be done if the activity
            // is being viewed. (I think) if the process of this
            // app is killed, then the handler is also killed.
            if ((mOldAlarm.ringsWithinHours(hoursBeforeUpcoming))) {
                showCanDismissNow();
            } else if (mOldAlarm.isSnoozed()) {
                showSnoozed(new Date(mOldAlarm.snoozingUntil()));
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_dismiss_now:
            case R.id.action_done_snoozing:
                cancelAlarm(checkNotNull(mOldAlarm), true);
                // cancelAlarm() should have turned off this alarm if appropriate
                showEnabled(mOldAlarm.isEnabled());
                item.setVisible(false);
                // This only matters for case R.id.action_done_snoozing.
                // It won't hurt to call this for case R.id.action_dismiss_now.
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        return R.layout.activity_edit_alarm_v2;
    }

    @Override
    protected int menuResId() {
        return R.menu.menu_edit_alarm;
    }

    @Override
    public void onBackPressed() {
        // This if check must be here unless you want to write a presenter
        // method called isNumpadOpen()...
        if (mNumpad.getVisibility() == View.VISIBLE) {
            showNumpad(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onAcceptChanges() {
        showNumpad(false);
        showEnabled(true);
    }

    @Override
    public void onNumberInput(String formattedInput) {
        showTimeText(formattedInput);
    }

    @Override
    public void onCollapse() {
        showNumpad(false);
    }

    @Override
    public void onBackspace(String newStr) {
        showTimeTextPostBackspace(newStr);
    }

    @Override
    public void onLongBackspace() {
        showTimeTextPostBackspace("");
    }

    /* // TODO: remove
    @OnTouch(R.id.input_time)
    boolean touch(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            hideKeyboard(this); // If not open, does nothing.
            showTimeTextFocused(true);
            if (mNumpad.getVisibility() != View.VISIBLE) {
                // TODO: If keyboard was open, consider adding delay to opening the numpad.
                // Otherwise, it opens immediately behind the keyboard as it is still animating
                // out of the window.
                showNumpad(true);
            }
        }
        return true;
    }
    */

    @OnClick(R.id.ringtone)
    void ringtone() {
        showRingtonePickerDialog();
    }

    @OnClick(R.id.save)
    void save() {
        Alarm alarm = Alarm.builder()
                .hour(mSelectedHourOfDay)
                .minutes(mSelectedMinute)
                .ringtone(getRingtone())
                .label(getLabel())
                .vibrates(vibrates())
                .build();
        alarm.setEnabled(isEnabled());
        for (int i = SUNDAY; i <= SATURDAY; i++) {
            alarm.setRecurring(i, isRecurringDay(i));
        }

        Intent intent = new Intent();
        if (mOldAlarm != null) {
            if (mOldAlarm.isEnabled()) {
                Log.d(TAG, "Cancelling old alarm first");
                cancelAlarm(mOldAlarm, false);
            }
            alarm.setId(mOldAlarm.id());
            intent.putExtra(EXTRA_IS_DELETING, false);
        }
        intent.putExtra(EXTRA_MODIFIED_ALARM, alarm);

        // The reason we don't schedule the alarm here is AlarmUtils
        // will attempt to retrieve the specified alarm
        // from the database; however, the alarm hasn't yet
        // been added to the database at this point.

        setResult(RESULT_OK, intent);
        showEditorClosed();
    }

    // TODO: Private accessor
    @OnClick(R.id.delete)
    void delete() {
        if (mOldAlarm != null) {
            if (mOldAlarm.isEnabled()) {
                cancelAlarm(mOldAlarm, false);
                // Re-enable in case this is restored so
                // the alarm can be scheduled again. This
                // change is saved to the db if the alarm
                // is restored (re-inserting into to the db).
                mOldAlarm.setEnabled(true);
            }
            Intent intent = new Intent();
            intent.putExtra(EXTRA_IS_DELETING, true);
            intent.putExtra(EXTRA_MODIFIED_ALARM, mOldAlarm);
            setResult(RESULT_OK, intent);
        }
        showEditorClosed();
    }

    // This isn't actually concerned with setting the alarm on/off.
    // It only checks if the touch event is valid to be processed.
    // The actual toggling of on/off is handled when the OnCheckedChange
    // event is fired. See #onChecked(boolean) below.
    @OnTouch(R.id.on_off)
    boolean toggleSwitch(MotionEvent event) {
        // Event captured on start of pressed gesture
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (mTimeText.length() == 0 || mNumpad.checkTimeValid()) {
                return false; // proceed to call through
            } else {
                Toast.makeText(this, "Enter a valid time first.", Toast.LENGTH_SHORT).show();
                return true; // capture and end the touch event here
            }
        }
        return false;
    }

    @OnTouch(R.id.label)
    boolean touchLabel(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP && mNumpad.getVisibility() == VISIBLE) {
            showNumpad(false);
        }
        return false; // don't capture
    }

    @OnCheckedChanged(R.id.on_off)
    void onChecked(boolean checked) {
        if (checked && mTimeText.length() == 0) {
            mNumpad.setTime(0, 0);
        }
    }

    @OnClick(R.id.numpad)
    void captureClickEvent() {
        /*
         * ====================== DO NOT IMPLEMENT =====================================
         * A stray click in the vicinity of the persistent footer buttons, even while
         * they are covered by the numpad, will still have the click event call through
         * to those buttons. This captures the buttons' click events as long as the numpad
         * is in view.
         * =============================================================================
         */
    }

    @OnClick(R.id.input_time)
    void openTimePicker() {
        NumpadTimePickerDialog.newInstance(EditAlarmActivity.this)
                .show(getSupportFragmentManager(), TAG_TIME_PICKER);
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

    @Override
    public void showRecurringDays(int weekDay, boolean recurs) {
        // What position in the week is this day located at?
        int at = DaysOfWeek.getInstance(this).positionOf(weekDay);
        // Toggle the button that corresponds to this day
        mDays[at].setChecked(recurs);
    }

    @Override
    public void showRingtone(String ringtone) {
        // Initializing to Settings.System.DEFAULT_ALARM_ALERT_URI will show
        // "Default ringtone (Name)" on the button text, and won't show the
        // selection on the dialog when first opened. (unless you choose to show
        // the default item in the intent extra?)
        // Compare with getDefaultUri(int), which returns the symbolic URI instead of the
        // actual sound URI. For TYPE_ALARM, this actually returns the same constant.
        if (null == ringtone || ringtone.isEmpty()) {
            mSelectedRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
        } else {
            mSelectedRingtoneUri = Uri.parse(ringtone);
        }
        updateRingtoneButtonText();
    }

    @Override
    public void showVibrates(boolean vibrates) {
        mVibrate.setChecked(vibrates);
    }

    @Override
    public void showEditorClosed() {
        finish();
    }

    @Override
    public int getHour() {
        return mNumpad.getHours();
    }

    @Override
    public int getMinutes() {
        return mNumpad.getMinutes();
    }

    @Override
    public boolean isEnabled() {
        return mSwitch.isChecked();
    }

    @Override
    public boolean isRecurringDay(int weekDay) {
        // What position in the week is this day located at?
        int pos = DaysOfWeek.getInstance(this).positionOf(weekDay);
        // Return the state of this day according to its button
        return mDays[pos].isChecked();
    }

    @Override
    public String getLabel() {
        return mLabel.getText().toString();
    }

    @Override
    public String getRingtone() {
        return mSelectedRingtoneUri.toString();
    }

    @Override
    public boolean vibrates() {
        return mVibrate.isChecked();
    }

    @Override
    public void showTime(int hour, int minutes) {
        mNumpad.setTime(hour, minutes);
    }

    @Override
    public void showLabel(String label) {
        mLabel.setText(label);
    }

    @Override
    public void showEnabled(boolean enabled) {
        mSwitch.setChecked(enabled);
    }

    @Override
    public void showNumpad(boolean show) {
        mNumpad.setVisibility(show ? VISIBLE : GONE);
    }

    @Override
    public void showCanDismissNow() {
        Menu menu = checkNotNull(getMenu());
        MenuItem item = menu.findItem(R.id.action_dismiss_now);
        if (!item.isVisible()) {
            item.setVisible(true);
            menu.findItem(R.id.action_done_snoozing).setVisible(false);
        }
    }

    @Override
    public void showSnoozed(Date snoozingUntilMillis) {
        Menu menu = checkNotNull(getMenu());
        MenuItem item = menu.findItem(R.id.action_done_snoozing);
        if (!item.isVisible()) {
            item.setVisible(true);
            menu.findItem(R.id.action_dismiss_now).setVisible(false);
        }
        String title = getString(R.string.title_snoozing_until,
                getTimeFormat(this).format(snoozingUntilMillis));
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowTitleEnabled(true);
        ab.setTitle(title);
    }

    @Override
    public void setTimeTextHint() {
        if (DateFormat.is24HourFormat(this)) {
            mTimeText.setText(R.string.default_alarm_time_24h);
        } else {
            showTimeText(getString(R.string.default_alarm_time_12h));
        }
    }

    @Override
    public void showTimeText(String formattedInput) {
        TimeTextUtils.setText(formattedInput, mTimeText);
    }

    @Deprecated // TODO: Remove
    @Override
    public void showTimeTextPostBackspace(String newStr) {
        mTimeText.setText(newStr);
        if (!mNumpad.checkTimeValid() && mSwitch.isChecked()) {
            mSwitch.setChecked(false);
        }
    }

    @Override
    public void showRingtonePickerDialog() {
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

    @Override
    public void showTimeTextFocused(boolean focused) {
        if (focused) {
            mTimeText.requestFocus();
            // Move cursor to end
//TODO:delete            mTimeText.setSelection(mTimeText.length());
        } else {
            mTimeText.clearFocus(); // TODO: not cleared! focus needs to go to a neighboring view.
        }
    }

    // TODO: Delete this
    @Deprecated
    @Override
    public void scheduleAlarm(Alarm alarm) {
        //AlarmUtils.scheduleAlarm(this, alarm, true);
    }

    @Override
    public void cancelAlarm(Alarm alarm, boolean showToast) {
        // TODO: Rewrite XML layout to use CoordinatorLayout and
        // pass in the snackbar anchor.
        new AlarmController(this, mMainContent).cancelAlarm(alarm, true);
        if (RingtoneActivity.isAlive()) {
            LocalBroadcastHelper.sendBroadcast(this, RingtoneActivity.ACTION_FINISH);
        }
    }

    @Override
    public int getInt(@StringRes int key, int defaultValue) {
        return AlarmUtils.readPreference(this, key, defaultValue);
    }

    @Override
    public Loader<Alarm> onCreateLoader(int id, Bundle args) {
        return new AlarmLoader(this, mOldAlarmId);
    }

    @Override
    public void onLoadFinished(Loader<Alarm> loader, Alarm data) {
        mOldAlarm = data;
        showDetails();
    }

    @Override
    public void onLoaderReset(Loader<Alarm> loader) {
        // nothing to reset
    }

    // TODO: Privatize access of each method called here.
    private void showDetails() {
        if (mOldAlarm != null) {
            showTime(mOldAlarm.hour(), mOldAlarm.minutes());
            showEnabled(mOldAlarm.isEnabled());
            for (int i = SUNDAY; i <= SATURDAY; i++) {
                showRecurringDays(i, mOldAlarm.isRecurring(i));
            }
            showLabel(mOldAlarm.label());
            showRingtone(mOldAlarm.ringtone());
            showVibrates(mOldAlarm.vibrates());
            // Editing so don't show
            showNumpad(false);
            showTimeTextFocused(false);
        } else {
            // TODO default values
            showTimeTextFocused(true);
            showRingtone(""); // gets default ringtone
            showEnabled(true);
            //showNumpad(true);
        }
    }
}
