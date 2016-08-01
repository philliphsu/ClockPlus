package com.philliphsu.clock2.timers;

import android.view.ViewGroup;

import com.philliphsu.clock2.BaseCursorAdapter;
import com.philliphsu.clock2.OnListItemInteractionListener;
import com.philliphsu.clock2.Timer;
import com.philliphsu.clock2.model.TimerCursor;

/**
 * Created by Phillip Hsu on 7/29/2016.
 */
public class TimersCursorAdapter extends BaseCursorAdapter<Timer, TimerViewHolder, TimerCursor> {

    public TimersCursorAdapter(OnListItemInteractionListener<Timer> listener) {
        super(listener);
    }

    @Override
    protected TimerViewHolder onCreateViewHolder(ViewGroup parent, OnListItemInteractionListener<Timer> listener, int viewType) {
        return new TimerViewHolder(parent, listener);
    }
}
