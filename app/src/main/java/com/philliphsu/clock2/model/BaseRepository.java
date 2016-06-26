package com.philliphsu.clock2.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Phillip Hsu on 5/31/2016.
 */
@Deprecated
public abstract class BaseRepository<T extends JsonSerializable> implements Repository<T> {
    private static final String TAG = "BaseRepository";
    // Cannot do this! Multiple classes will extend from this,
    // so this "singleton" would be a class property for all of them.
    //private static BaseRepositoryV2<T> sInstance;

    // Never used since subclasses provide the ioHelper, but I think
    // the intention is that we hold onto the global context so this
    // never gets GCed for the lifetime of the app.
    @NonNull private final Context mContext;
    @NonNull private final List<T> mItems; // TODO: Consider ArrayMap<Long, T>?
    @NonNull private final JsonIoHelper<T> mIoHelper;

    // TODO: Test that the callbacks work.
    private DataObserver<T> mDataObserver;

    // We could use T but since it's already defined, we should avoid
    // the needless confusion and use a different type param. You won't
    // be able to refer to the type that T resolves to anyway.
    public interface DataObserver<T2> {
        void onItemAdded(T2 item);
        void onItemDeleted(T2 item);
        void onItemUpdated(T2 oldItem, T2 newItem);
    }

    /*package-private*/ BaseRepository(@NonNull Context context,
                   @NonNull JsonIoHelper<T> ioHelper) {
        Log.d(TAG, "BaseRepositoryV2 initialized");
        mContext = context.getApplicationContext();
        mIoHelper = ioHelper; // MUST precede loading items
        mItems = loadItems(); // TOneverDO: move this elsewhere
    }

    @Override @NonNull
    public List<T> getItems() {
        return Collections.unmodifiableList(mItems);
    }

    @Nullable
    @Override
    public T getItem(long id) {
        for (T item : getItems())
            if (item.id() == id)
                return item;
        return null;
    }

    @Override
    public final void addItem(@NonNull T item) {
        Log.d(TAG, "New item added");
        mItems.add(item);
        mDataObserver.onItemAdded(item); // TODO: Set StopwatchView as DataObserver
        saveItems();
    }

    @Override
    public final void deleteItem(@NonNull T item) {
        if (!mItems.remove(item)) {
            Log.e(TAG, "Cannot remove an item that is not in the list");
        } else {
            mDataObserver.onItemDeleted(item); // TODO: Set StopwatchView as DataObserver
            saveItems();
        }
    }

    @Override
    public final void updateItem(@NonNull T item1, @NonNull T item2) {
        // TODO: Won't work unless objects are immutable, so item1
        // can't change and thus its index will never change
        // **
        // Actually, since the items come from this list,
        // modifications to items will directly "propagate".
        // In the process, the index of that modified item
        // has not changed. If that's the case, there really
        // isn't any point for an update method, especially
        // since item2 would be unnecessary and won't even need
        // to be used.
        mItems.set(mItems.indexOf(item1), item2);
        mDataObserver.onItemUpdated(item1, item2); // TODO: Set StopwatchView as DataObserver
        saveItems();
    }

    @Override
    public final boolean saveItems() {
        try {
            mIoHelper.saveItems(mItems);
            Log.d(TAG, "Saved items to file");
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error writing items to file: " + e);
            return false;
        }
    }

    @Override
    public final void clear() {
        mItems.clear();
        saveItems();
    }

    public final void registerDataObserver(@NonNull DataObserver<T> observer) {
        mDataObserver = observer;
    }

    // TODO: Do we need to call this?
    public final void unregisterDataObserver() {
        mDataObserver = null;
    }

    @NonNull
    private List<T> loadItems() {
        try {
            return mIoHelper.loadItems();
        } catch (IOException e) {
            Log.e(TAG, "Error loading items from file: " + e);
            return new ArrayList<>();
        }
    }
}
