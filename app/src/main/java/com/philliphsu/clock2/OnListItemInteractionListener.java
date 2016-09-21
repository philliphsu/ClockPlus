package com.philliphsu.clock2;

/**
 * Created by Phillip Hsu on 5/31/2016.
 */
public interface OnListItemInteractionListener<T> {
    void onListItemClick(T item, int position);
    void onListItemDeleted(T item);
    void onListItemUpdate(T item, int position);
}
