package com.philliphsu.clock2.alarms.ui;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.philliphsu.clock2.alarms.Alarm;
import com.philliphsu.clock2.alarms.misc.AlarmController;
import com.philliphsu.clock2.alarms.misc.AlarmPreferences;
import com.philliphsu.clock2.dialogs.AddLabelDialog;
import com.philliphsu.clock2.dialogs.AddLabelDialogController;
import com.philliphsu.clock2.BaseViewHolder;
import com.philliphsu.clock2.OnListItemInteractionListener;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.TimePickerDialogController;
import com.philliphsu.clock2.aospdatetimepicker.Utils;
import com.philliphsu.clock2.timepickers.BaseTimePickerDialog.OnTimeSetListener;
import com.philliphsu.clock2.util.TimeTextUtils;
import com.philliphsu.clock2.util.FragmentTagUtils;

import java.util.Date;

import butterknife.Bind;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTouch;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.philliphsu.clock2.util.TimeFormatUtils.formatTime;

/**
 * Created by Phillip Hsu on 7/31/2016.
 */
public abstract class BaseAlarmViewHolder extends BaseViewHolder<Alarm> {
    private static final String TAG = "BaseAlarmViewHolder";

    private final AlarmController mAlarmController;
    private final AddLabelDialogController mAddLabelDialogController;
    private final TimePickerDialogController mTimePickerDialogController;

    // TODO: Should we use VectorDrawable type?
    private final Drawable mDismissNowDrawable;
    private final Drawable mCancelSnoozeDrawable;

    final FragmentManager mFragmentManager;

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

