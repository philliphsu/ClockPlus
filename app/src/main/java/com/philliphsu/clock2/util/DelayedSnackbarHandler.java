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
