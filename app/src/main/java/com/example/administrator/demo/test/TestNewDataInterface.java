package com.example.administrator.demo.test;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.administrator.demo.R;
import com.example.administrator.demo.model.net.ResponseMessage;
import com.example.administrator.demo.net.RequestManager;
import com.example.administrator.demo.net.RequestSender;
import com.example.administrator.demo.net.ResponseParser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2018/5/22 0022.
 */

public class TestNewDataInterface extends AppCompatActivity {

    private TextView tv_new_data_interface;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            tv_new_data_interface.setText((String)msg.obj);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_new_data_interface);
        tv_new_data_interface = findViewById(R.id.tv_new_data_interface);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = RequestManager.dataMonitoring;
                String content = "{\"guid\":\"0\",\"equipmentName\":\"推土机\"}";
                String response = RequestSender.postRequest(url, content);
                ResponseMessage message = null;
                if (response != null) {
                    message = ResponseParser.parseResponse(response);
                }
                if (message != null && message.isSuccess()) {
                    Message msg = Message.obtain();
                    msg.obj = message.getData();
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

}
