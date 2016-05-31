package com.philliphsu.clock2.alarms;

import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.R;

import java.util.Arrays;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Alarm} and makes a call to the
 * specified {@link AlarmsFragment.OnListFragmentInteractionListener}.
 */
public class AlarmsAdapter extends RecyclerView.Adapter<AlarmViewHolder> {

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
    public AlarmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // TODO: Move this to the BaseAdapter.
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(final AlarmViewHolder holder, int position) {
        // TODO: Move this to the BaseAdapter.
        holder.onBind(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        // TODO: Move this to the BaseAdapter.
        return mItems.size();
    }
}
