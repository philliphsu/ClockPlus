package com.philliphsu.clock2.alarms;

import android.view.ViewGroup;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.BaseCursorAdapter;
import com.philliphsu.clock2.OnListItemInteractionListener;
import com.philliphsu.clock2.util.AlarmController;

/**
 * Created by Phillip Hsu on 6/29/2016.
 *
 * TODO: Extend from BaseCursorAdapter
 */
public class AlarmsCursorAdapter extends BaseCursorAdapter<Alarm, AlarmViewHolder, com.philliphsu.clock2.model.AlarmCursor> {
    private static final String TAG = "AlarmsCursorAdapter";

    private final AlarmController mAlarmController;

    public AlarmsCursorAdapter(OnListItemInteractionListener<Alarm> listener,
                               AlarmController alarmController) {
        super(listener);
        mAlarmController = alarmController;
    }

    @Override
    protected AlarmViewHolder onCreateViewHolder(ViewGroup parent, OnListItemInteractionListener<Alarm> listener) {
        return new AlarmViewHolder(parent, listener, mAlarmController);
    }
}