        // TODO: This is bad! Use a Controller/Presenter instead...
        // or simply pass in an instance of FragmentManager to the ctor.
        AppCompatActivity act = (AppCompatActivity) getContext();
        mFragmentManager = act.getSupportFragmentManager();
        mAddLabelDialogController = new AddLabelDialogController(mFragmentManager,
            new AddLabelDialog.OnLabelSetListener() {
                @Override
                public void onLabelSet(String label) {
                    final Alarm oldAlarm = getAlarm();
                    Alarm newAlarm = oldAlarm.toBuilder()
                            .label(label)
                            .build();
                    oldAlarm.copyMutableFieldsTo(newAlarm);
                    persistUpdatedAlarm(newAlarm, false);
                }
            }
        );
        mTimePickerDialogController = new TimePickerDialogController(mFragmentManager, getContext(),
            new OnTimeSetListener() {
                @Override
                public void onTimeSet(ViewGroup viewGroup, int hourOfDay, int minute) {
                    final Alarm oldAlarm = getAlarm();
                    // I don't think we need this; scheduling a new alarm that is considered
                    // equal to a previous alarm will overwrite the previous alarm.
//                        mAlarmController.cancelAlarm(oldAlarm, false);
                    Alarm newAlarm = oldAlarm.toBuilder()
                            .hour(hourOfDay)
                            .minutes(minute)
                            .build();
                    oldAlarm.copyMutableFieldsTo(newAlarm);
                    // -------------------------------------------
                    // TOneverDO: precede copyMutableFieldsTo()
                    newAlarm.setEnabled(true); // Always enabled, esp. if oldAlarm is not enabled
                    // ----------------------------------------------
                    persistUpdatedAlarm(newAlarm, true);
                }
            }
        );
    }

    @Override
    public void onBind(Alarm alarm) {
        super.onBind(alarm);
        // Items that are not in view will not be bound. If in one orientation the item was in view
        // and in another it is out of view, then the callback for that item will not be restored
        // for the new orientation.
        mAddLabelDialogController.tryRestoreCallback(makeTag(R.id.label));
        mTimePickerDialogController.tryRestoreCallback(makeTag(R.id.time));
        bindTime(alarm);
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
                // TODO: On 21+, upcoming notification doesn't post immediately
                persistUpdatedAlarm(alarm, true);
            } else {
                mAlarmController.cancelAlarm(alarm, true);
                // cancelAlarm() already calls save() for you.
            }
            mSwitch.setPressed(false); // clear the pressed focus, esp. if setPressed(true) was called manually
        }
    }

    @OnClick(R.id.time)
    void openTimePicker() {
        Alarm alarm = getAlarm();
        mTimePickerDialogController.show(alarm.hour(), alarm.minutes(), makeTag(R.id.time));
    }

    @OnClick(R.id.label)
    void openLabelEditor() {
        mAddLabelDialogController.show(mLabel.getText(), makeTag(R.id.label));
    }

    /**
     * Helper method that should be called each time a change is made to the underlying alarm.
     * We should schedule a new alarm with the AlarmManager any time a change is made, even when
     * it was not the alarm's time that changed. This is so that we cancel and update the
     * PendingIntent's extra data with the most up-to-date Alarm's values. The effect of this
     * is to guarantee that the Intent that will launch RingtoneActivity has the most up-to-date
     * extra data about the updated alarm.
     *
     * @param newAlarm The new alarm that has the updated values
     */
    final void persistUpdatedAlarm(Alarm newAlarm, boolean showSnackbar) {
        mAlarmController.scheduleAlarm(newAlarm, showSnackbar);
        mAlarmController.save(newAlarm);
    }

    private void bindTime(Alarm alarm) {
        String time = DateFormat.getTimeFormat(getContext()).format(new Date(alarm.ringsAt()));
        if (DateFormat.is24HourFormat(getContext())) {
            mTime.setText(time);
        } else {
            TimeTextUtils.setText(time, mTime);
        }

        // Use a mock TextView to get our colors, because its ColorStateList is never
        // mutated for the lifetime of this ViewHolder (even when reused).
        // This solution is robust against dark/light theme changes, whereas using
        // color resources is not.
        TextView colorsSource = (TextView) itemView.findViewById(R.id.colors_source);
        ColorStateList colors = colorsSource.getTextColors();
        int def = colors.getDefaultColor();
        // Too light
//        int disabled = colors.getColorForState(new int[] {-android.R.attr.state_enabled}, def);
        // Material guidelines say text hints and disabled text should have the same color.
        int disabled = colorsSource.getCurrentHintTextColor();
        // However, digging around in the system's textColorHint for 21+ says its 50% black for our
        // light theme. I'd like to follow what the guidelines says, but I want code that is robust
        // against theme changes. Alternatively, override the attribute values to what you want
        // in both your dark and light themes...
//        int disabled = ContextCompat.getColor(getContext(), R.color.text_color_disabled_light);
        // We only have two states, so we don't care about losing the other state colors.
        mTime.setTextColor(alarm.isEnabled() ? def : disabled);
    }

    private void bindSwitch(boolean enabled) {
        mSwitch.setChecked(enabled);
    }

    private void bindDismissButton(Alarm alarm) {
        int hoursBeforeUpcoming = AlarmPreferences.hoursBeforeUpcoming(getContext());
        boolean upcoming = alarm.ringsWithinHours(hoursBeforeUpcoming);
        boolean snoozed = alarm.isSnoozed();
        boolean visible = alarm.isEnabled() && (upcoming || snoozed);
        String buttonText = snoozed
                ? getContext().getString(R.string.title_snoozing_until, formatTime(getContext(), alarm.snoozingUntil()))
                : getContext().getString(R.string.dismiss_now);
        setVisibility(mDismissButton, visible);
        mDismissButton.setText(buttonText);
        // Set drawable start
        Drawable icon = upcoming ? mDismissNowDrawable : mCancelSnoozeDrawable;
        Utils.setTint(icon, mDismissButton.getCurrentTextColor());
        mDismissButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
    }

    private void bindLabel(String label) {
        boolean visible = label.length() > 0;
        bindLabel(visible, label);
    }

    private String makeTag(@IdRes int viewId) {
        return FragmentTagUtils.makeTag(BaseAlarmViewHolder.class, viewId, getItemId());
    }
}
