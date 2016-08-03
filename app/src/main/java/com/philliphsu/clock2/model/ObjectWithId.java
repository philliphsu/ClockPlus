package com.philliphsu.clock2.model;

/**
 * Created by Phillip Hsu on 7/29/2016.
 *
 * Superclass for objects that can be persisted in SQLite.
 */
public abstract class ObjectWithId {
    private long id;

    public final long getId() {
        return id;
    }

    public final void setId(long id) {
        this.id = id;
    }

    public final int getIntId() {
        return (int) id;
    }
}
