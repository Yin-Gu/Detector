package com.example.administrator.demo.ui.condition_monitoring;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.demo.R;
import com.example.administrator.demo.model.condition_monitoring.DataMonitoringItem;
import com.example.administrator.demo.model.net.ResponseMessage;
import com.example.administrator.demo.net.RequestManager;
import com.example.administrator.demo.net.RequestSender;
import com.example.administrator.demo.net.ResponseParser;
import com.example.administrator.demo.ui.common.CommonTitleActivity;
import com.example.administrator.demo.ui.common.ItemClickListener;
import com.example.administrator.demo.ui.condition_monitoring.adapter.DataMonitoringAdapter;
import com.example.administrator.demo.utils.OkHttpUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DataMonitoringActivity extends CommonTitleActivity {

    private final static String TAG = "DataMonitoring";

    private List<DataMonitoringItem> dataMonitoringItemList = new ArrayList<>();
    private DataMonitoringAdapter dataMonitoringAdapter;

    private Unbinder unbinder;
    private volatile boolean endRequest;    //结束线程
    private String abnormalMotors;

    @BindView(R.id.rv_data_monitoring) RecyclerView dataMonitoring;
    @BindView(R.id.tv_abnormal_motors) TextView tv_abnormal_motors;
    @BindView(R.id.tv_alarm_prompt) TextView tv_alarm_prompt;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    dismissLoadingDialog();
                    dataMonitoringAdapter.notifyDataSetChanged();
                    updatePrompt();
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
        Log.d(TAG, "onCreate()");
        unbinder = ButterKnife.bind(this);
        endRequest = false;

        createDataMonitoring();
        showLoadingDialog("查询中");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        endRequest = false;
        sendRequest();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        endRequest = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        endRequest = true;
        unbinder.unbind();
    }

    @Override
    public void beforeFinish() {

    }

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
                int mid = dataMonitoringItemList.get(position).getMotorId();
                intent.putExtra("mid", mid);
                intent.putExtra("guid", 0);
                endRequest = true;
                startActivity(intent);
            }
        });
    }

    private void sendRequest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!endRequest) {
                    String url = RequestManager.dataMonitoring;
                    String content = "{\"guid\":\"0\",\"equipmentName\":\"推土机\"}";
                    String response = RequestSender.postRequest(url, content);
                    if (!endRequest) {
                        ResponseMessage message = null;
                        if (response != null) {
                            message = ResponseParser.parseResponse(response);
                        }
                        if (message != null) {
                            if (message.isSuccess()) {
                                parseData(message.getData());
                                Message msg = new Message();
                                msg.what = 1;
                                handler.sendMessage(msg);
                                try {
                                    Thread.sleep(RequestManager.REQUEST_FREQUENCE);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Log.d(TAG, "sendRequest(){ "
                                        + "errorCode: " + message.getErrorCode()
                                        + "errorString: " + message.getErrorString());
                            }
                        }
                    }
                }
            }
        }).start();
    }

    private void parseData(String dataJson) {
        if (dataJson != null && dataJson.isEmpty()) {
            return;
        }
        try {
            JSONObject data = new JSONObject(dataJson);
            String equipmentName = data.getString("equipmentName");
            int guid = data.getInt("guid");

            if (equipmentName.equals("推土机") && guid == 0) {
                JSONArray motors = data.getJSONArray("motor");
                StringBuilder builder = new StringBuilder();
                dataMonitoringItemList.clear();
                for (int i = 0; i < motors.length(); i++) {
                    JSONObject motor = motors.getJSONObject(i);
                    Integer mid = motor.getInt("mid");
                    String motorName = motor.getString("motorName");
                    String temperature = motor.getString("temperature");
                    String current = motor.getString("current");
                    String voltage = motor.getString("voltage");
                    Boolean status = false;
                    JSONArray statuses = motor.getJSONArray("status");
                    Log.d(TAG, statuses.toString());
                    for (int j = 0; j < statuses.length(); j++) {
                        if (statuses.getInt(j) == 1) {
                            status = true;
                        }
                    }
                    if (status) {
                        //设置异常提示文本
                        builder.append(mid).append("、");
                    }
                    DataMonitoringItem item = new DataMonitoringItem(
                            mid, motorName, status, temperature, voltage, current);
                    dataMonitoringItemList.add(item);
                }
                if (builder.length() != 0) {
                    builder.setLength(builder.length() - 1);
                    builder.append("号电机");
                    abnormalMotors = builder.toString();
                } else {
                    abnormalMotors = "";
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "parseData()");
        }
    }

    private void updatePrompt() {
        if (abnormalMotors.isEmpty()) {
            tv_abnormal_motors.setTextColor(Color.GRAY);
            tv_abnormal_motors.setText(getString(R.string.no_alarm));
        } else {
            String promptText = abnormalMotors
                    + getString(R.string.please_process);
            tv_abnormal_motors.setTextColor(Color.RED);
            tv_abnormal_motors.setText(promptText);
        }
    }

}
