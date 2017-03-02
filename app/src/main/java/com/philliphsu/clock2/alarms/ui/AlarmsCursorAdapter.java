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

package com.philliphsu.clock2.alarms.ui;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.philliphsu.clock2.list.BaseCursorAdapter;
import com.philliphsu.clock2.list.OnListItemInteractionListener;
import com.philliphsu.clock2.alarms.Alarm;
import com.philliphsu.clock2.alarms.data.AlarmCursor;
import com.philliphsu.clock2.alarms.misc.AlarmController;

/**
 * Created by Phillip Hsu on 6/29/2016.
 */
public class AlarmsCursorAdapter extends BaseCursorAdapter<Alarm, BaseAlarmViewHolder, AlarmCursor> {
    private static final String TAG = "AlarmsCursorAdapter";
    private static final int VIEW_TYPE_COLLAPSED = 0;
    private static final int VIEW_TYPE_EXPANDED = 1;

    private final AlarmController mAlarmController;

    // TOneverDO: initial value >= 0
    private int mExpandedPosition = RecyclerView.NO_POSITION;
    private long mExpandedId = RecyclerView.NO_ID;

    public AlarmsCursorAdapter(OnListItemInteractionListener<Alarm> listener,
                               AlarmController alarmController) {
        super(listener);
        mAlarmController = alarmController;
    }

    @Override
    protected BaseAlarmViewHolder onCreateViewHolder(ViewGroup parent, OnListItemInteractionListener<Alarm> listener, int viewType) {
        if (viewType == VIEW_TYPE_COLLAPSED)
            return new CollapsedAlarmViewHolder(parent, listener, mAlarmController);
        return new ExpandedAlarmViewHolder(parent, listener, mAlarmController);
    }

    @Override
    public int getItemViewType(int position) {
        final long stableId = getItemId(position);
        return stableId != RecyclerView.NO_ID && stableId == mExpandedId
//                position == mExpandedPosition
                ? VIEW_TYPE_EXPANDED : VIEW_TYPE_COLLAPSED;
    }

//    // TODO
//    public void saveInstance(Bundle outState) {
//        outState.putLong(KEY_EXPANDED_ID, mExpandedId);
//    }

    public boolean expand(int position) {
        if (position == RecyclerView.NO_POSITION)
            return false;
        final long stableId = getItemId(position);
        if (stableId == RecyclerView.NO_ID || mExpandedId == stableId)
            return false;
        mExpandedId = stableId;
        // If we can call this, the item is in view, so we don't need to scroll to it?
//        mScrollHandler.smoothScrollTo(position);
        if (mExpandedPosition >= 0) {
            // Collapse this position first. getItemViewType() will be called
            // in onCreateViewHolder() to verify which ViewHolder to create
            // for the position.
            notifyItemChanged(mExpandedPosition);
        }
        mExpandedPosition = position;
        notifyItemChanged(position);
        return true;

        // This would be my alternative solution. But we're keeping Google's
        // because the stable ID *could* hold up better for orientation changes
        // than the position? I.e. when saving instance state we save the id.
//        int oldExpandedPosition = mExpandedPosition;
//        mExpandedPosition = position;
//        if (oldExpandedPosition >= 0) {
//            notifyItemChanged(oldExpandedPosition);
//        }
//        notifyItemChanged(mExpandedPosition);
    }

    public void collapse(int position) {
        mExpandedId = RecyclerView.NO_ID;
        mExpandedPosition = RecyclerView.NO_POSITION;
        notifyItemChanged(position);
    }

    public int getExpandedPosition() {
        return mExpandedPosition;
    }
}
