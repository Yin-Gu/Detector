package com.example.administrator.demo.ui.condition_monitoring;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.example.administrator.demo.R;
import com.example.administrator.demo.model.condition_monitoring.DataMonitoringItem;
import com.example.administrator.demo.ui.common.CommonTitleActivity;
import com.example.administrator.demo.ui.common.ItemClickListener;
import com.example.administrator.demo.ui.condition_monitoring.adapter.DataMonitoringAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    private volatile boolean endRequest;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    dismissLoadingDialog();
                    dataMonitoringAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

    private Unbinder unbinder;

    @BindView(R.id.rv_data_monitoring)
    RecyclerView dataMonitoring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_monitoring);
        unbinder = ButterKnife.bind(this);
        endRequest = false;

        createDataMonitoring();
        showLoadingDialog("查询中");
        sendRequest();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

//    public void initDataMonitoring() {
//        DataMonitoringItem first = new DataMonitoringItem("1号电机", true, "73℃", "724V", "548A");
//        DataMonitoringItem second = new DataMonitoringItem("2号电机", true, "80℃", "722V", "522A");
//        DataMonitoringItem third = new DataMonitoringItem("3号电机", true, "80℃", "722V", "522A");
//
//        dataMonitoringItemList.add(first);
//        dataMonitoringItemList.add(second);
//        dataMonitoringItemList.add(third);
//
//        Message message = new Message();
//        handler.sendMessage(message);
//    }

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

//        private void parseJSON(String jsonData) {
//            try{
//                JSONObject jsonObject = new JSONObject(removeBOM(jsonData));
//                JSONObject data = jsonObject.getJSONObject("data");
//                JSONArray itemList = data.getJSONArray("motor");
//                for(int i=0;i<itemList.length();i++){
//                    DataMonitoringItem item = new DataMonitoringItem();
//                    item.setEquipmentName(itemList.getJSONObject(i).getString("motorName"));
//                    item.setStatus(true);
//                    item.setTemperature(itemList.getJSONObject(i).getString("temperature"));
//                    item.setElectricCurrent(itemList.getJSONObject(i).getString("current"));
//                    item.setVoltage(itemList.getJSONObject(i).getString("voltage"));
//                    dataMonitoringItemList.add(item);
//                }
//                dataMonitoringAdapter.setDatas(dataMonitoringItemList);
//
//            }catch (JSONException e){
//                e.printStackTrace();
//            }
//
//        }
//
//    public static final String removeBOM(String data) {
//        if (TextUtils.isEmpty(data)) {
//            return data;
//        }
//        if (data.startsWith("\ufeff")) {
//            return data.substring(1);
//        } else {
//            return data;
//        }
//    }


}
