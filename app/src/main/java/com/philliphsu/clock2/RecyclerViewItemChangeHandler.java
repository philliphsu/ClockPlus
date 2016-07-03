package com.philliphsu.clock2;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Phillip Hsu on 7/1/2016.
 */
public final class RecyclerViewItemChangeHandler<T> {

    private final RecyclerView mRecyclerView;
    private final View mSnackbarAnchor;

    /**
     * @param recyclerView the RecyclerView for which we should handle item change events
     * @param snackbarAnchor an optional anchor for a Snackbar to anchor to
     */
    public RecyclerViewItemChangeHandler(RecyclerView recyclerView, View snackbarAnchor) {
        mRecyclerView = recyclerView;
        mSnackbarAnchor = snackbarAnchor;
    }

    /**
     * Dispatches an item change event to the item in the
     * RecyclerView with the given stable id.
     */
    // This won't work if the change on the item would cause it
    // to be sorted in a different position!
    public void notifyItemChanged(long id) {
        if (id < 0) throw new IllegalArgumentException("id < 0");
        int position = mRecyclerView.findViewHolderForItemId(id).getAdapterPosition();
        mRecyclerView.getAdapter().notifyItemChanged(position);
    }

    public void notifyItemRemoved(long id) {
        if (id < 0) throw new IllegalArgumentException("id < 0");
        int position = mRecyclerView.findViewHolderForItemId(id).getAdapterPosition();
        mRecyclerView.getAdapter().notifyItemRemoved(position);
    }
}
