package com.philliphsu.clock2;

import android.support.annotation.StringRes;

/**
 * Created by Phillip Hsu on 6/6/2016.
 */
@Deprecated
public interface SharedPreferencesHelper {
    /** Suitable for retrieving the value of a ListPreference */
    int getInt(@StringRes int key, int defaultValue);
}
