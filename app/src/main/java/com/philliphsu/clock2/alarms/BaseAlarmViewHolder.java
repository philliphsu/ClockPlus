package com.philliphsu.clock2.alarms;

import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.BaseViewHolder;
import com.philliphsu.clock2.OnListItemInteractionListener;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.editalarm.TimeTextUtils;
import com.philliphsu.clock2.util.AlarmController;
import com.philliphsu.clock2.util.AlarmUtils;

import java.util.Date;

import butterknife.Bind;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTouch;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.philliphsu.clock2.util.DateFormatUtils.formatTime;

/**
 * Created by Phillip Hsu on 7/31/2016.
 */
public abstract class BaseAlarmViewHolder extends BaseViewHolder<Alarm> {

    private final AlarmController mAlarmController;
    // TODO: Should we use VectorDrawable type?
    private final Drawable mDismissNowDrawable;
    private final Drawable mCancelSnoozeDrawable;

    @Bind(R.id.time) TextView mTime;
    @Bind(R.id.on_off_switch) SwitchCompat mSwitch;
    @Bind(R.id.label) TextView mLabel;
    @Bind(R.id.dismiss) Button mDismissButton;

    public BaseAlarmViewHolder(ViewGroup parent, @LayoutRes int layoutRes,
                               OnListItemInteractionListener<Alarm> listener,
                               AlarmController controller) {
        super(parent, layoutRes, listener);
        mAlarmController = controller;
        // Because of VH binding, setting drawable resources on views would be bad for performance.
        // Instead, we create and cache the Drawables once.
        mDismissNowDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_dismiss_alarm_24dp);
        mCancelSnoozeDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_cancel_snooze);
    }

    @Override
    public void onBind(Alarm alarm) {
        super.onBind(alarm);
        bindTime(new Date(alarm.ringsAt()));
        bindSwitch(alarm.isEnabled());
        bindDismissButton(alarm);
        bindLabel(alarm.label());
    }

    /**
     * Exposed to subclasses if they have different visibility criteria.
     * The default criteria for visibility is if {@code label} has
     * a non-zero length.
     */
    protected void bindLabel(boolean visible, String label) {
        setVisibility(mLabel, visible);
        mLabel.setText(label);
    }

    /**
     * Exposed to subclasses if they have visibility logic for their views.
     */
    protected final void setVisibility(@NonNull View view, boolean visible) {
        view.setVisibility(visible ? VISIBLE : GONE);
    }

    protected final Alarm getAlarm() {
        return getItem();
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

    @OnTouch(R.id.on_off_switch)
    boolean slide(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            mSwitch.setPressed(true); // needed so the OnCheckedChange event calls through
        }
        return false; // proceed as usual
    }

//    // Changed in favor of OnCheckedChanged
//    @Deprecated
//    @OnClick(R.id.on_off_switch)
//    void toggle() {
//        Alarm alarm = getAlarm();
//        alarm.setEnabled(mSwitch.isChecked());
//        if (alarm.isEnabled()) {
//            AlarmUtils.scheduleAlarm(getContext(), alarm);
//            bindCountdown(true, alarm.ringsIn());
//            bindDismissButton(alarm);
//        } else {
//            AlarmUtils.cancelAlarm(getContext(), alarm); // might save repo
//            bindCountdown(false, -1);
//            bindDismissButton(false, "");
//        }
//        save();
//    }

    @OnCheckedChanged(R.id.on_off_switch)
    void toggle(boolean checked) {
        // http://stackoverflow.com/q/27641705/5055032
        if (mSwitch.isPressed()) { // filters out automatic calls from VH binding
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

    private void bindDismissButton(Alarm alarm) {
        int hoursBeforeUpcoming = AlarmUtils.hoursBeforeUpcoming(getContext());
        boolean upcoming = alarm.ringsWithinHours(hoursBeforeUpcoming);
        boolean snoozed = alarm.isSnoozed();
        boolean visible = alarm.isEnabled() && (upcoming || snoozed);
        String buttonText = snoozed
                ? getContext().getString(R.string.title_snoozing_until, formatTime(getContext(), alarm.snoozingUntil()))
                : getContext().getString(R.string.dismiss_now);
        setVisibility(mDismissButton, visible);
        mDismissButton.setText(buttonText);
        // Set drawable start
        mDismissButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                upcoming ? mDismissNowDrawable : mCancelSnoozeDrawable,
                null, null, null);
    }

    private void bindLabel(String label) {
        boolean visible = label.length() > 0;
        bindLabel(visible, label);
    }
}
