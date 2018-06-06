package com.example.administrator.demo.net;


import okhttp3.MediaType;

public class RequestManager {

    static final MediaType JSON = MediaType.parse("application/json");
    //数据刷新的时间间隔，单位ms
    public static final int REQUEST_RATE = 3000;

    private static final String baseUrl = "http://47.104.29.62:8080/DeviceDataServer";

    //单个测点数据接口
    public static final String singleMotorData = baseUrl + "/dataDisplay/singleMotorData";
    //数据监控接口
    public static final String dataMonitoring = baseUrl + "/dataDisplay/dataMonitoring";
    //时间段数据曲线接口
    public static final String getRangeData = baseUrl + "/dataDisplay/getRangeData";
    //测点范围获取接口
    public static final String getDataRange = baseUrl + "/dataDisplay/getDataRange";
    //设备所有部件类型名称获取接口
    public static final String getCmptNames = baseUrl + "/dataDisplay/getCmptNames";
    //获取选定部件的所有测点范围
    public static final String getCmptRange = baseUrl + "/dataDisplay/getCmptRange";
    //部件类型测点范围更新
    public static final String updateCmptRange = baseUrl + "/dataDisplay/updateCmptRange";
}

