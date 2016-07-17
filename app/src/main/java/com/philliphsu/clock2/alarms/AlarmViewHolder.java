package com.philliphsu.clock2.alarms;

import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.BaseViewHolder;
import com.philliphsu.clock2.DaysOfWeek;
import com.philliphsu.clock2.OnListItemInteractionListener;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.editalarm.TimeTextUtils;
import com.philliphsu.clock2.model.AlarmsRepository;
import com.philliphsu.clock2.util.AlarmController;
import com.philliphsu.clock2.util.AlarmUtils;

import java.util.Date;

import butterknife.Bind;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTouch;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.philliphsu.clock2.DaysOfWeek.NUM_DAYS;
import static com.philliphsu.clock2.util.DateFormatUtils.formatTime;

/**
 * Created by Phillip Hsu on 5/31/2016.
 */
public class AlarmViewHolder extends BaseViewHolder<Alarm> implements AlarmCountdown.OnTickListener {
    private final AlarmController mAlarmController;

    @Bind(R.id.time) TextView mTime;
    @Bind(R.id.on_off_switch) SwitchCompat mSwitch;
    @Bind(R.id.label) TextView mLabel;
    @Bind(R.id.countdown) AlarmCountdown mCountdown;
    @Bind(R.id.recurring_days) TextView mDays;
    @Bind(R.id.dismiss) Button mDismissButton;

    @Deprecated // TODO: Delete this, the only usage is from AlarmsAdapter (SortedList), which is not used anymore.
    public AlarmViewHolder(ViewGroup parent, OnListItemInteractionListener<Alarm> listener) {
        super(parent, R.layout.item_alarm, listener);
        mAlarmController = null;
        mCountdown.setOnTickListener(this);
    }

    public AlarmViewHolder(ViewGroup parent, OnListItemInteractionListener<Alarm> listener,
                           AlarmController alarmController) {
        super(parent, R.layout.item_alarm, listener);
        mAlarmController = alarmController;
        mCountdown.setOnTickListener(this);
    }

    @Override
    public void onBind(Alarm alarm) {
        super.onBind(alarm);
        bindTime(new Date(alarm.ringsAt()));
        bindSwitch(alarm.isEnabled());
        bindCountdown(alarm.isEnabled(), alarm.ringsIn());
        bindDismissButton(alarm);
        bindLabel(alarm);
        bindDays(alarm);
    }

    @Override
    public void onTick() {
        mCountdown.showAsText(getAlarm().ringsIn());
    }

    @OnClick(R.id.dismiss)
    void dismiss() {
        Alarm alarm = getAlarm();
        if (!alarm.hasRecurrence()) {
            // This is a single-use alarm, so turn it off completely.
            mSwitch.setPressed(true); // needed so the OnCheckedChange event calls through
            bindSwitch(false); // fires OnCheckedChange to turn off the alarm for us
        } else {
            // Dismisses the current upcoming alarm and handles scheduling the next alarm for us.
            // Since changes are saved to the database, this prompts a UI refresh.
            mAlarmController.cancelAlarm(alarm, true);
        }
        // TOneverDO: AlarmUtils.cancelAlarm() otherwise it will be called twice
        /*
        AlarmUtils.cancelAlarm(getContext(), getAlarm());
        if (!getAlarm().isEnabled()) {
            // TOneverDO: mSwitch.setPressed(true);
            bindSwitch(false); // will fire OnCheckedChange, but switch isn't set as pressed so nothing happens.
            bindCountdown(false, -1);
        }
        bindDismissButton(false, ""); // Will be set to correct text the next time we bind.
        // If cancelAlarm() modified the alarm's fields, then it will save changes for you.
        */
    }

    // Changed in favor or OnCheckedChange
    /*
    @Deprecated
    @OnClick(R.id.on_off_switch)
    void toggle() {
        Alarm alarm = getAlarm();
        alarm.setEnabled(mSwitch.isChecked());
        if (alarm.isEnabled()) {
            AlarmUtils.scheduleAlarm(getContext(), alarm);
            bindCountdown(true, alarm.ringsIn());
            bindDismissButton(alarm);
        } else {
            AlarmUtils.cancelAlarm(getContext(), alarm); // might save repo
            bindCountdown(false, -1);
            bindDismissButton(false, "");
        }
        save();
    }
    */

