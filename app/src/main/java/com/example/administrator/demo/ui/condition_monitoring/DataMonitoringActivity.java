package com.example.administrator.demo.ui.condition_monitoring;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.example.administrator.demo.R;
import com.example.administrator.demo.model.condition_monitoring.DataMonitoringItem;
import com.example.administrator.demo.model.net.ResponseMessage;
import com.example.administrator.demo.net.RequestManager;
import com.example.administrator.demo.net.RequestSender;
import com.example.administrator.demo.net.ResponseParser;
import com.example.administrator.demo.ui.common.CommonTitleActivity;
import com.example.administrator.demo.ui.common.ItemClickListener;
import com.example.administrator.demo.ui.condition_monitoring.adapter.DataMonitoringAdapter;
import com.example.administrator.demo.conf.LoggingConfigure;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class DataMonitoringActivity extends CommonTitleActivity {

    private final static String TAG = "DataMonitoring";
    private final static int UPDATE_DATA = 0;

    private List<DataMonitoringItem> dataMonitoringItemList = new ArrayList<>();
    private DataMonitoringAdapter dataMonitoringAdapter;

    private Unbinder unbinder;
    private volatile boolean endRequest;    //结束线程

    @BindView(R.id.rv_data_monitoring) RecyclerView dataMonitoring;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_DATA:
                    dataMonitoringAdapter.notifyDataSetChanged();
                    dismissLoadingDialog();
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_monitoring);

        unbinder = ButterKnife.bind(this);
        createDataMonitoring();
    }

    @Override
    protected void onResume() {
        super.onResume();

        endRequest = false;
        if (RequestSender.checkNetworkAvailable(this)) {
            showLoadingDialog("查询中");
            sendRequest();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        endRequest = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        endRequest = true;
        unbinder.unbind();
    }

    @Override
    public void beforeFinish() {}

    @Override
    public void setTitle() {
        titleBar.setText(getString(R.string.data_monitoring));
    }

    public void createDataMonitoring() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        dataMonitoring.setLayoutManager(linearLayoutManager);
        dataMonitoringAdapter = new DataMonitoringAdapter(dataMonitoringItemList);
        dataMonitoring.setAdapter(dataMonitoringAdapter);

        dataMonitoringAdapter.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent(DataMonitoringActivity.this, MonitoringDetailActivity.class);
                DataMonitoringItem item = dataMonitoringItemList.get(position);
                intent.putExtra("guid", 0);
                intent.putExtra("mid", item.getMotorId());
                intent.putExtra("attrs", item.getAttrs());
                intent.putExtra("motorName", item.getMotorName());
                endRequest = true;
                startActivity(intent);
            }
        });
    }

    private void sendRequest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = RequestManager.dataMonitoring;
                String content = "{\"guid\":\"0\",\"equipmentName\":\"推土机\"}";

                while (!endRequest) {
                    String response = RequestSender.postRequest(url, content);
                    if (!endRequest) {
                        ResponseMessage message = null;
                        if (response != null) {
                            message = ResponseParser.parseResponse(response);
                        }
                        if (message != null && message.isSuccess()) {
                            if (LoggingConfigure.LOGGING) {
                                Log.d(TAG, message.getData());
                            }

                            parseData(message.getData());
                            Message msg = Message.obtain();
                            msg.what = UPDATE_DATA;
                            handler.sendMessage(msg);
                        } else if (message != null) {
                            if (LoggingConfigure.LOGGING) {
                                Log.d(TAG, "sendRequest(){ "
                                        + "errorCode: " + message.getErrorCode()
                                        + "errorString: " + message.getErrorString()
                                        + "}");
                            }
                        }
                    }

                    try {
                        Thread.sleep(RequestManager.REQUEST_RATE);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void parseData(String dataJson) {
        if (dataJson == null || dataJson.isEmpty())
            return;

        try {
            JSONObject data = new JSONObject(dataJson);
            String equipmentName = data.getString("equipmentName");
            int guid = data.getInt("guid");

            if (!equipmentName.equals("推土机") || guid != 0)
                return;

            JSONArray motors = data.getJSONArray("motor");
            dataMonitoringItemList.clear();
            for (int i = 0; i < motors.length(); i++) {
                JSONObject motor = motors.getJSONObject(i);
                Integer mid = motor.getInt("mid");
                String motorName = motor.getString("motorName");
                JSONArray statuses = motor.getJSONArray("status");
                JSONArray attrs = motor.getJSONArray("attrs");
                JSONArray values = motor.getJSONArray("values");

                String[] attrsArray = new String[attrs.length()];
                String[] valuesArray = new String[values.length()];
                Boolean status = false;
                for (int j = 0; j < attrs.length(); j++) {
                    attrsArray[j] = attrs.getString(j);
                    valuesArray[j] = values.getString(j);
                }
                for (int j = 0; j < statuses.length(); j++) {
                    if (statuses.getInt(j) == 1) {
                        status = true;
                        break;
                    }
                }

                DataMonitoringItem item = new DataMonitoringItem(
                        mid, motorName, status, attrsArray, valuesArray);
                dataMonitoringItemList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
