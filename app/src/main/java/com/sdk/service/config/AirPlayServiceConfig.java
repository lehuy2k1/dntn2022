package com.sdk.service.config;

import org.json.JSONException;
import org.json.JSONObject;

public class AirPlayServiceConfig extends ServiceConfig {
    public static final String KEY_AUTH_TOKEN = "authToken";
    String authToken;

    public AirPlayServiceConfig(JSONObject json) {
        super(json);

        authToken = json.optString(KEY_AUTH_TOKEN);
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
        notifyUpdate();
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject jsonObj = super.toJSONObject();

        try {
            jsonObj.put(KEY_AUTH_TOKEN, authToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObj;
    }

}
