package com.philliphsu.clock2.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.philliphsu.clock2.Alarm;

/**
 * Created by Phillip Hsu on 5/31/2016.
 */
@Deprecated
public class AlarmsRepository extends BaseRepository<Alarm> {
    private static final String TAG = "AlarmsRepository";
    // Singleton, so this is the sole instance for the lifetime
    // of the application; thus, instance fields do not need to
    // be declared static because they are already associated with
    // this single instance. Since no other instance can exist,
    // any member fields are effectively class fields.
    // **
    // Can't be final, otherwise you'd need to instantiate inline
    // or in static initializer, but ctor requires Context so you
    // can't do that either.
    private static AlarmsRepository sRepo;

    private AlarmsRepository(@NonNull Context context) {
        super(context, new AlarmIoHelper(context));
    }

    public static AlarmsRepository getInstance(@NonNull Context context) {
        if (null == sRepo) {
            Log.d(TAG, "Loading AlarmsRepository for the first time");
            sRepo = new AlarmsRepository(context);
        }
        return sRepo;
    }
}
