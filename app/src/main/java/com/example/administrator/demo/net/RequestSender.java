package com.example.administrator.demo.net;


import android.util.Log;

import com.example.administrator.demo.utils.OkHttpUtils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.administrator.demo.net.RequestManager.JSON;

/**
 * Created by Administrator on 2018/4/20 0020.
 */

public class RequestSender {

    private static final String TAG = "RequestSender";

    public static String postRequest(String url, String content) {
        RequestBody body = RequestBody.create(JSON, content);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        OkHttpClient client = OkHttpUtils.newClient();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() == 200) {
                return response.body().string();
            }
        } catch (IOException e) {
            Log.d(TAG, "postRequest()");
        }

        return null;
    }
}
