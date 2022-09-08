package com.sdk.core;

import org.json.JSONException;
import org.json.JSONObject;

public interface JSONSerializable {
    public JSONObject toJSONObject() throws JSONException;
}
