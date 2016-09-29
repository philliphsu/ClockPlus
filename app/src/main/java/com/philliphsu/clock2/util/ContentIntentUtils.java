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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.philliphsu.clock2.MainActivity;
import com.philliphsu.clock2.list.RecyclerViewFragment;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;

/**
 * Created by Phillip Hsu on 9/28/2016.
 *
 * Helper to create content intents for e.g. notifications that should
 * open the app, scroll to the specified page, and then scroll to the
 * item with the specified stable id.
 */
public final class ContentIntentUtils {

    public static PendingIntent create(@NonNull Context context, int targetPage, long stableId) {
        Intent intent = new Intent(context, MainActivity.class)
                .setAction(RecyclerViewFragment.ACTION_SCROLL_TO_STABLE_ID)
                .putExtra(MainActivity.EXTRA_SHOW_PAGE, targetPage)
                .putExtra(RecyclerViewFragment.EXTRA_SCROLL_TO_STABLE_ID, stableId);
        return PendingIntent.getActivity(context, (int) stableId, intent, FLAG_CANCEL_CURRENT);
    }

    private ContentIntentUtils() {}
}
