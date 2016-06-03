package com.philliphsu.clock2.editalarm;

/**
 * Created by Phillip Hsu on 6/2/2016.
 */
interface AlarmContract {

    interface View {
        void showTime(String time);
        void showLabel(String label);
        void showEnabled(boolean enabled);
        void showCanDismissNow();
        void showSnoozed(String message);
    }

    interface Presenter {
        void dismissNow();
        void endSnoozing();
    }
}
