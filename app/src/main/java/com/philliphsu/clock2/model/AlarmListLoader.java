package com.philliphsu.clock2.model;

import android.content.Context;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.model.AlarmDatabaseHelper.AlarmCursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Phillip Hsu on 7/2/2016.
 */
@Deprecated
public class AlarmListLoader extends DataListLoader<Alarm, AlarmCursor> {

    public AlarmListLoader(Context context) {
        super(context);
    }

    // Why not just have one method where we just call DatabaseManager.getAlarms()?
    // I.e. why not load the cursor and extract the Alarms from it all in one?
    // I figure if the loader is interrupted in the middle of loading, the underlying
    // cursor won't be closed...

    // TODO: If we end up doing it this way, then delete the redundant methods in DatabaseManager.

    @Override
    protected AlarmCursor loadCursor() {
        return DatabaseManager.getInstance(getContext()).queryAlarms();
    }

    @Override
    protected List<Alarm> loadItems(AlarmCursor cursor) {
        ArrayList<Alarm> alarms = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                alarms.add(cursor.getAlarm());
            }
            cursor.close();
        }
        return alarms;
    }
}
