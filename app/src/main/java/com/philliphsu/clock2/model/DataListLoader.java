package com.philliphsu.clock2.model;

import android.content.Context;
import android.database.CursorWrapper;
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;

/**
 * Created by Phillip Hsu on 7/2/2016.
 */
@Deprecated
// TODO: Consider C extends MyTypeBoundedCursorWrapper<D>
public abstract class DataListLoader<D, C extends CursorWrapper> extends AsyncTaskLoader<List<D>> {
    
    private C mCursor;
    private List<D> mItems;
    
    public DataListLoader(Context context) {
        super(context);
    }
    
    protected abstract C loadCursor();
    protected abstract List<D> loadItems(C cursor);

    @Override
    public List<D> loadInBackground() {
        mCursor = loadCursor();
        if (mCursor != null) {
            // Ensure that the content window is filled
            // Ensure that the data is available in memory once it is
            // passed to the main thread
            mCursor.getCount();
        }
        return loadItems(mCursor);
    }

    @Override
    public void deliverResult(List<D> items) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (mCursor != null) {
                mCursor.close();
            }
            return;
        }

        mItems = items;
        if (isStarted()) {
            super.deliverResult(items);
        }

        // TODO: might not be necessary. The analogue of this was
        // to close the *old* cursor after assigning the new cursor.
        // This is closing the current cursor? But then again, we don't
        // care about the cursor after we've extracted the items from it..
        // Close the cursor because it is no longer needed.
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
    }

    @Override
    protected void onStartLoading() {
        if (mCursor != null && mItems != null) {
            // Deliver any previously loaded data immediately.
            deliverResult(mItems);
        }
        if (takeContentChanged() || mCursor == null || mItems == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(List<D> data) {
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();
        // Ensure the loader is stopped
        onStopLoading();
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;
        mItems = null;
    }
}
