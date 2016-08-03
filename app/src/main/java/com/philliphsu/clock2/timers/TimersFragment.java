package com.philliphsu.clock2.timers;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;

import com.philliphsu.clock2.AsyncTimersTableUpdateHandler;
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

    private AsyncTimersTableUpdateHandler mAsyncTimersTableUpdateHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAsyncTimersTableUpdateHandler = new AsyncTimersTableUpdateHandler(getActivity(), this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK || data == null)
            return;
        int hour = data.getIntExtra(EditTimerActivity.EXTRA_HOUR, -1);
        int minute = data.getIntExtra(EditTimerActivity.EXTRA_MINUTE, -1);
        int second = data.getIntExtra(EditTimerActivity.EXTRA_SECOND, -1);
        String label = data.getStringExtra(EditTimerActivity.EXTRA_LABEL);
        boolean startTimer = data.getBooleanExtra(EditTimerActivity.EXTRA_START_TIMER, false);
        // TODO: Timer's group?

        Timer t = Timer.createWithLabel(hour, minute, second, label);
        if (startTimer) {
            t.start();
        }
        mAsyncTimersTableUpdateHandler.asyncInsert(t);
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
        // Create a new adapter. This is called before we can initialize mAsyncTimersTableUpdateHandler,
        // so right now it is null. However, after super.onCreate() returns, it is initialized, and
        // the reference variable will be pointing to an actual object. This assignment "propagates"
        // to all references to mAsyncTimersTableUpdateHandler.
        return new TimersCursorAdapter(this, mAsyncTimersTableUpdateHandler);
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

    @Override
    protected void onScrolledToStableId(long id, int position) {

    }
}
