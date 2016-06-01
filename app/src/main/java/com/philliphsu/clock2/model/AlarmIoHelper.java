package com.philliphsu.clock2.model;

import android.content.Context;
import android.support.annotation.NonNull;

import com.philliphsu.clock2.Alarm;

import org.json.JSONObject;

/**
 * Created by Phillip Hsu on 5/31/2016.
 */
public class AlarmIoHelper extends JsonIoHelper<Alarm> {
    private static final String FILENAME = "alarms.json";

    public AlarmIoHelper(@NonNull Context context) {
        super(context, FILENAME);
    }

    @Override
    protected Alarm newItem(@NonNull JSONObject jsonObject) {
        return Alarm.create(jsonObject);
    }
}
