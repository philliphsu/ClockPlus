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

    private FragmentTagUtils() {}
}
