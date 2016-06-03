package com.philliphsu.clock2.editalarm;

/**
 * Created by Phillip Hsu on 6/2/2016.
 */
public interface AlarmItem {

    interface View extends AlarmContract.View {
        void showCountdown(long remainingTime);
        void showRecurringDays(String recurringDays);
    }

    interface Presenter extends AlarmContract.Presenter {
        void setEnabled(boolean enabled);
    }
}
