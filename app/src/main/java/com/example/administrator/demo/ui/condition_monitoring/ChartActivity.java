package com.example.administrator.demo.ui.condition_monitoring;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.example.administrator.demo.R;
import com.example.administrator.demo.conf.ChartConfigure;
import com.example.administrator.demo.conf.LoggingConfigure;
import com.example.administrator.demo.model.condition_monitoring.CurveData;
import com.example.administrator.demo.model.net.ResponseMessage;
import com.example.administrator.demo.net.RequestManager;
import com.example.administrator.demo.net.RequestSender;
import com.example.administrator.demo.net.ResponseParser;
import com.example.administrator.demo.ui.condition_monitoring.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

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

/**
 * Created by Administrator on 2018/5/30 0030.
 */

public class ChartActivity extends AppCompatActivity {

    private static final String TAG = "ChartActivity";
    private static final int UPDATE_CHART = 0;

    private List<Entry> entries;    //点集
    private List<String> labels;    //X轴标签
    private List<Integer> colors;   //点的颜色

    private int guid;
    private int mid;
    private String motorName;
    private String spinnerItem;
    private float up;
    private float down;

    private Unbinder unbinder;

    private volatile boolean endRequest;

    @BindView(R.id.line_chart_activity) LineChart lineChart;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_CHART:
                    updateLineChart((CurveData[]) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_line_chart);

        unbinder = ButterKnife.bind(this);
        initLineChart();
    }


    @Override
    protected void onResume() {
        super.onResume();

        endRequest = false;
        initRequestParams();
        requestLineChart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        endRequest = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbinder.unbind();
        endRequest = true;
    }

    @Override
    public void finish() {
        super.finish();
    }


    private void initLineChart() {
        entries = new ArrayList<>();
        labels = new ArrayList<>();
        colors = new ArrayList<>();

        //是否隐藏图表描述
        lineChart.getDescription().setEnabled(false);
        //默认Y轴有两个，左右各一个
        //设置右轴不显示
        lineChart.getAxisRight().setEnabled(false);
        lineChart.setScaleXEnabled(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(Color.LTGRAY);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(ChartConfigure.AXIS_TEXT_SIZE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new XAxisValueFormatter(labels));

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setDrawGridLines(true);
        yAxis.setGridColor(Color.LTGRAY);
        yAxis.setTextColor(Color.BLACK);
        yAxis.setTextSize(ChartConfigure.AXIS_TEXT_SIZE);
    }

    private void initRequestParams() {
        Intent intent = getIntent();
        guid = intent.getIntExtra("guid", -1);
        mid = intent.getIntExtra("mid", -1);
        motorName = intent.getStringExtra("motorName");
        spinnerItem = intent.getStringExtra("spinnerItem");
        up = intent.getFloatExtra("up", Float.MIN_VALUE);
        down = intent.getFloatExtra("down", Float.MIN_VALUE);

        if (guid == -1 || mid == -1 || motorName == null || spinnerItem == null) {
            endRequest = true;
        }
    }

    private void requestLineChart() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

                String url = RequestManager.getRangeData;
                while (!endRequest) {
                    Date date = new Date();
                    String end = format.format(date);
                    date.setTime(date.getTime() - 1000 * 60 * ChartConfigure.POINT_MINUTE);  //2分钟
                    String start = format.format(date);

                    String content = "{\"guid\":" + guid
                            + ",\"mid\":" + mid
                            + ",\"motorName\":\"" + motorName + "\""
                            + ",\"category\":\"" + spinnerItem + "\""
                            + ",\"start\":\"" + start + "\""
                            + ",\"end\":\"" + end + "\""
                            + "}";
                    String response = RequestSender.postRequest(url, content);
                    if (!endRequest) {
                        ResponseMessage message = null;
                        if (response != null) {
                            message = ResponseParser.parseResponse(response);
                        }
                        if (message != null && message.isSuccess()) {
                            CurveData[] points = parseChart(message.getData());
                            Message msg = Message.obtain();
                            msg.what = UPDATE_CHART;
                            msg.obj = points;
                            handler.sendMessage(msg);
                        } else if (message != null) {
                            if (LoggingConfigure.LOGGING) {
                                Log.d(TAG, "requestLineChart(){ "
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

    private CurveData[] parseChart(String dataJson) {
        if (dataJson != null && dataJson.isEmpty())
            return null;

        try {
            JSONArray data = new JSONArray(dataJson);
            int length = data.length();
            CurveData[] points = new CurveData[length];
            for (int i = 0; i < length; i++) {
                JSONObject item = data.getJSONObject(i);
                CurveData point = new CurveData();
                point.time = formatTime(item.getString("time"));
                point.value = item.getDouble("value");
                points[i] = point;
            }

            return invertArray(points);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private CurveData[] invertArray(CurveData[] points) {
        //获取到的数据反序，需要调换位置
        CurveData[] results = new CurveData[points.length];
        for (int i = 0; i < results.length; i++) {
            results[results.length - 1 - i] = points[i];
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

    private void updateLineChart(CurveData[] points) {
        if (LoggingConfigure.LOGGING) {
            for (CurveData point : points) {
                Log.d(TAG, point.time + " " + point.value);
            }
        }

        entries.clear();
        labels.clear();
        colors.clear();
        
        for (int i = 0; i < points.length; i++) {
            float value = (float) points[i].value;
            entries.add(new Entry(i * ChartConfigure.DATA_GAP, value));
            labels.add(points[i].time);

            if (Math.abs(down - Float.MIN_VALUE) < 1E-10) {
                if (value > up) {
                    colors.add(Color.parseColor("#FF0000"));
                } else {
                    colors.add(Color.parseColor("#32CD32"));
                }
            } else {
                if (value > up || value < down) {
                    colors.add(Color.parseColor("#FF0000"));
                } else {
                    colors.add(Color.parseColor("#32CD32"));
                }
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, spinnerItem);
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        LineData lineData = new LineData(dataSet);
        lineData.setValueTextSize(ChartConfigure.POINT_TEXT_SIZE);
        lineData.setValueTextColors(colors);

        lineChart.setData(lineData);
        lineChart.animateX(1000);

        float maxXRange = 10;
        float maxXValue = points.length * ChartConfigure.DATA_GAP + 1.0f;
        lineChart.setVisibleXRangeMaximum(maxXRange);
        lineChart.getXAxis().setAxisMinimum(-0.5f);
        lineChart.getXAxis().setAxisMaximum(maxXValue);
    }

}
