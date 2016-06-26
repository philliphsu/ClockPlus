package com.philliphsu.clock2.editalarm;

import java.util.Date;

/**
 * Created by Phillip Hsu on 6/2/2016.
 */
@Deprecated
interface AlarmContract {

    interface View {
        void showLabel(String label);
        void showEnabled(boolean enabled);
        void showCanDismissNow();
        void showSnoozed(Date snoozingUntilMillis);
    }

    interface Presenter {
        void dismissNow();
        void stopSnoozing();
    }
}
