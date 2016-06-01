package com.philliphsu.clock2.alarms.dummy;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.DaysOfWeek;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Alarm> ITEMS = new ArrayList<>();

    private static final int COUNT = 10;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createAlarm(i));
        }
    }

    private static void addItem(Alarm item) {
        ITEMS.add(item);
    }

    private static Alarm createAlarm(int position) {
        Alarm.Builder b = Alarm.builder();
        if (position % 2 == 0) {
            b.hour(21).minutes(0);
        }
        boolean[] recurrences = new boolean[DaysOfWeek.NUM_DAYS];
        recurrences[0] = true;
        Alarm a = b.id(position).recurringDays(recurrences).build();
        a.setEnabled(true);
        return a;
    }
}
