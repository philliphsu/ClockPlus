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

package com.philliphsu.clock2.stopwatch.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.philliphsu.clock2.list.BaseViewHolder;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.stopwatch.Lap;

import butterknife.BindView;


/**
 * Created by Phillip Hsu on 8/8/2016.
 */
public class LapViewHolder extends BaseViewHolder<Lap> {

    @BindView(R.id.lap_number) TextView mLapNumber;
    @BindView(R.id.elapsed_time) ChronometerWithMillis mElapsedTime;
    @BindView(R.id.total_time) TextView mTotalTime;

    public LapViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_lap, null);
    }

    @Override
    public void onBind(Lap lap) {
        super.onBind(lap);
        if (getItemViewType() == LapsAdapter.VIEW_TYPE_FIRST_LAP) {
            itemView.setVisibility(View.GONE);
        } else {
            mLapNumber.setText(String.valueOf(lap.getId()));
            bindElapsedTime(lap);
            bindTotalTime(lap);
        }
    }

    private void bindElapsedTime(Lap lap) {
        // In case we're reusing a chronometer instance that could be running:
        // If the lap is not running, this just guarantees the chronometer
        // won't tick, regardless of whether it was running.
        // If the lap is running, we don't care whether the chronometer is
        // also running, because we call start() right after. Stopping it just
        // guarantees that, if it was running, we don't deliver another set of
        // concurrent messages to its handler.
        mElapsedTime.stop();
        // We're going to forget about the + sign in front of the text. I think
        // the 'Elapsed' header column is sufficient to convey what this timer means.
        // (Don't want to figure out a solution)
        mElapsedTime.setDuration(lap.elapsed());
        if (lap.isRunning()) {
            mElapsedTime.start();
        }
    }

    private void bindTotalTime(Lap lap) {
        if (lap.isEnded()) {
            mTotalTime.setVisibility(View.VISIBLE);
            mTotalTime.setText(lap.totalTimeText());
        } else {
            mTotalTime.setVisibility(View.INVISIBLE);
        }
    }
}
