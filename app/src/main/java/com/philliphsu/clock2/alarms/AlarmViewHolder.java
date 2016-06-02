package com.philliphsu.clock2.alarms;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.SwitchCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.style.RelativeSizeSpan;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.BaseViewHolder;
import com.philliphsu.clock2.DaysOfWeek;
import com.philliphsu.clock2.OnListItemInteractionListener;
import com.philliphsu.clock2.R;

import java.util.Date;

import butterknife.Bind;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.philliphsu.clock2.DaysOfWeek.NUM_DAYS;

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
        String time = DateFormat.getTimeFormat(getContext()).format(new Date(alarm.ringsAt()));
        if (DateFormat.is24HourFormat(getContext())) {
            mTime.setText(time);
        } else {
            // No way around having to construct this on binding
            SpannableString s = new SpannableString(time);
            s.setSpan(AMPM_SIZE_SPAN, time.indexOf(" "), time.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTime.setText(s, TextView.BufferType.SPANNABLE);
        }

        if (alarm.isEnabled()) {
            mSwitch.setChecked(true);
            //TODO:mCountdown.showAsText(alarm.ringsIn());
            mCountdown.setVisibility(VISIBLE);
            //todo:mCountdown.getTickHandler().startTicking(true)
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            // how many hours before alarm is considered upcoming
            // TODO: shared prefs
            /*int hoursBeforeUpcoming = Integer.parseInt(prefs.getString(
                    mContext.getString(-1TODO:R.string.key_notify_me_of_upcoming_alarms),
                    "2"));*/
            if (alarm.ringsWithinHours(2) || alarm.isSnoozed()) {
                // TODO: Register dynamic broadcast receiver in this class to listen for
                // when this alarm crosses the upcoming threshold, so we can show this button.
                mDismissButton.setVisibility(VISIBLE);
            } else {
                mDismissButton.setVisibility(GONE);
            }
        } else {
            mSwitch.setChecked(false);
            mCountdown.setVisibility(GONE);
            //TODO:mCountdown.getTickHandler().stopTicking();
            mDismissButton.setVisibility(GONE);
        }

        mLabel.setText(alarm.label());
        if (mLabel.length() == 0 && mCountdown.getVisibility() != VISIBLE) {
            mLabel.setVisibility(GONE);
        } else {
            // needed for proper positioning of mCountdown
            mLabel.setVisibility(VISIBLE);
        }

        int numRecurringDays = alarm.numRecurringDays();
        if (numRecurringDays > 0) {
            String text;
            if (numRecurringDays == NUM_DAYS) {
                text = getContext().getString(R.string.every_day);
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
            mDays.setText(text);
            mDays.setVisibility(VISIBLE);
        } else {
            mDays.setVisibility(GONE);
        }
    }
}
