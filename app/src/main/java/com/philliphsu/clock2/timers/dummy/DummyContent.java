package com.philliphsu.clock2.timers.dummy;

import com.philliphsu.clock2.Timer;

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
    public static final List<Timer> ITEMS = new ArrayList<>();

    private static final int COUNT = 10;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createTimer(i));
        }
    }

    private static void addItem(Timer item) {
        ITEMS.add(item);
    }

    private static Timer createTimer(int position) {
        return Timer.create(1, 0, 0);
    }
}
