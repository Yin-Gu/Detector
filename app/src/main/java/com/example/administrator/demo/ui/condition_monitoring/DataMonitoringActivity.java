package com.example.administrator.demo.ui.condition_monitoring;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.example.administrator.demo.ui.common.CommonTitleActivity;
import com.example.administrator.demo.ui.common.ItemClickListener;
import com.example.administrator.demo.ui.condition_monitoring.adapter.DataMonitoringAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class DataMonitoringActivity extends CommonTitleActivity {

    private final static String TAG = "DataMonitoringActivity";
    public static final MediaType JSON = MediaType.parse("application/json");

    private List<DataMonitoringItem> dataMonitoringItemList = new ArrayList<>();
    private DataMonitoringAdapter dataMonitoringAdapter;

    private Unbinder unbinder;
    private volatile boolean endRequest;
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

        createDataMonitoring();
        showLoadingDialog("查询中");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
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
        endRequest = true;
        Log.d(TAG, "onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        endRequest = true;
    }

    public void createDataMonitoring() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        dataMonitoring.setLayoutManager(linearLayoutManager);
        dataMonitoringAdapter = new DataMonitoringAdapter(dataMonitoringItemList);
        dataMonitoring.setAdapter(dataMonitoringAdapter);

        dataMonitoringAdapter.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                endRequest = true;
                Intent intent = new Intent(DataMonitoringActivity.this, MonitoringDetailActivity.class);
                int mid = dataMonitoringItemList.get(position).getMotorId();
                intent.putExtra("mid", mid);
                intent.putExtra("guid", 0);
                startActivity(intent);
            }
        });
    }

    @Override
    public void beforeFinish() {

    }

    @Override
    public void setTitle() {
        titleBar.setText(getString(R.string.data_monitoring));
    }

    private void sendRequest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!endRequest) {
                    try {
                        RequestBody requestBody =
                                RequestBody.create(JSON, "{\"guid\":\"0\",\"equipmentName\":\"推土机\"}");
                        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                        okhttp3.Request request = new okhttp3.Request.Builder()
                                .url("http://47.104.29.62:8080/DeviceDataServer/dataDisplay/dataMonitoring")
                                .post(requestBody)
                                .build();
                        okhttp3.Response response = client.newCall(request).execute();
                        String responseData = response.body().string();
                        if (!endRequest) {
                            parseJSON(responseData);
                            Message message = new Message();
                            message.what = 1;
                            handler.sendMessage(message);
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void parseJSON(String jsonData) {
        if (jsonData == null || jsonData.length() == 0) {
            return;
        }
        try {
            Log.d(TAG, jsonData);
            JSONObject jsonObject = new JSONObject(jsonData);
            boolean success = jsonObject.getBoolean("status");
            if (success) {
                String dataString = jsonObject.getString("data");
                JSONObject data = new JSONObject(dataString);
                String equipmentName = data.getString("equipmentName");
                int guid = data.getInt("guid");
                if (equipmentName.equals("推土机") && guid == 0) {
                    JSONArray motors = data.getJSONArray("motor");
                    dataMonitoringItemList.clear();
                    StringBuilder builder = new StringBuilder();
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
                            builder.append(mid + "、");
                        }
                        if (builder.length() != 0) {
                            abnormalMotors = builder.substring(0, builder.length() - 1);
                            abnormalMotors += "号电机";
                        } else {
                            abnormalMotors = "";
                        }
                        DataMonitoringItem item = new DataMonitoringItem(
                                mid, motorName, status, temperature, voltage, current);
                        dataMonitoringItemList.add(item);
                    }
                }
                Log.d(TAG, dataMonitoringItemList.toString());
            } else {
                int errorCode = jsonObject.getInt("errorCode");
                String errorString = jsonObject.getString("errorString");
                Log.d(TAG, "parseJSON(){ " +
                        "errorCode: " + errorCode + "errorString: " + errorString);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updatePrompt() {
        if (abnormalMotors.isEmpty()) {
            tv_abnormal_motors.setText("无异常");
        } else {
            String promptText = abnormalMotors
                    + getString(R.string.please_process);
            tv_abnormal_motors.setText(promptText);
        }
    }

}
