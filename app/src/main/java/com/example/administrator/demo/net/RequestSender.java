package com.example.administrator.demo.net;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.example.administrator.demo.conf.LoggingConfigure;
import com.example.administrator.demo.utils.OkHttpUtils;

import java.io.IOException;

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

    public static boolean checkNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo[] networkInfos = manager.getAllNetworkInfo();
            if (networkInfos != null) {
                for (NetworkInfo networkInfo : networkInfos) {
                    if (networkInfo.getState() ==
                            NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        Toast.makeText(context, "无网络，请检查网络设置", Toast.LENGTH_SHORT).show();

        return false;
    }

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
            if (LoggingConfigure.LOGGING) {
                Log.d(TAG, "postRequest()");
            }
        }

        return null;
    }
}
