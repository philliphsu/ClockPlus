package com.philliphsu.clock2.model;

import android.content.Context;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.model.AlarmDatabaseHelper.AlarmCursor;

import java.util.ArrayList;

/**
 * Created by Phillip Hsu on 6/25/2016.
 */
public class DatabaseManager {

    private static DatabaseManager sDatabaseManager;
    private final Context mContext;
    private final AlarmDatabaseHelper mHelper; // TODO: Call close() when *the app* is exiting.

    private DatabaseManager(Context context) {
        // TODO: Do we need to hold onto this?
        mContext = context.getApplicationContext();
        // This will internally get the application context
        mHelper = new AlarmDatabaseHelper(context);
    }

    public static DatabaseManager getInstance(Context context) {
        if (sDatabaseManager == null) {
            sDatabaseManager = new DatabaseManager(context);
        }
        return sDatabaseManager;
    }

    public long insertAlarm(Alarm alarm) {
        return mHelper.insertAlarm(alarm);
    }

    /**
     * @deprecated Use {@link #updateAlarm(long, Alarm)} instead, because all
     *             that is needed from the oldAlarm is its id.
     */
    @Deprecated
    public int updateAlarm(Alarm oldAlarm, Alarm newAlarm) {
        return mHelper.updateAlarm(oldAlarm, newAlarm);
    }

    public int updateAlarm(long id, Alarm newAlarm) {
        return mHelper.updateAlarm(id, newAlarm);
    }

    public int deleteAlarm(Alarm alarm) {
        return mHelper.deleteAlarm(alarm);
    }

    // Since the query returns at most one row, just return the Alarm the row represents.
    public Alarm getAlarm(long id) {
        Alarm alarm = null;
        AlarmCursor cursor = mHelper.queryAlarm(id);
        if (cursor != null && cursor.moveToFirst()) {
            alarm = cursor.getAlarm();
            cursor.close();
        }
        return alarm;
    }

    /** @deprecated Use {@link #queryAlarms()} */
    // TODO: Possible redundant. See AlarmListLoader.
    @Deprecated
    public ArrayList<Alarm> getAlarms() {
        return getAlarms(mHelper.queryAlarms());
    }

    // TODO: Possible redundant. See AlarmListLoader.
    public ArrayList<Alarm> getEnabledAlarms() {
        return getAlarms(mHelper.queryEnabledAlarms());
    }

    // TODO: Possible redundant. See AlarmListLoader.
    private ArrayList<Alarm> getAlarms(AlarmCursor cursor) {
        ArrayList<Alarm> alarms = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                alarms.add(cursor.getAlarm());
            }
            cursor.close();
        }
        return alarms;
    }

    public AlarmCursor queryAlarms() {
        return mHelper.queryAlarms();
    }

    public AlarmCursor queryEnabledAlarms() {
        return mHelper.queryEnabledAlarms();
    }
}
