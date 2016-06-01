package com.philliphsu.clock2.model;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Phillip Hsu on 5/31/2016.
 */
public interface JsonSerializable {
    String KEY_ID = "id";

    @NonNull JSONObject toJsonObject() throws JSONException;
    long id();
}
