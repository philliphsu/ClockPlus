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

package com.philliphsu.clock2.data;

import android.content.Context;
import android.os.AsyncTask;

import com.philliphsu.clock2.list.ScrollHandler;

/**
 * Created by Phillip Hsu on 7/1/2016.
 */
public abstract class AsyncDatabaseTableUpdateHandler<
        T extends ObjectWithId,
        TM extends DatabaseTableManager<T>> {
    private static final String TAG = "AsyncDatabaseTableUpdateHandler";

    private final Context mAppContext;
    private final ScrollHandler mScrollHandler;
    private final TM mTableManager;

    /**
     * @param context the Context from which we get the application context
     */
    public AsyncDatabaseTableUpdateHandler(Context context, ScrollHandler scrollHandler) {
        mAppContext = context.getApplicationContext(); // to prevent memory leaks
        mScrollHandler = scrollHandler;
        mTableManager = onCreateTableManager(context);
    }

    public final void asyncInsert(final T item) {
        new InsertAsyncTask(item).execute();
    }

    public final void asyncUpdate(final long id, final T newItem) {
        new UpdateAsyncTask(id, newItem).execute();
    }

    public final void asyncDelete(final T item) {
        // TODO: If we want to scroll somewhere after we delete this item, we can
        // create a DeleteAsyncTask subclass of the BaseAsyncTask. This involves
        // using a Long result, however.
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                return mTableManager.deleteItem(item);
            }

            @Override
            protected void onPostExecute(Integer integer) {
                onPostAsyncDelete(integer, item);
            }
        }.execute();
    }

    public final void asyncClear() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                mTableManager.clear();
                return null;
            }
        }.execute();
    }

    public final TM getTableManager() {
        return mTableManager;
    }

    protected final Context getContext() {
        return mAppContext;
    }

    protected abstract TM onCreateTableManager(Context context);

    protected abstract void onPostAsyncDelete(Integer result, T item);

    protected abstract void onPostAsyncInsert(Long result, T item);

    protected abstract void onPostAsyncUpdate(Long result, T item);

    ////////////////////////////////////////////////////////////
    // Insert and update AsyncTasks
    ////////////////////////////////////////////////////////////

    /**
     * Created because the code in insert and update AsyncTasks are exactly the same.
     */
    private abstract class BaseAsyncTask extends AsyncTask<Void, Void, Long> {
        final T mItem;

        BaseAsyncTask(T item) {
            mItem = item;
        }

        @Override
        protected void onPostExecute(Long result) {
            if (mScrollHandler != null) {
                // Prepare to scroll to this alarm
                mScrollHandler.setScrollToStableId(result);
            }
        }
    }

    private class InsertAsyncTask extends BaseAsyncTask {
        InsertAsyncTask(T item) {
            super(item);
        }

        @Override
        protected Long doInBackground(Void... params) {
            return mTableManager.insertItem(mItem);
        }

        @Override
        protected void onPostExecute(Long result) {
            super.onPostExecute(result);
            onPostAsyncInsert(result, mItem);
        }
    }

    private class UpdateAsyncTask extends BaseAsyncTask {
        private final long mId;

        UpdateAsyncTask(long id, T item) {
            super(item);
            mId = id;
        }

        @Override
        protected Long doInBackground(Void... params) {
            mTableManager.updateItem(mId, mItem);
            return mId;
        }

        @Override
        protected void onPostExecute(Long result) {
            super.onPostExecute(result);
            onPostAsyncUpdate(result, mItem);
        }
    }
}
