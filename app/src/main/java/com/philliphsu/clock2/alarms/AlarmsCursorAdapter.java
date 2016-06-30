package com.philliphsu.clock2.alarms;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.OnListItemInteractionListener;
import com.philliphsu.clock2.model.AlarmDatabaseHelper.AlarmCursor;

/**
 * Created by Phillip Hsu on 6/29/2016.
 *
 * TODO: Make this abstract for other data types.
 */
public class AlarmsCursorAdapter extends RecyclerView.Adapter<AlarmViewHolder> {
    private static final String TAG = "AlarmsCursorAdapter";

    private final OnListItemInteractionListener<Alarm> mListener;
    private AlarmCursor mCursor;

    public AlarmsCursorAdapter(OnListItemInteractionListener<Alarm> listener) {
        mListener = listener;
        setHasStableIds(true); // TODO: why do we need this?
    }

    @Override
    public AlarmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AlarmViewHolder(parent, mListener);
    }

    @Override
    public void onBindViewHolder(AlarmViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            Log.e(TAG, "Failed to bind alarm " + position);
            return;
        }
        holder.onBind(mCursor.getAlarm());
    }

    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        if (mCursor == null || !mCursor.moveToPosition(position)) {
            return super.getItemId(position); // -1
        }
        return mCursor.getId();
    }

    // TODO: Cursor param should be the appropriate subclass?
    public void swapCursor(Cursor cursor) {
        if (mCursor == cursor) {
            return;
        }
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = (AlarmCursor) cursor;
        notifyDataSetChanged();
    }
}
