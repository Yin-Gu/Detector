package com.example.administrator.demo.net;

import android.util.Log;

import com.example.administrator.demo.model.net.ResponseMessage;
import com.example.administrator.demo.conf.LoggingConfigure;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2018/4/20 0020.
 */

public class ResponseParser {

    public static final String TAG = "ResponseParser";

    public static ResponseMessage parseResponse(String json) {
        if (json.isEmpty()) {
            return null;
        }
        ResponseMessage message = null;
        try {
            JSONObject jsonObject  = new JSONObject(json);
            boolean status = jsonObject.getBoolean("status");
            String data = jsonObject.getString("data");
            String errorCode = jsonObject.getString("errorCode");
            String errorString = jsonObject.getString("errorString");
            message = new ResponseMessage(status, data, errorCode, errorString);
        } catch (JSONException e) {
            e.printStackTrace();
            if (LoggingConfigure.LOGGING) {
                Log.d(TAG, "parseResponse()");
            }
        }

        return message;
    }
}