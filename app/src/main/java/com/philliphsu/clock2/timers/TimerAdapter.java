package com.philliphsu.clock2.timers;

import android.view.ViewGroup;

import com.philliphsu.clock2.BaseAdapter;
import com.philliphsu.clock2.OnListItemInteractionListener;
import com.philliphsu.clock2.Timer;

import java.util.List;

/**
 * Created by Phillip Hsu on 7/26/2016.
 */
public class TimerAdapter extends BaseAdapter<Timer, TimerViewHolder> {

    public TimerAdapter(List<Timer> items, OnListItemInteractionListener<Timer> listener) {
        super(Timer.class, items, listener);
    }

    @Override
    protected TimerViewHolder onCreateViewHolder(ViewGroup parent, OnListItemInteractionListener<Timer> listener) {
        return new TimerViewHolder(parent, listener);
    }

    @Override
    protected int compare(Timer o1, Timer o2) {
        return 0;
    }

    @Override
    protected boolean areContentsTheSame(Timer oldItem, Timer newItem) {
        return false;
    }

    @Override
    protected boolean areItemsTheSame(Timer item1, Timer item2) {
        return false;
    }
}
