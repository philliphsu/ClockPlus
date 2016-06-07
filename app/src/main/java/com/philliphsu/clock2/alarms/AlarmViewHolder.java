package com.philliphsu.clock2.alarms;

import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.BaseViewHolder;
import com.philliphsu.clock2.DaysOfWeek;
import com.philliphsu.clock2.OnListItemInteractionListener;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.util.AlarmUtils;

import java.util.Date;

import butterknife.Bind;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.philliphsu.clock2.DaysOfWeek.NUM_DAYS;
import static com.philliphsu.clock2.util.DateFormatUtils.formatTime;

/**
 * Created by Phillip Hsu on 5/31/2016.
 */
public class AlarmViewHolder extends BaseViewHolder<Alarm> {
    private static final RelativeSizeSpan AMPM_SIZE_SPAN = new RelativeSizeSpan(0.5f);

    @Bind(R.id.time) TextView mTime;
    @Bind(R.id.on_off_switch) SwitchCompat mSwitch;
    @Bind(R.id.label) TextView mLabel;
    @Bind(R.id.countdown) TextView mCountdown; // TODO: Change type to NextAlarmText, once you move that class to this project
    @Bind(R.id.recurring_days) TextView mDays;
    @Bind(R.id.dismiss) Button mDismissButton;

    public AlarmViewHolder(ViewGroup parent, OnListItemInteractionListener<Alarm> listener) {
        super(parent, R.layout.item_alarm, listener);
    }

    @Override
    public void onBind(Alarm alarm) {
        super.onBind(alarm);
        bindTime(new Date(alarm.ringsAt()));
        bindSwitch(alarm.isEnabled());
        bindCountdown(alarm.isEnabled(), alarm.ringsIn());

        int hoursBeforeUpcoming = AlarmUtils.hoursBeforeUpcoming(getContext());
        boolean visible = alarm.isEnabled() && (alarm.ringsWithinHours(hoursBeforeUpcoming) || alarm.isSnoozed());
        String buttonText = alarm.isSnoozed()
                ? getContext().getString(R.string.title_snoozing_until, formatTime(getContext(), alarm.snoozingUntil()))
                : getContext().getString(R.string.dismiss_now);
        // TODO: Register dynamic broadcast receiver in this class to listen for
        // when this alarm crosses the upcoming threshold, so we can show this button.
        bindDismissButton(visible, buttonText);

        // Should also be visible even if alarm has no label so mCountdown is properly positioned next
        // to mLabel. That is, mCountdown's layout position is dependent on mLabel being present.
        boolean labelVisible = alarm.label().length() > 0 || mCountdown.getVisibility() == VISIBLE;
        bindLabel(labelVisible, alarm.label());

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

    @OnClick(R.id.dismiss)
    void onClick() {
        AlarmUtils.cancelAlarm(getContext(), getItem());
        bindDismissButton(false, ""); // Will be set to correct text the next time we bind.
        // TODO: Check if alarm has no recurrence, then turn it off.
    }

    private void bindTime(Date date) {
        String time = DateFormat.getTimeFormat(getContext()).format(date);
        if (DateFormat.is24HourFormat(getContext())) {
            mTime.setText(time);
        } else {
            // No way around having to construct this on binding
            SpannableString s = new SpannableString(time);
            s.setSpan(AMPM_SIZE_SPAN, time.indexOf(" "), time.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTime.setText(s, TextView.BufferType.SPANNABLE);
        }
    }

    private void bindSwitch(boolean enabled) {
        mSwitch.setChecked(enabled);
    }

    private void bindCountdown(boolean enabled, long remainingTime) {
        if (enabled) {
            //TODO:mCountdown.showAsText(remainingTime);
            //TODO:mCountdown.getTickHandler().startTicking(true)
            mCountdown.setVisibility(VISIBLE);
        } else {
            //TODO:mCountdown.getTickHandler().stopTicking();
            mCountdown.setVisibility(GONE);
        }
    }

    private void bindDismissButton(boolean visible, String buttonText) {
        setVisibility(mDismissButton, visible);
        mDismissButton.setText(buttonText);
    }

    private void bindLabel(boolean visible, String label) {
        setVisibility(mLabel, visible);
        mLabel.setText(label);
    }

    private void bindDays(boolean visible, String text) {
        setVisibility(mDays, visible);
        mDays.setText(text);
    }

    private void setVisibility(@NonNull View view, boolean visible) {
        view.setVisibility(visible ? VISIBLE : GONE);
    }
}
