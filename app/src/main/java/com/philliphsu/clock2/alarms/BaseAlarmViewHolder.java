package com.philliphsu.clock2.alarms;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.philliphsu.clock2.AddLabelDialog;
import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.BaseViewHolder;
import com.philliphsu.clock2.OnListItemInteractionListener;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.editalarm.BaseTimePickerDialog;
import com.philliphsu.clock2.editalarm.BaseTimePickerDialog.OnTimeSetListener;
import com.philliphsu.clock2.editalarm.TimePickerHelper;
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
    private static final String TAG = "BaseAlarmViewHolder";
    private static final String TAG_ADD_LABEL_DIALOG = "add_label_dialog";

    // Visible for subclasses.
    final AlarmController mAlarmController;

    // TODO: Should we use VectorDrawable type?
    private final Drawable mDismissNowDrawable;
    private final Drawable mCancelSnoozeDrawable;
    private final FragmentManager mFragmentManager;

    // These should only be changed from the OnTimeSet callback.
    // If we had initialized these in onBind(), they would be reset to their original values
    // from the Alarm each time the ViewHolder binds.
    // A value of -1 indicates that the Alarm's time has not been changed.
    int mSelectedHourOfDay = -1;
    int mSelectedMinute = -1;

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

        // Are we recreating this because of a rotation?
        // If so, try finding any dialog that was last shown in our backstack,
        // and restore the callback.
        BaseTimePickerDialog picker = (BaseTimePickerDialog)
                mFragmentManager.findFragmentByTag(AlarmsFragment.TAG_TIME_PICKER);
        if (picker != null) {
            Log.i(TAG, "Restoring time picker callback");
            picker.setOnTimeSetListener(newOnTimeSetListener());
        }
        AddLabelDialog labelDialog = (AddLabelDialog)
                mFragmentManager.findFragmentByTag(TAG_ADD_LABEL_DIALOG);
        if (labelDialog != null) {
            Log.i(TAG, "Restoring add label callback");
            labelDialog.setOnLabelSetListener(newOnLabelSetListener());
        }
    }

    @Override
    public void onBind(Alarm alarm) {
        super.onBind(alarm);
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
                mAlarmController.scheduleAlarm(alarm, true);
                mAlarmController.save(alarm);
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
        BaseTimePickerDialog dialog = TimePickerHelper.newDialog(getContext(),
                newOnTimeSetListener(), alarm.hour(), alarm.minutes());
        dialog.show(mFragmentManager, AlarmsFragment.TAG_TIME_PICKER);
    }

    @OnClick(R.id.label)
    void openLabelEditor() {
        AddLabelDialog dialog = AddLabelDialog.newInstance(newOnLabelSetListener(), mLabel.getText());
        dialog.show(mFragmentManager, TAG_ADD_LABEL_DIALOG);
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

    private AddLabelDialog.OnLabelSetListener newOnLabelSetListener() {
        // Create a new listener per request. This is primarily used for
        // setting the dialog callback again after a rotation.
        //
        // If we saved a reference to a listener, it would be tied to
        // its ViewHolder instance. ViewHolders are reused, so we
        // could accidentally leak this reference to other Alarm items
        // in the list.
        return new AddLabelDialog.OnLabelSetListener() {
            @Override
            public void onLabelSet(String label) {
                final Alarm oldAlarm = getAlarm();
                Alarm newAlarm = oldAlarm.toBuilder()
                        .label(label)
                        .build();
                oldAlarm.copyMutableFieldsTo(newAlarm);
                mAlarmController.save(newAlarm);
            }
        };
    }

    private OnTimeSetListener newOnTimeSetListener() {
        // Create a new listener per request. This is primarily used for
        // setting the dialog callback again after a rotation.
        //
        // If we saved a reference to a listener, it would be tied to
        // its ViewHolder instance. ViewHolders are reused, so we
        // could accidentally leak this reference to other Alarm items
        // in the list.
        return new OnTimeSetListener() {
            @Override
            public void onTimeSet(ViewGroup viewGroup, int hourOfDay, int minute) {
                mSelectedHourOfDay = hourOfDay;
                mSelectedMinute = minute;
            }
        };
    }
}
