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

package com.philliphsu.clock2.stopwatch.ui;

import android.view.ViewGroup;

import com.philliphsu.clock2.list.BaseCursorAdapter;
import com.philliphsu.clock2.list.OnListItemInteractionListener;
import com.philliphsu.clock2.stopwatch.Lap;
import com.philliphsu.clock2.stopwatch.data.LapCursor;

/**
 * Created by Phillip Hsu on 8/9/2016.
 */
public class LapsAdapter extends BaseCursorAdapter<Lap, LapViewHolder, LapCursor> {
    public static final int VIEW_TYPE_FIRST_LAP = 1; // TOneverDO: 0, that's the default view type

    public LapsAdapter() {
        super(null/*OnListItemInteractionListener*/);
    }

    @Override
    protected LapViewHolder onCreateViewHolder(ViewGroup parent, OnListItemInteractionListener<Lap> listener, int viewType) {
        // TODO: Consider defining a view type for the lone first lap. We should persist this first lap
        // when is is created, but this view type will tell us it should not be visible until there
        // are at least two laps.
        // Or could we return null? Probably not because that will get passed into onBindVH, unless
        // you check for null before accessing it.
        // RecyclerView.ViewHolder has an internal field that holds viewType for us,
        // and can be retrieved by the instance via getItemViewType().
        return new LapViewHolder(parent);
    }

    @Override
    public int getItemViewType(int position) {
        return getItemCount() == 1 ? VIEW_TYPE_FIRST_LAP : super.getItemViewType(position);
    }
}
