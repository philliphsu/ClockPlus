package com.philliphsu.clock2.alarms;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.DaysOfWeek;
import com.philliphsu.clock2.R;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.philliphsu.clock2.DaysOfWeek.NUM_DAYS;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Alarm} and makes a call to the
 * specified {@link AlarmsFragment.OnListFragmentInteractionListener}.
 */
public class AlarmsAdapter extends RecyclerView.Adapter<AlarmsAdapter.ViewHolder> {
    private static final RelativeSizeSpan AMPM_SIZE_SPAN = new RelativeSizeSpan(0.5f);

    private final SortedList<Alarm> mItems;
    private final AlarmsFragment.OnListFragmentInteractionListener mListener;

    public AlarmsAdapter(List<Alarm> alarms, AlarmsFragment.OnListFragmentInteractionListener listener) {
        mItems = new SortedList<>(Alarm.class, new SortedListAdapterCallback<Alarm>(this) {
            @Override
            public int compare(Alarm o1, Alarm o2) {
                return Long.compare(o1.ringsAt(), o2.ringsAt());
            }

            @Override
            public boolean areContentsTheSame(Alarm oldItem, Alarm newItem) {
                return oldItem.hour() == newItem.hour()
                        && oldItem.minutes() == newItem.minutes()
                        && oldItem.isEnabled() == newItem.isEnabled()
                        && oldItem.label().equals(newItem.label())
                        && oldItem.ringsIn() == newItem.ringsIn()
                        && Arrays.equals(oldItem.recurringDays(), newItem.recurringDays())
                        && oldItem.snoozingUntil() == newItem.snoozingUntil();
            }

            @Override
            public boolean areItemsTheSame(Alarm item1, Alarm item2) {
                return item1.id() == item2.id();
            }
        });
        mItems.addAll(alarms);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alarm, parent, false);
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.onBind(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final Context mContext;
        private final AlarmsFragment.OnListFragmentInteractionListener mListener;
        private Alarm mItem;

        @Bind(R.id.time) TextView mTime;
        @Bind(R.id.on_off_switch) SwitchCompat mSwitch;
        @Bind(R.id.label) TextView mLabel;
        @Bind(R.id.countdown) TextView mCountdown; // TODO: Change type to NextAlarmText, once you move that class to this project
        @Bind(R.id.recurring_days) TextView mDays;
        @Bind(R.id.dismiss) Button mDismissButton;

        public ViewHolder(View view, AlarmsFragment.OnListFragmentInteractionListener listener) {
            super(view);
            ButterKnife.bind(this, view);
            mContext = view.getContext();
            mListener = listener;
            view.setOnClickListener(this);
        }

        public void onBind(Alarm alarm) {
            mItem = alarm;
            String time = DateFormat.getTimeFormat(mContext).format(new Date(alarm.ringsAt()));
            if (DateFormat.is24HourFormat(mContext)) {
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
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
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
                    text = mContext.getString(R.string.every_day);
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; // ordinal number, i.e. the position in the week, not an actual day!
                         i < NUM_DAYS; i++) {
                        if (alarm.isRecurring(i)) { // Is the i-th day in the week recurring?
                            // This is the actual day at the i-th position in the week.
                            int weekDay = DaysOfWeek.getInstance(mContext).weekDay(i);
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

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onListFragmentInteraction(mItem);
            }
        }
    }
}
