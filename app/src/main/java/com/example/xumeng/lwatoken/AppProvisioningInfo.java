package com.example.xumeng.lwatoken;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by xumeng on 17/1/4.
 */
public class AppProvisioningInfo {
    private final String accessToken;

    public AppProvisioningInfo(String accessToken){
        this.accessToken=accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public JSONObject toJson() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(AuthConstants.ACCESS_TOKEN, accessToken);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
