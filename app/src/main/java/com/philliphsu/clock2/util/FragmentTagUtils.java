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

package com.philliphsu.clock2.util;

import android.support.annotation.IdRes;

/**
 * Created by Phillip Hsu on 9/7/2016.
 */
public final class FragmentTagUtils {

    /**
     * For general use.
     */
    public static String makeTag(Class<?> cls, @IdRes int viewId) {
        return cls.getName() + ":" + viewId;
    }

    /**
     * A version suitable for our ViewHolders.
     */
    public static String makeTag(Class<?> cls, @IdRes int viewId, long itemId) {
        return makeTag(cls, viewId) + ":" + itemId;
    }

    private FragmentTagUtils() {}
}