    @OnTouch(R.id.on_off_switch)
    boolean slide(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            mSwitch.setPressed(true); // needed so the OnCheckedChange event calls through
        }
        return false; // proceed as usual
    }

    @OnCheckedChanged(R.id.on_off_switch)
    void toggle(boolean checked) {
        if (mSwitch.isPressed()) { // needed to distinguish automatic calls when VH binds from actual presses
            // don't need to toggle the switch state
            Alarm alarm = getAlarm();
            alarm.setEnabled(checked);
            if (alarm.isEnabled()) {
                // TODO: On Moto X, upcoming notification doesn't post immediately
                mAlarmController.scheduleAlarm(alarm, true);
                mAlarmController.save(alarm);
            } else {
                mAlarmController.cancelAlarm(alarm, true);
                // cancelAlarm() already calls save() for you.
            }
            mSwitch.setPressed(false); // clear the pressed focus, esp. if setPressed(true) was called manually
        }
    }

    private void bindTime(Date date) {
        String time = DateFormat.getTimeFormat(getContext()).format(date);
        if (DateFormat.is24HourFormat(getContext())) {
            mTime.setText(time);
        } else {
            TimeTextUtils.setText(time, mTime);
        }
    }

    private void bindSwitch(boolean enabled) {
        mSwitch.setChecked(enabled);
    }

    private void bindCountdown(boolean enabled, long remainingTime) {
        if (enabled) {
            mCountdown.showAsText(remainingTime);
            mCountdown.startTicking(true);
            mCountdown.setVisibility(VISIBLE);
        } else {
            mCountdown.stopTicking();
            mCountdown.setVisibility(GONE);
        }
    }

    private void bindDismissButton(Alarm alarm) {
        int hoursBeforeUpcoming = AlarmUtils.hoursBeforeUpcoming(getContext());
        boolean visible = alarm.isEnabled() && (alarm.ringsWithinHours(hoursBeforeUpcoming) || alarm.isSnoozed());
        String buttonText = alarm.isSnoozed()
                ? getContext().getString(R.string.title_snoozing_until, formatTime(getContext(), alarm.snoozingUntil()))
                : getContext().getString(R.string.dismiss_now);
        bindDismissButton(visible, buttonText);
    }

    private void bindDismissButton(boolean visible, String buttonText) {
        setVisibility(mDismissButton, visible);
        mDismissButton.setText(buttonText);
    }

    private void bindLabel(Alarm alarm) {
        // Should also be visible even if alarm has no label so mCountdown is properly positioned next
        // to mLabel. That is, mCountdown's layout position is dependent on mLabel being present.
        boolean labelVisible = alarm.label().length() > 0 || mCountdown.getVisibility() == VISIBLE;
        bindLabel(labelVisible, alarm.label());
    }

    private void bindLabel(boolean visible, String label) {
        setVisibility(mLabel, visible);
        mLabel.setText(label);
    }

    private void bindDays(Alarm alarm) {
        int num = alarm.numRecurringDays();
        String text;
        if (num == NUM_DAYS) {
            text = getContext().getString(R.string.every_day);
        } else if (num == 0) {
            text = "";
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0 /* Ordinal days*/; i < NUM_DAYS; i++) {
                // What day is at this position in the week?
                int weekDay = DaysOfWeek.getInstance(getContext()).weekDayAt(i);
                if (alarm.isRecurring(weekDay)) {
                    sb.append(DaysOfWeek.getLabel(weekDay)).append(", ");
                }
            }
            // Cut off the last comma and space
            sb.delete(sb.length() - 2, sb.length());
            text = sb.toString();
        }
        bindDays(num > 0, text);
    }

    private void bindDays(boolean visible, String text) {
        setVisibility(mDays, visible);
        mDays.setText(text);
    }

    private void setVisibility(@NonNull View view, boolean visible) {
        view.setVisibility(visible ? VISIBLE : GONE);
    }

    private Alarm getAlarm() {
        return getItem();
    }

    private void save() {
        AlarmsRepository.getInstance(getContext()).saveItems();
    }
}
