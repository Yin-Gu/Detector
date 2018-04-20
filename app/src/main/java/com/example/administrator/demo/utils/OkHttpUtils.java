package com.example.administrator.demo.utils;

import okhttp3.OkHttpClient;

/**
 * Created by Administrator on 2018/4/20 0020.
 */

public class OkHttpUtils {

    private volatile static OkHttpClient INSTANCE;

    private OkHttpUtils() {}

    public static OkHttpClient newClient() {
        if (INSTANCE == null) {
            synchronized (OkHttpUtils.class) {
                if (INSTANCE == null) {
                    INSTANCE = new OkHttpClient();
                }
            }
        }
        return INSTANCE;
    }
}
