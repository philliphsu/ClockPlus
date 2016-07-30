package com.philliphsu.clock2.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.philliphsu.clock2.util.LocalBroadcastHelper;

/**
 * Created by Phillip Hsu on 7/29/2016.
 */
public abstract class BaseDatabaseHelper<T extends ObjectWithId> extends SQLiteOpenHelper {

    public static final String COLUMN_ID = "_id";

    private final Context mAppContext;

    /**
     * @param context the Context with which the application context will be retrieved
     * @param name the name of the database file. Because this is required by the SQLiteOpenHelper
     *             constructor, we can't, for instance, have an abstract getDatabaseFileName() that
     *             subclasses implement and the base class can call on their behalf.
     * @param version the version
     */
    public BaseDatabaseHelper(Context context, String name,
                              /*SQLiteDatabase.CursorFactory factory,*/
                              int version) {
        super(context.getApplicationContext(), name, null, version);
        mAppContext = context.getApplicationContext();
    }

    /**
     * @return the table managed by this helper
     */
    protected abstract String getTableName();

    /**
     * @return the ContentValues representing the item's properties.
     * You do not need to put a mapping for {@link #COLUMN_ID}, since
     * the database manages ids for us (if you created your table
     * with {@link #COLUMN_ID} as an {@code INTEGER PRIMARY KEY}).
     */
    protected abstract ContentValues toContentValues(T item);

    /**
     * @return optional String specifying the sort order
     * to use when querying the database. The default
     * implementation returns null, which may return
     * queries unordered.
     */
    protected String getQuerySortOrder() {
        return null;
    }

    public long insertItem(T item) {
        long id = getWritableDatabase().insert(
                getTableName(), null, toContentValues(item));
        item.setId(id);
        notifyContentChanged();
        return id;
    }

    public int updateItem(long id, T newItem) {
        newItem.setId(id);
        SQLiteDatabase db = getWritableDatabase();
        int rowsUpdated = db.update(getTableName(),
                toContentValues(newItem),
                COLUMN_ID + " = " + id,
                null);
        notifyContentChanged();
        return rowsUpdated;
    }

    public int deleteItem(T item) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsDeleted = db.delete(getTableName(),
                COLUMN_ID + " = " + item.getId(),
                null);
        notifyContentChanged();
        return rowsDeleted;
    }

    public Cursor queryItem(long id) {
        return queryItems(COLUMN_ID + " = " + id, "1");
    }

    public Cursor queryItems() {
        // Select all rows and columns
        return queryItems(null, null);
    }

    protected Cursor queryItems(String where, String limit) {
        return getReadableDatabase().query(getTableName(),
                null, // All columns
                where, // Selection, i.e. where COLUMN_* = [value we're looking for]
                null, // selection args, none b/c id already specified in selection
                null, // group by
                null, // having
                getQuerySortOrder(), // order/sort by
                limit); // limit
    }

    /**
     * Broadcasts to any registered receivers that the data backed
     * by this helper has changed, and so they should requery and
     * update themselves as necessary.
     */
    private void notifyContentChanged() {
        LocalBroadcastHelper.sendBroadcast(mAppContext,
                SQLiteCursorLoader.ACTION_CHANGE_CONTENT);
    }
}
