package com.example.administrator.demo.ui.condition_monitoring;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.administrator.demo.R;
import com.example.administrator.demo.model.condition_monitoring.DataMonitoringItem;
import com.example.administrator.demo.model.net.ResponseMessage;
import com.example.administrator.demo.net.RequestManager;
import com.example.administrator.demo.net.RequestSender;
import com.example.administrator.demo.net.ResponseParser;
import com.example.administrator.demo.ui.common.CommonTitleActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class MonitoringDetailActivity extends CommonTitleActivity {

    private static final String TAG = "MonitoringDetail";
    private static final int UPDATE_VALUE = 0;
    private static final int UPDATE_CHART = 1;

    private int guid;
    private int mid;
    private volatile boolean endRequest;
    private volatile String spinnerItem;
    private String abnormalText;
    private Thread chartThread;
    private String[] spinnerList;

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
                    updateSingleMotor((DataMonitoringItem) msg.obj);
                    updatePrompt();
                    break;
                case UPDATE_CHART:
                    dismissLoadingDialog();
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
        showLoadingDialog("查询中");
        requestSingleMotor();
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

    @Override
    public void beforeFinish(){}

    @Override
    public void setTitle(){ titleBar.setText(getText(R.string.monitoring_detail));}

    private void initMotorId() {
        Intent intent = getIntent();
        guid = intent.getIntExtra("guid", -1);
        mid = intent.getIntExtra("mid", -1);
        if (guid == -1 || mid == -1) {
            endRequest = true;
        }
    }

    private void initSpinner() {
        spinnerList = getResources().getStringArray(R.array.measuring_point);
        spinnerItem = spinnerList[0];

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                spinnerItem = spinnerList[position];
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

    private void requestSingleMotor() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!endRequest) {
                    String url = RequestManager.singleMotorData;
                    String content = "{\"guid\":\"" + guid + "\",\"mid\":\"" + mid + "\"}";
                    String response = RequestSender.postRequest(url, content);
                    if (!endRequest) {
                        ResponseMessage message = null;
                        if (response != null) {
                            message = ResponseParser.parseResponse(response);
                        }
                        if (message != null) {
                            if (message.isSuccess()) {
                                DataMonitoringItem item =
                                        parseSingleMotor(message.getData());
                                if (item != null) {
                                    Message msg = new Message();
                                    msg.what = UPDATE_VALUE;
                                    msg.obj = item;
                                    handler.sendMessage(msg);
                                    try {
                                        Thread.sleep(RequestManager.REQUEST_FREQUENCE);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                Log.d(TAG, "requestSingleMotor(){ "
                                        + "errorCode: " + message.getErrorCode()
                                        + "errorString: " + message.getErrorString());
                            }
                        }
                    }
                }
            }
        }).start();
    }

    private DataMonitoringItem parseSingleMotor(String dataJson) {
        if (dataJson != null && dataJson.isEmpty()) {
            return null;
        }
        try {
            JSONObject data = new JSONObject(dataJson);
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
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "parseSingleMotor()");
        }

        return null;
    }

    private void requestChart() {
        chartThread = new Thread(new Runnable() {
            @Override
            public void run() {
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                while (!endRequest) {
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
                    String url = RequestManager.rangeData;
                    String response = RequestSender.postRequest(url, content);
                    if (!endRequest) {
                        ResponseMessage message = null;
                        if (response != null) {
                            message = ResponseParser.parseResponse(response);
                        }
                        if (message != null) {
                            if (message.isSuccess()) {
                                ChartPoint[] points = parseChart(message.getData());
                                Message msg = new Message();
                                msg.what = UPDATE_CHART;
                                msg.obj = points;
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
        });
        chartThread.start();
    }

    private ChartPoint[] parseChart(String dataJson) {
        if (dataJson != null && dataJson.isEmpty()) {
            return null;
        }
        try {
            JSONArray data = new JSONArray(dataJson);
            int length = data.length();
            Log.d(TAG, "data.length = " + length);
            ChartPoint[] points = new ChartPoint[length];
            for (int i = 0; i < length; i++) {
                JSONObject item = data.getJSONObject(i);
                ChartPoint point = new ChartPoint();
                point.time = formatTime(item.getString("time"));
                point.value = item.getInt("value");
                points[i] = point;
            }

            return filterTime(points);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "parseChart()");
        }

        return null;
    }

    private ChartPoint[] filterTime(ChartPoint[] points) {
        if (points.length <= 100) {
            return points;
        }
        ArrayList<ChartPoint> list = new ArrayList<>(20);
        list.add(points[0]);
        //取分钟数
        int minuteIndex = points[0].time.indexOf(":");
        String currentMinute = points[0].time
                .substring(minuteIndex + 1, minuteIndex + 3);
        if (currentMinute.charAt(0) == '0') {
            currentMinute = currentMinute.substring(1, 2);
        }
        int lastMinute = Integer.valueOf(currentMinute) - 1;
        lastMinute = lastMinute < 0 ? lastMinute + 60 : lastMinute;
        for (int i = 1; i < points.length; i++) {
            currentMinute = points[i].time
                    .substring(minuteIndex + 1, minuteIndex + 3);
            String last = String.valueOf(lastMinute);
            if (lastMinute < 10) {
                last = "0" + last;
            }
            if (last.equals(currentMinute)) {
                Log.d(TAG, points[i].toString());
                list.add(points[i]);
                lastMinute--;
                lastMinute = lastMinute < 0 ? lastMinute + 60 : lastMinute;
            }
        }

        //获取到的数据反序，需要调换位置
        ChartPoint[] results = new ChartPoint[list.size()];
        for (int i = 0; i < results.length; i++) {
            results[results.length - 1 - i] = list.get(i);
        }
        return results;
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
    }

    private void updateSingleMotor(DataMonitoringItem item) {
        tv_motor_name.setText(item.getEquipmentName());
        tv_single_temperature.setText(item.getTemperature());
        tv_single_current.setText(item.getElectricCurrent());
        tv_single_voltage.setText(item.getVoltage());
    }

    private void updatePrompt() {
        if (abnormalText.isEmpty()) {
            tv_alarm_tint.setText(getString(R.string.no_alarm));
            tv_alarm_tint.setTextColor(Color.GRAY);
        } else {
            String alarmHint = getResources().getString(R.string.alarm_hint);
            int index = alarmHint.indexOf(" ");
            //替换空格部分为异常项
            String promptText = alarmHint.substring(0, index);
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
                0f, maxValue + 1.0f, (points.length - 1) * 1.5f + 0.5f, minValue - 1.0f);
        chartView.setMaximumViewport(maxViewPort);
        Viewport currentViewPort = new Viewport(
                0f, maxValue + 1.0f, 9.0f, minValue - 1.0f);
        chartView.setCurrentViewport(currentViewPort);
    }

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
