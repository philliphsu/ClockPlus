package com.philliphsu.clock2.model;

import android.content.Context;

import com.philliphsu.clock2.Alarm;

/**
 * Created by Phillip Hsu on 6/30/2016.
 */
public class AlarmLoader extends DataLoader<Alarm> {

    private long mAlarmId;

    // TODO: Consider writing a super ctor that has the id param, so
    // subclasses don't need to write their own.
    public AlarmLoader(Context context, long alarmId) {
        super(context);
        mAlarmId = alarmId;
    }

    @Override
    public Alarm loadInBackground() {
        return new AlarmsTableManager(getContext()).queryItem(mAlarmId).getItem();
    }
}
