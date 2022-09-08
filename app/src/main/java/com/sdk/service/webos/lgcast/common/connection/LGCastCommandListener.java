package com.sdk.service.webos.lgcast.common.connection;

import org.json.JSONObject;

public interface LGCastCommandListener {
    void onReceive(JSONObject response);
}