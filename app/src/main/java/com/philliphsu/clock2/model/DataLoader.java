package com.philliphsu.clock2.model;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Created by Phillip Hsu on 6/30/2016.
 */
// TODO: Consider adding a DatabaseTableManager type param, so we can then
// implement loadInBackground for subclasses. You would, however, need to write
// an abstract method getTableManager() that subclasses implement for us.
public abstract class DataLoader<D> extends AsyncTaskLoader<D> {

    private D mData;

    public DataLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        } else {
            forceLoad();
        }
    }

    @Override
    public void deliverResult(D data) {
        mData = data;
        if (isStarted()) {
            super.deliverResult(data);
        }
    }

}
