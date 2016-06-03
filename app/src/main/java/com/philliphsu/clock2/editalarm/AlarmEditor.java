package com.philliphsu.clock2.editalarm;

/**
 * Created by Phillip Hsu on 6/2/2016.
 */
public interface AlarmEditor {

    interface View extends AlarmContract.View {
        void showRecurringDays(int weekDay, boolean recurs);
        void showRingtone(String ringtone);
        void showVibrates(boolean vibrates);
        void showEditorClosed();
        int getHour();
        int getMinutes();
        boolean isEnabled();
        boolean isRecurringDay(int weekDay);
        String getLabel();
        String getRingtone();
        boolean vibrates();
    }

    interface Presenter extends AlarmContract.Presenter {
        void save();
        void delete();
    }
}
