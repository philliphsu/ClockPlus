package com.philliphsu.clock2.alarms;

import android.view.ViewGroup;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.BaseAdapter;
import com.philliphsu.clock2.OnListItemInteractionListener;

import java.util.Arrays;
import java.util.List;

public class AlarmsAdapter extends BaseAdapter<Alarm, AlarmViewHolder> {

    public AlarmsAdapter(List<Alarm> alarms, OnListItemInteractionListener<Alarm> listener) {
        super(Alarm.class, alarms, listener);
    }

    @Override
    public AlarmViewHolder onCreateViewHolder(ViewGroup parent, OnListItemInteractionListener<Alarm> listener) {
        return new AlarmViewHolder(parent, listener);
    }

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
}
