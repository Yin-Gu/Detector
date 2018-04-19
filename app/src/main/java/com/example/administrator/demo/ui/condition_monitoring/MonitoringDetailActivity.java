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
import android.widget.Toast;

import com.example.administrator.demo.R;
import com.example.administrator.demo.model.condition_monitoring.DataMonitoringItem;
import com.example.administrator.demo.ui.common.CommonTitleActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
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

    private static final String TAG = "MonitoringDetail";
    public static final MediaType JSON = MediaType.parse("application/json");
    private final int UPDATE_VALUE = 0;
    private final int UPDATE_CHART = 1;
    private final int REFRASH_FREQUENCE = 5000 ; //刷新数据的时间间隔

    private int guid;
    private int mid;
    private volatile boolean endRequest;
    private volatile String spinnerItem;
    private String abnormalText;
    private Thread chartThread;

    private List<Line> lines;   //线集，若需要显示多条线则添加多个Line
    private List<PointValue> pointValues;   //图像点集
    private List<AxisValue> axisXValues;   //图像x轴值集

    @BindView(R.id.chart) LineChartView chartView;
    @BindView(R.id.point_spinner) Spinner spinner;
    @BindView(R.id.tv_motor_name) TextView tv_motor_name;
    @BindView(R.id.tv_single_temperature) TextView tv_single_temperature;
    @BindView(R.id.tv_single_current) TextView tv_single_current;
    @BindView(R.id.tv_single_voltage) TextView tv_single_voltage;
    @BindView(R.id.tv_alarm_tint) TextView tv_alarm_tint;

    private Unbinder unbinder;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_VALUE:
                    updateValue((DataMonitoringItem) msg.obj);
                    updatePrompt();
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
        unbinder.unbind();
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
                                Thread.sleep(REFRASH_FREQUENCE);
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
                    StringBuilder builder = new StringBuilder();
                    JSONArray statuses = data.getJSONArray("status");
                    if (statuses.getInt(0) == 1) {
                        status = true;
                        builder.append("温度、");
                    }
                    if (statuses.getInt(1) == 1) {
                        status = true;
                        builder.append("电流、");
                    }
                    if (statuses.getInt(2) == 1) {
                        status = true;
                        builder.append("电压、");
                    }
                    if (builder.length() != 0) {
                        abnormalText = builder.substring(0, builder.length() - 1);
                    } else {
                        abnormalText = "";
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
                        date.setTime(date.getTime() - 1000 * 60 * 15);  //15分钟
                        String start = format.format(date);

                        String content = "{\"guid\":" + guid
                                + ",\"mid\":" + mid
                                + ",\"category\":\"" + spinnerItem + "\""
                                + ",\"start\":\"" + start + "\""
                                + ",\"end\":\"" + end + "\""
                                + "}";
                        RequestBody requestBody = RequestBody.create(JSON, content);
                        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                        okhttp3.Request request = new okhttp3.Request.Builder()
                                .url("http://47.104.29.62:8080/DeviceDataServer/dataDisplay/getRangeData")
                                .post(requestBody)
                                .build();
                        okhttp3.Response response = client.newCall(request).execute();
                        String responseData = response.body().string();

                        if (!endRequest) {
                            ChartPoint[] points = parseChart(responseData);
                            Message message = new Message();
                            message.what = UPDATE_CHART;
                            message.obj = points;
                            handler.sendMessage(message);
                            try {
                                Thread.sleep(REFRASH_FREQUENCE);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException e) {
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
                Log.d(TAG, "data.length = " + length);
                ChartPoint[] points = new ChartPoint[length];
                for (int i = 0; i < length; i++) {
                    JSONObject item = data.getJSONObject(i);
                    ChartPoint point = new ChartPoint();
                    point.time = formatTime(item.getString("time"));
                    point.value = item.getInt("value");
                    points[points.length - 1 - i] = point;
                }
                return filterTime(points);
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

    private ChartPoint[] filterTime(ChartPoint[] points) {
        if (points.length <= 5 * 20) {
            return points;
        }
        ArrayList<ChartPoint> list = new ArrayList<>(20);
        list.add(points[0]);
        //取分钟数
        int index = points[0].time.indexOf(":");
        String currentMinute = points[0].time.substring(index + 1, index + 3);
        if (currentMinute.charAt(0) == '0') {
            currentMinute = currentMinute.substring(1, 2);
        }
        Log.d(TAG, "time: " + points[0].time);
        Log.d(TAG, "currentMinute: " + currentMinute);
        int nextMinute = Integer.valueOf(currentMinute) + 1;
        nextMinute = nextMinute >= 60 ? nextMinute - 60 : nextMinute;
        for (int i = 1; i < points.length; i++) {
            currentMinute = points[i].time.substring(index + 1, index + 3);
            String next = String.valueOf(nextMinute);
            if (nextMinute < 10) {
                next = "0" + next;
            }
            if (next.equals(currentMinute)) {
                Log.d(TAG, points[i].toString());
                list.add(points[i]);
                nextMinute++;
                nextMinute = nextMinute >= 60 ? nextMinute - 60 : nextMinute;
            }
        }

        return list.toArray(new ChartPoint[list.size()]);
    }

    private String formatTime(String time) {
        StringBuilder builder = new StringBuilder();
        char[] timeArr = time.toCharArray();
        for (char c : timeArr) {
            switch (c) {
                case '年':
                case '月':
                    builder.append('.');
                    break;
                case '日':
                    break;
                case ' ':
                    builder.append('/');
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

    private void updatePrompt() {
        if (abnormalText.isEmpty()) {
            tv_alarm_tint.setText("无异常");
            tv_alarm_tint.setTextColor(Color.GRAY);
        } else {
            String alarmHint = getResources().getString(R.string.alarm_hint);
            int index = alarmHint.indexOf(" ");
            String promptText = alarmHint.substring(0, index);  //替换空格部分为异常项
            promptText += abnormalText;
            promptText += alarmHint.substring(index + 1, alarmHint.length());
            tv_alarm_tint.setText(promptText);
            tv_alarm_tint.setTextColor(Color.RED);
        }
    }

    private void updateChart(ChartPoint[] points) {
        if (points == null) {
            return;
        }

        Log.d(TAG, "updateChart:[" + points.length + "]");
        for (int i = 0; i < points.length; i++) {
            Log.d(TAG, "points[ " + i + "]:" + points[i].toString());
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
            pointValues.add(new PointValue(i * 1.5f, value));
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
            int index = points[i].time.indexOf("/");
            String label = points[i].time
                    .substring(index + 1, points[i].time.length());
            axisXValues.add(new AxisValue(i * 1.5f).setLabel(label));
        }
        //设置x轴
        Axis x = new Axis();
        x.setValues(axisXValues)
                .setTextSize(12)
                .setTextColor(Color.BLACK)
                .setHasLines(true)
                .setLineColor(Color.LTGRAY)
                .setName("采集时间");
        data.setAxisXBottom(x);

        //设置y轴
        Axis y = new Axis();
        y.setTextColor(Color.BLACK)
                .setHasLines(true)
                .setLineColor(Color.LTGRAY);
        data.setAxisYLeft(y);

        chartView.setLineChartData(data);

        Viewport maxViewPort = new Viewport(
                0f, maxValue + 1.0f, (points.length) * 1.5f, minValue - 1.0f);
        chartView.setMaximumViewport(maxViewPort);
        Viewport currentViewPort = new Viewport(
                0f, maxValue + 1.0f, 9.0f, minValue - 1.0f);
        chartView.setCurrentViewport(currentViewPort);
    }

    @Override
    public void beforeFinish(){}

    @Override
    public void setTitle(){ titleBar.setText(getText(R.string.monitoring_detail));}


    static class ChartPoint {
        String time;
        int value;

        @Override
        public String toString() {
            return "ChartPoint{" +
                    "time='" + time + '\'' +
                    ", value=" + value +
                    '}';
        }
    }
}
