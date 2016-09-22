package com.philliphsu.clock2.list;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import com.philliphsu.clock2.data.BaseItemCursor;
import com.philliphsu.clock2.data.ObjectWithId;

/**
 * Created by Phillip Hsu on 7/29/2016.
 */
public abstract class BaseCursorAdapter<
        T extends ObjectWithId,
        VH extends BaseViewHolder<T>,
        C extends BaseItemCursor<T>>
    extends RecyclerView.Adapter<VH> {

    private static final String TAG = "BaseCursorAdapter";

    private final OnListItemInteractionListener<T> mListener;
    private C mCursor;

    protected abstract VH onCreateViewHolder(ViewGroup parent, OnListItemInteractionListener<T> listener, int viewType);

    public BaseCursorAdapter(OnListItemInteractionListener<T> listener) {
        mListener = listener;
        // Excerpt from docs of notifyDataSetChanged():
        // "RecyclerView will attempt to synthesize [artificially create?]
        // visible structural change events [when items are inserted, removed or
        // moved] for adapters that report that they have stable IDs when
        // [notifyDataSetChanged()] is used. This can help for the purposes of
        // animation and visual object persistence [?] but individual item views
        // will still need to be rebound and relaid out."
        setHasStableIds(true);
    }

    /**
     * not final to allow subclasses to use the viewType if needed
     */
    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return onCreateViewHolder(parent, mListener, viewType);
    }

    @Override
    public final void onBindViewHolder(VH holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            Log.e(TAG, "Failed to bind item at position " + position);
            return;
        }
        holder.onBind(mCursor.getItem());
    }

    @Override
    public final int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    @Override
    public final long getItemId(int position) {
        if (mCursor == null || !mCursor.moveToPosition(position)) {
            return super.getItemId(position); // -1
        }
        return mCursor.getId();
    }

    public final void swapCursor(C cursor) {
        if (mCursor == cursor) {
            return;
        }
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;
        notifyDataSetChanged();
    }
}
