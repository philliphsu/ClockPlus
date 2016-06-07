package com.philliphsu.clock2.editalarm;

/**
 * Created by Phillip Hsu on 6/2/2016.
 *
 * TODO: Consider NOT extending from AlarmContract. Instead, define the methods
 * specific to this interface. Make a base implementation class of the base
 * AlarmContract. When you create an implementation class of the more specific
 * interface, all you need to do is implement the more specific interface.
 * The base impl class will already implement the base interface methods.
 * That way, each concrete interface impl doesn't need to implement the
 * base interface methods again and again.
 */
public interface AlarmItemContract {

    interface View extends AlarmContract.View {
        void showTime(String time);
        void showCountdown(long remainingTime);
        void showRecurringDays(String recurringDays);
    }

    interface Presenter extends AlarmContract.Presenter {
        void setEnabled(boolean enabled);
    }
}
