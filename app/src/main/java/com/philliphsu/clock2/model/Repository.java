package com.philliphsu.clock2.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by Phillip Hsu on 5/31/2016.
 */
public interface Repository<T> {
    @NonNull List<T> getItems();
    @Nullable T getItem(long id);
    void addItem(@NonNull T item);
    void deleteItem(@NonNull T item);
    void updateItem(@NonNull T item1, @NonNull T item2);
    boolean saveItems();
    void clear();
}
