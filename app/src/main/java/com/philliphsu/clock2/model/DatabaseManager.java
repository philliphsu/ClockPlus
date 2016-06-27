package com.philliphsu.clock2.model;

import android.content.Context;

import com.philliphsu.clock2.Alarm;

import java.util.ArrayList;

/**
 * Created by Phillip Hsu on 6/25/2016.
 */
public class DatabaseManager {

    private static DatabaseManager sDatabaseManager;
    private final Context mContext;
    private final AlarmDatabaseHelper mHelper;

    private DatabaseManager(Context context) {
        mContext = context.getApplicationContext();
        mHelper = new AlarmDatabaseHelper(mContext);
    }

    public static DatabaseManager getInstance(Context context) {
        if (sDatabaseManager == null) {
            sDatabaseManager = new DatabaseManager(context);
        }
        return sDatabaseManager;
    }

    // TODO: why return an Alarm?
    public Alarm insertAlarm(Alarm alarm) {
        mHelper.insertAlarm(alarm);
        return alarm;
    }

    public int updateAlarm(Alarm oldAlarm, Alarm newAlarm) {
        return mHelper.updateAlarm(oldAlarm, newAlarm);
    }

    public int deleteAlarm(Alarm alarm) {
        return mHelper.deleteAlarm(alarm);
    }

    // Since the query returns at most one row, just return the Alarm the row represents.
    public Alarm getAlarm(long id) {
        Alarm alarm = null;
        AlarmDatabaseHelper.AlarmCursor cursor = mHelper.queryAlarm(id);
        if (cursor != null && cursor.moveToFirst()) {
            alarm = cursor.getAlarm();
            cursor.close();
        }
        return alarm;
    }

    public ArrayList<Alarm> getAlarms() {
        ArrayList<Alarm> alarms = new ArrayList<>();
        AlarmDatabaseHelper.AlarmCursor cursor = mHelper.queryAlarms();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                alarms.add(cursor.getAlarm());
            }
            cursor.close();
        }
        return alarms;
    }
}
