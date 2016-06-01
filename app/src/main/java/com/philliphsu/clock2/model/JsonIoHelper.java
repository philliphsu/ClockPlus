package com.philliphsu.clock2.model;

import android.content.Context;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Phillip Hsu on 5/31/2016.
 */
public abstract class JsonIoHelper<T extends JsonSerializable> {
    @NonNull private final Context mContext;
    @NonNull private final String mFilename;

    public JsonIoHelper(@NonNull Context context,
                        @NonNull String filename) {
        mContext = context.getApplicationContext();
        mFilename = filename;
    }

    protected abstract T newItem(@NonNull JSONObject jsonObject);

    public final List<T> loadItems() throws IOException {
        ArrayList<T> items = new ArrayList<>();
        BufferedReader reader = null;
        try {
            // Opens the file in a FileInputStream for byte-reading
            InputStream in = mContext.openFileInput(mFilename);
            // Use an InputStreamReader to convert bytes to characters. A BufferedReader wraps the
            // existing Reader and provides a buffer (a cache) for storing the characters.
            // From https://docs.oracle.com/javase/7/docs/api/java/io/BufferedReader.html:
            // "In general, each read request made of a Reader causes a corresponding read request
            // to be made of the underlying character or byte stream. It is therefore advisable to
            // wrap a BufferedReader around any Reader whose read() operations may be costly, such as
            // FileReaders and InputStreamReaders."
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                // Line breaks are omitted and irrelevant
                jsonString.append(line);
            }
            // JSONTokener parses a String in JSON "notation" into a "JSON-compatible" object.
            // JSON objects are instances of JSONObject and JSONArray. You actually have to call
            // nextValue() on the returned Tokener to get the corresponding JSON object.
            JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();
            for (int i = 0; i < array.length(); i++) {
                items.add(newItem(array.getJSONObject(i)));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } finally {
            if (reader != null)
                reader.close();
        }
        return items;
    }

    public final void saveItems(@NonNull List<T> items) throws IOException {
        // Convert items to JSONObjects and store in a JSONArray
        JSONArray array = new JSONArray();
        try {
            for (T item : items) {
                array.put(item.toJsonObject());
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        OutputStreamWriter writer = null;
        try {
            // Create a character stream from the byte stream
            writer = new OutputStreamWriter(mContext.openFileOutput(mFilename, Context.MODE_PRIVATE));
            // Write JSONArray to file
            writer.write(array.toString());
        } finally {
            if (writer != null) {
                writer.close(); // also calls close on the byte stream
            }
        }
    }
}
