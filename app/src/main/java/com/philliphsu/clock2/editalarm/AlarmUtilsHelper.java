package com.philliphsu.clock2.editalarm;

import com.philliphsu.clock2.Alarm;

/**
 * Created by Phillip Hsu on 6/3/2016.
 */
public interface AlarmUtilsHelper {
    void scheduleAlarm(Alarm alarm);
    void cancelAlarm(Alarm alarm, boolean showToast);
}
