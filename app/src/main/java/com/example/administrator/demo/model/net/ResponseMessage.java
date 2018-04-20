package com.example.administrator.demo.model.net;

/**
 * Created by Administrator on 2018/4/20 0020.
 */

public class ResponseMessage {
    private boolean success;
    private String data;
    private String errorCode;
    private String errorString;

    public ResponseMessage(boolean status, String data, String errorCode, String errorString) {
        this.success = status;
        this.data = data;
        this.errorCode = errorCode;
        this.errorString = errorString;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getData() {
        return data;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorString() {
        return errorString;
    }

}
