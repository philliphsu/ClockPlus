package com.philliphsu.clock2.util;

/**
 * Created by Phillip Hsu on 5/28/2016.
 */
public final class Preconditions {
    private Preconditions() {}

    public static <T> T checkNotNull(T obj) {
        if (null == obj)
            throw new NullPointerException();
        return obj;
    }
}
