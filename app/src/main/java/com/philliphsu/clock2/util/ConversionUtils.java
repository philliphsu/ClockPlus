package com.philliphsu.clock2.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.TypedValue;

/**
 * Created by Phillip Hsu on 4/21/2016.
 */
public final class ConversionUtils {

    public static float dpToPx(@NonNull final Context context, final float dp) {
        return toPx(context, TypedValue.COMPLEX_UNIT_DIP, dp);
    }

    public static float spToPx(@NonNull final Context context, final float sp) {
        return toPx(context, TypedValue.COMPLEX_UNIT_SP, sp);
    }

    private static float toPx(@NonNull final Context context, final int unit, final float val) {
        // This always returns a floating point value, i.e. pixels.
        return TypedValue.applyDimension(unit, val, context.getResources().getDisplayMetrics());
    }

    private ConversionUtils() {}

}
