package com.philliphsu.clock2;

import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Phillip Hsu on 5/31/2016.
 */
public abstract class BaseAdapter<T, VH extends BaseViewHolder<T>> extends RecyclerView.Adapter<VH> {

    private final OnListItemInteractionListener<T> mListener;
    private final SortedList<T> mItems;

    protected BaseAdapter(Class<T> cls, List<T> items, OnListItemInteractionListener<T> listener) {
        mItems = new SortedList<>(cls, new SortedListAdapterCallback<T>(this) {
            @Override
            public int compare(T o1, T o2) {
                //return BaseAdapter.this.<T, VH>*compare(o1, o2); // *: See note below
                return BaseAdapter.this.compare(o1, o2);
                // Only need to specify the type when calling a _generic_ method
                // that defines _its own type parameters_ in its signature, but even
                // then, it's not actually necessary because the compiler can
                // infer the type.
                // This is just an _abstract_ method that takes params of type T.
            }

            @Override
            public boolean areContentsTheSame(T oldItem, T newItem) {
                return BaseAdapter.this.areContentsTheSame(oldItem, newItem);
            }

            @Override
            public boolean areItemsTheSame(T item1, T item2) {
                return BaseAdapter.this.areItemsTheSame(item1, item2);
            }
        });
        mListener = listener;
        mItems.addAll(items);
    }

    protected abstract VH onCreateViewHolder(ViewGroup parent, OnListItemInteractionListener<T> listener);

    protected abstract int compare(T o1, T o2);

    protected abstract boolean areContentsTheSame(T oldItem, T newItem);

    protected abstract boolean areItemsTheSame(T item1, T item2);

    @Override // not final to allow subclasses to use the viewType if needed
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return onCreateViewHolder(parent, mListener);
    }

    @Override
    public final void onBindViewHolder(VH holder, int position) {
        holder.onBind(mItems.get(position));
    }

    @Override
    public final int getItemCount() {
        return mItems.size();
    }

    public final void replaceData(List<T> items) {
        mItems.clear();
        mItems.addAll(items);
    }

    protected final T getItem(int position) {
        return mItems.get(position);
    }

    public final int addItem(T item) {
        return mItems.add(item);
    }

    public final boolean removeItem(T item) {
        return mItems.remove(item);
    }

    public final void updateItem(T oldItem, T newItem) {
        // SortedList finds the index of an item by using its callback's compare() method.
        // We can describe our current item update process is as follows:
        // * An item's fields are modified
        // * The changes are saved to the repository
        // * A item update callback is fired to the RV
        // * The RV calls its adapter's updateItem(), passing the instance of the modified item as both arguments
        //   (because modifying an item keeps the same instance of the item)
        // * The SortedList tries to find the index of the param oldItem, but since its fields are changed,
        //   the search may end up failing because compare() could return the wrong index.
        // A workaround is to copy construct the original item instance BEFORE you modify the fields.
        // Then, oldItem should point to the copied instance.
        // Alternatively, a better approach is to make items immutable.
        mItems.updateItemAt(mItems.indexOf(oldItem), newItem);
    }
}
