package com.philliphsu.clock2.timers;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;

import com.philliphsu.clock2.RecyclerViewFragment;
import com.philliphsu.clock2.Timer;
import com.philliphsu.clock2.edittimer.EditTimerActivity;
import com.philliphsu.clock2.model.TimerCursor;
import com.philliphsu.clock2.model.TimersListCursorLoader;

public class TimersFragment extends RecyclerViewFragment<
        Timer,
        TimerViewHolder,
        TimerCursor,
        TimersCursorAdapter> {
    public static final int REQUEST_CREATE_TIMER = 0;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode != Activity.RESULT_OK || data == null)
//            return;
    }

    @Override
    public void onFabClick() {
        Intent intent = new Intent(getActivity(), EditTimerActivity.class);
        startActivityForResult(intent, REQUEST_CREATE_TIMER);
    }

    @Nullable
    @Override
    protected TimersCursorAdapter getAdapter() {
        if (super.getAdapter() != null)
            return super.getAdapter();
        // Create a new adapter
        return new TimersCursorAdapter(this);
    }

    @Override
    public Loader<TimerCursor> onCreateLoader(int id, Bundle args) {
        return new TimersListCursorLoader(getActivity());
    }

    @Override
    public void onListItemClick(Timer item, int position) {

    }

    @Override
    public void onListItemDeleted(Timer item) {

    }

    @Override
    public void onListItemUpdate(Timer item, int position) {

    }
}
