package com.philliphsu.clock2;

import com.philliphsu.clock2.util.DurationUtils;

import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by Phillip Hsu on 6/10/2016.
 */
public class DurationUtilsTest {

    @Test
    public void testBreakdown() {
        long duration = TimeUnit.HOURS.toMillis(45)
                + TimeUnit.MINUTES.toMillis(97);
        long[] l = DurationUtils.breakdown(duration);
        System.out.println(Arrays.toString(l));
    }

}
