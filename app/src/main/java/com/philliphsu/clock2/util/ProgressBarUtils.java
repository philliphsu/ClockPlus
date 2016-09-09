package com.philliphsu.clock2.util;

import android.animation.ObjectAnimator;
import android.widget.ProgressBar;

/**
 * Created by Phillip Hsu on 8/10/2016.
 */
public class ProgressBarUtils {
    private static final int MAX_PROGRESS = 1000000;

    /**
     * Constructs and starts a new countdown ObjectAnimator with the given properties.
     * @param ratio The initial ratio to show on the progress bar. This will be scaled
     *              by {@link #MAX_PROGRESS}. A ratio of 1 means the bar is full.
     * @return the created ObjectAnimator for holding a reference to
     */
    public static ObjectAnimator startNewAnimator(ProgressBar bar, double ratio, long duration) {
        bar.setMax(MAX_PROGRESS);
        final int progress = scaleRatio(ratio);
        ObjectAnimator animator = ObjectAnimator.ofInt(
                // The object that has the property we wish to animate
                bar,
                // The name of the property of the object that identifies which setter method
                // the animation will call to update its values. Here, a property name of
                // "progress" will result in a call to the function setProgress() in ProgressBar.
                // The docs for ObjectAnimator#setPropertyName() says that for best performance,
                // the setter method should take a float or int parameter, and its return type
                // should be void (both of which setProgress() satisfies).
                "progress",
                // The set of values to animate between. A single value implies that that value
                // is the one being animated to. Two values imply starting and ending values.
                // More than two values imply a starting value, values to animate through along
                // the way, and an ending value (these values will be distributed evenly across
                // the duration of the animation).
                // TODO: Consider leaving the set of values up to the client. Currently, we
                // have hardcoded this animator to be a "countdown" progress bar. This is
                // sufficient for our current needs.
                progress, 0);
        animator.setDuration(duration < 0 ? 0 : duration);
        // The algorithm that calculates intermediate values between keyframes. We use linear
        // interpolation so that the animation runs at constant speed.
        animator.setInterpolator(null/*results in linear interpolation*/);
        // This MUST be run on the UI thread.
        animator.start();
        return animator;
    }

    /**
     * Wrapper around {@link ProgressBar#setProgress(int) setProgress(int)} that keeps {@code bar}'s
     * max in sync with the animation's progress scale factor.
     */
    public static void setProgress(ProgressBar bar, double ratio) {
        bar.setMax(MAX_PROGRESS);
        bar.setProgress(scaleRatio(ratio));
    }

    private static int scaleRatio(double ratio) {
        return (int) (MAX_PROGRESS * ratio);
    }
}
