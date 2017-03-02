/*
 * Copyright 2017 Phillip Hsu
 *
 * This file is part of ClockPlus.
 *
 * ClockPlus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ClockPlus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ClockPlus.  If not, see <http://www.gnu.org/licenses/>.
 */

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
