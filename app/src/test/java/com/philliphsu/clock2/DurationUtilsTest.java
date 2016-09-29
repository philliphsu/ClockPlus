/*
 * Copyright (C) 2016 Phillip Hsu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
