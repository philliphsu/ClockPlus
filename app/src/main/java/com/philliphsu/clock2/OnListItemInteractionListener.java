package com.philliphsu.clock2;

/**
 * Created by Phillip Hsu on 5/31/2016.
 * This interface MUST be extended by Fragments that display a RecyclerView as a list.
 * The reason for this is Fragments need to do an instanceof check on their host Context
 * to see if it implements this interface, and instanceof cannot be used with generic type
 * parameters. Why not just define this interface as a member of the Fragment class?
 * Because the Fragment's BaseAdapter needs a reference to this interface, and we don't want
 * to couple the BaseAdapter
 * to the Fragment. By keeping this interface as generic as possible, the BaseAdapter can
 * be easily adapted to not just Fragments, but also custom Views, Activities, etc.
 */
public interface OnListItemInteractionListener<T> {
    void onListItemClick(T item, int position);
    void onListItemDeleted(T item);
    void onListItemUpdate(T item, int position);
}
