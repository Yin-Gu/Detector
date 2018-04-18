package com.example.administrator.demo.ui.condition_monitoring;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.administrator.demo.R;
import com.example.administrator.demo.model.condition_monitoring.DataMonitoringItem;
import com.example.administrator.demo.ui.common.CommonTitleActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lecho.lib.hellocharts.formatter.AxisValueFormatter;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class MonitoringDetailActivity extends CommonTitleActivity {

    private static final String TAG = "MonitorDetailActivity";
    public static final MediaType JSON = MediaType.parse("application/json");
    private static final int UPDATE_VALUE = 0;
    private static final int UPDATE_CHART = 1;

    private int guid;
    private int mid;
    private volatile boolean endRequest;
    private volatile String spinnerItem;
    private Thread chartThread;

    private List<Line> lines;   //线集，若需要显示多条线则添加多个Line
    private List<PointValue> pointValues;   //图像点集
    private List<AxisValue> axisXValues;   //图像x轴值集
    private AxisValueFormatter formatter;

    @BindView(R.id.chart) LineChartView chartView;
    @BindView(R.id.point_spinner) Spinner spinner;
    @BindView(R.id.tv_motor_name) TextView tv_motor_name;
    @BindView(R.id.tv_single_temperature) TextView tv_single_temperature;
    @BindView(R.id.tv_single_current) TextView tv_single_current;
    @BindView(R.id.tv_single_voltage) TextView tv_single_voltage;

    private Unbinder unbinder;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_VALUE:
                    updateValue((DataMonitoringItem) msg.obj);
                    break;
                case UPDATE_CHART:
                    updateChart((ChartPoint[]) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring_detail);
        unbinder = ButterKnife.bind(this);
        endRequest = false;

        initMotorId();
        requestValue();
        initSpinner();
        initChartView();
        requestChart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        endRequest = true;
    }

    private void initMotorId() {
        Intent intent = getIntent();
        guid = intent.getIntExtra("guid", -1);
        mid = intent.getIntExtra("mid", -1);
    }

    private void initSpinner() {
        String[] arr = getResources().getStringArray(R.array.measuring_point);
        spinnerItem = arr[0];

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String[] arr = getResources().getStringArray(R.array.measuring_point);
                spinnerItem = arr[position];
                chartThread.interrupt();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void initChartView() {
        chartView.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
        chartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);

        lines = new ArrayList<>();
        pointValues = new ArrayList<>();
        axisXValues = new ArrayList<>();
    }

    private void requestValue() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "requestValue start()");
                if (guid == -1 || mid == -1) {
                    return;
                }
                while (!endRequest) {
                    try {
                        String content = "{\"guid\":\"" + guid + "\",\"mid\":\"" + mid + "\"}";
                        RequestBody requestBody =
                                RequestBody.create(JSON, content);
                        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                        okhttp3.Request request = new okhttp3.Request.Builder()
                                .url("http://47.104.29.62:8080/DeviceDataServer/dataDisplay/singleMotorData")
                                .post(requestBody)
                                .build();
                        okhttp3.Response response = client.newCall(request).execute();
                        String responseData = response.body().string();
                        if (!endRequest) {
                            DataMonitoringItem item = parseSingleMotor(responseData);
                            if (item != null) {
                                Message message = new Message();
                                message.what = UPDATE_VALUE;
                                message.obj = item;
                                handler.sendMessage(message);
                            }
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

    private DataMonitoringItem parseSingleMotor(String jsonData) {
        if (jsonData == null || jsonData.length() == 0) {
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            boolean success = jsonObject.getBoolean("status");
            if (success) {
                String dataString = jsonObject.getString("data");
                JSONObject data = new JSONObject(dataString);
                int mid_ = data.getInt("mid");
                if (mid_ == mid) {
                    String motorName = data.getString("motorName");
                    String temperature = data.getString("temperature");
                    String current = data.getString("current");
                    String voltage = data.getString("voltage");
                    Boolean status = false;
                    JSONArray statuses = data.getJSONArray("status");
                    for (int j = 0; j < statuses.length(); j++) {
                        if (statuses.getInt(j) == 1) {
                            status = true;
                        }
                    }
                    return new DataMonitoringItem(
                            mid_, motorName, status, temperature, voltage, current);
                }
            } else {
                int errorCode = jsonObject.getInt("errorCode");
                String errorString = jsonObject.getString("errorString");
                Log.d(TAG, "parseSingleMotor(){ " +
                        "errorCode: " + errorCode + "errorString: " + errorString);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void requestChart() {
        chartThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (guid == -1 || mid == -1) {
                    return;
                }
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                while (!endRequest) {
                    try {
                        Date date = new Date();
                        String end = format.format(date);
                        date.setTime(date.getTime() - 60000);
                        String start = format.format(date);

                        String content = "{\"guid\":" + guid
                                + ",\"mid\":" + mid
                                + ",\"category\":\"" + spinnerItem + "\""
                                + ",\"start\":\"" + start + "\""
                                + ",\"end\":\"" + end + "\""
                                +"}";
                        RequestBody requestBody =
                                RequestBody.create(JSON, content);
                        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                        okhttp3.Request request = new okhttp3.Request.Builder()
                                .url("http://47.104.29.62:8080/DeviceDataServer/dataDisplay/getRangeData")
                                .post(requestBody)
                                .build();
                        okhttp3.Response response = client.newCall(request).execute();
                        String responseData = response.body().string();
                        Log.d(TAG, responseData);

                        if (!endRequest) {
                            ChartPoint[] points = parseChart(responseData);
                            Message message = new Message();
                            message.what = UPDATE_CHART;
                            message.obj = points;
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
        });
        chartThread.start();
    }

    private ChartPoint[] parseChart(String jsonData) {
        if (jsonData == null || jsonData.length() == 0) {
            return null;
        }
        try {
            Log.d(TAG, jsonData);
            JSONObject jsonObject = new JSONObject(jsonData);
            boolean success = jsonObject.getBoolean("status");
            if (success) {
                String dataString = jsonObject.getString("data");
                JSONArray data = new JSONArray(dataString);
                int length = data.length();
                ChartPoint[] points = new ChartPoint[length];
                for (int i = 0; i < length; i++) {
                    JSONObject item = data.getJSONObject(i);
                    ChartPoint point = new ChartPoint();
                    point.time = formatTime(item.getString("time"));
                    point.value = item.getInt("value");
                    points[points.length - 1 - i] = point;
                }
                return points;
            } else {
                int errorCode = jsonObject.getInt("errorCode");
                String errorString = jsonObject.getString("errorString");
                Log.d(TAG, "parseChart(){ " +
                        "errorCode: " + errorCode + ",errorString: " + errorString);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String formatTime(String time) {
        StringBuilder builder = new StringBuilder();
        char[] timeArr = time.toCharArray();
        for (char c : timeArr) {
            switch (c) {
                case '年':
                case '月':
                case '日':
                    break;
                default:
                    builder.append((c));
                    break;
            }
        }
        return builder.toString();

//        for (int i = 0; i < time.length(); i++) {
//            if (time.charAt(i) == ' ') {
//                return time.substring(i + 1,time.length());
//            }
//        }
//        return "";
    }

    private void updateValue(DataMonitoringItem item) {
        if (item == null) {
            return;
        }
        tv_motor_name.setText(item.getEquipmentName());
        tv_single_temperature.setText(item.getTemperature());
        tv_single_current.setText(item.getElectricCurrent());
        tv_single_voltage.setText(item.getVoltage());
    }

    private void updateChart(ChartPoint[] points) {
        if (points == null) {
            return;
        }
        lines.clear();
        pointValues.clear();
        axisXValues.clear();
        LineChartData data = new LineChartData();

        int maxValue = Integer.MIN_VALUE;
        int minValue = Integer.MAX_VALUE;
        //设置点
        for (int i = 0; i < points.length; i++) {
            int value = points[i].value;
            maxValue = value > maxValue ? value : maxValue;
            minValue = value < minValue ? value : minValue;
            pointValues.add(new PointValue(i * 2, value));
        }
        //设置线
        Line line = new Line(pointValues);
        line.setColor(Color.BLUE)
                .setCubic(false)
                .setHasLabels(true) //设置点上显示值
                .setPointColor(Color.BLACK)
                .setPointRadius(2)  //设置点的大小
                .setStrokeWidth(2); //设置线的粗细
        lines.add(line);
        data.setLines(lines);

        //设置x轴的标签
        for (int i = 0; i < points.length; i++) {
            axisXValues.add(new AxisValue(i).setLabel(points[i].time));
        }
        //设置x轴
        Axis x = new Axis();
        x.setValues(axisXValues)
                .setHasTiltedLabels(true)
                .setTextSize(10)
                .setHasLines(true)
                .setLineColor(Color.LTGRAY)
                .setTextColor(Color.BLACK)
                .setName("采集时间");
        data.setAxisXBottom(x);

        //设置y轴
        Axis y = new Axis();
        y.setTextColor(Color.BLACK)
                .setHasLines(true)
                .setLineColor(Color.LTGRAY);
        data.setAxisYLeft(y);

        chartView.setLineChartData(data);

        Viewport maxViewPort = new Viewport(0, maxValue + 3.0f, points.length, minValue - 3.0f);
        chartView.setMaximumViewport(maxViewPort);
        Viewport currentViewPort = new Viewport(0, maxValue * 3.0f, 6, minValue - 3.0f);
        chartView.setCurrentViewport(currentViewPort);
    }

    @Override
    public void beforeFinish(){}

    @Override
    public void setTitle(){ titleBar.setText(getText(R.string.monitoring_detail));}


    static class ChartPoint {
        String time;
        int value;
    }
}
