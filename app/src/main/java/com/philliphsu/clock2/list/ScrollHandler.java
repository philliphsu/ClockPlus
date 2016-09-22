package com.philliphsu.clock2.list;

/**
 * Created by Phillip Hsu on 7/6/2016.
 */
public interface ScrollHandler {
    /**
     * Specifies the stable id of the item we should scroll to in the list.
     * This does not scroll the list. This is useful for preparing to scroll
     * to the item when it does not yet exist in the list.
     */
    void setScrollToStableId(long id);

    void scrollToPosition(int position);
}
