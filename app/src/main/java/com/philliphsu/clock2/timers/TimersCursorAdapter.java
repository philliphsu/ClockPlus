package com.philliphsu.clock2.timers;

import android.view.ViewGroup;

import com.philliphsu.clock2.AsyncTimersTableUpdateHandler;
import com.philliphsu.clock2.list.BaseCursorAdapter;
import com.philliphsu.clock2.list.OnListItemInteractionListener;
import com.philliphsu.clock2.Timer;
import com.philliphsu.clock2.model.TimerCursor;

/**
 * Created by Phillip Hsu on 7/29/2016.
 */
public class TimersCursorAdapter extends BaseCursorAdapter<Timer, TimerViewHolder, TimerCursor> {

    private final AsyncTimersTableUpdateHandler mAsyncTimersTableUpdateHandler;

    public TimersCursorAdapter(OnListItemInteractionListener<Timer> listener,
                               AsyncTimersTableUpdateHandler asyncTimersTableUpdateHandler) {
        super(listener);
        mAsyncTimersTableUpdateHandler = asyncTimersTableUpdateHandler;
    }

    @Override
    protected TimerViewHolder onCreateViewHolder(ViewGroup parent, OnListItemInteractionListener<Timer> listener, int viewType) {
        return new TimerViewHolder(parent, listener, mAsyncTimersTableUpdateHandler);
    }


}
