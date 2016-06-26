package com.philliphsu.clock2.model;

import android.content.Context;

import com.philliphsu.clock2.Alarm;

/**
 * Created by Phillip Hsu on 6/25/2016.
 *
 * TODO: Not sure this class is all that useful? We could make the DbHelper singleton instance in its own class.
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

    public Alarm insertAlarm(Alarm alarm) {
        mHelper.insertAlarm(alarm);
        return alarm;
    }
}
