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

package com.philliphsu.clock2.util;

import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Created by Phillip Hsu on 7/10/2016.
 *
 * Handler to prepare a Snackbar to be shown only when requested to.
 * Useful when the Snackbar is created in an app component that
 * is not where it should be shown.
 */
public final class DelayedSnackbarHandler {
    // TODO: Consider wrapping this in a WeakReference, so that you
    // don't prevent this from being GCed if you never call #show().
    private static Snackbar snackbar;
    private static String message;

    private DelayedSnackbarHandler() {}

    /**
     * Saves a reference to the given Snackbar, so that you can
     * call {@link #show()} at a later time.
     */
    public static void prepareSnackbar(Snackbar sb) {
        snackbar = sb;
    }

    /**
     * Shows the Snackbar previously prepared with
     * {@link #prepareSnackbar(Snackbar)}
     */
    public static void show() {
        if (snackbar != null) {
            snackbar.show();
            snackbar = null;
        }
    }

    /**
     * Saves a static reference to the message, so that you can
     * call {@link #makeAndShow(View)} at a later time.
     */
    public static void prepareMessage(String msg) {
        message = msg;
    }

    /**
     * Makes a Snackbar with the message previously prepared with
     * {@link #prepareMessage(String)} and shows it.
     */
    public static void makeAndShow(View snackbarAnchor) {
        if (snackbarAnchor != null && message != null) {
            Snackbar.make(snackbarAnchor, message, Snackbar.LENGTH_LONG).show();
            message = null;
        }
    }
}
