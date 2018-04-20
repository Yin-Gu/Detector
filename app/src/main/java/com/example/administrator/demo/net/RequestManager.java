package com.example.administrator.demo.net;

import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;

public class RequestManager {

    private static final String baseUrl = "http://47.104.29.62:8080/DeviceDataServer";

    public static final String dataMonitoring = baseUrl + "/dataDisplay/dataMonitoring";
    public static final String singleMotorData = baseUrl + "/dataDisplay/singleMotorData";
    public static final String rangeData = baseUrl + "/dataDisplay/getRangeData";

    static final MediaType JSON = MediaType.parse("application/json");
    public static final int REQUEST_FREQUENCE = 5000 ; //刷新数据的时间间隔
}

