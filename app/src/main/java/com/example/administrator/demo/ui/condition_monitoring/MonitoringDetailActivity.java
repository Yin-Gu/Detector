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
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.demo.R;
import com.example.administrator.demo.conf.ChartConfigure;
import com.example.administrator.demo.model.condition_monitoring.CurveData;
import com.example.administrator.demo.model.condition_monitoring.DataMonitoringItem;
import com.example.administrator.demo.model.net.ResponseMessage;
import com.example.administrator.demo.net.RequestManager;
import com.example.administrator.demo.net.RequestSender;
import com.example.administrator.demo.net.ResponseParser;
import com.example.administrator.demo.ui.condition_monitoring.formatter.XAxisValueFormatter;
import com.example.administrator.demo.ui.common.CommonTitleActivity;
import com.example.administrator.demo.conf.LoggingConfigure;
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
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MonitoringDetailActivity extends CommonTitleActivity implements View.OnLongClickListener{

    private static final String TAG = "MonitoringDetail";
    private static final int UPDATE_VALUE = 0;
    private static final int UPDATE_CHART = 1;
    private static final int GET_MOTOR_BOUND = 2;
    private static final int NO_NEWEST_DATA = 3;

    private int guid;
    private int mid;
    private String motorName;
    private String[] attrs;
    private String[] downs;
    private String[] ups;
    private String[] units;
    private boolean[] status;
    private TextView[] testPoints;
    private TextView[] warns;

    private volatile String spinnerItem;
    private Thread chartThread;
    private List<String> spinnerList;

    private volatile boolean endRequest;
    private volatile boolean hasRange;
    private volatile int counter;

    private List<Entry> entries;    //点集
    private List<String> labels;    //X轴标签
    private List<Integer> colors;    //数据的颜色


    @BindView(R.id.line_chart) LineChart lineChart;
    @BindView(R.id.point_spinner) Spinner spinner;
    @BindView(R.id.tv_motor_name_detail) TextView tv_motor_name_detail;
    @BindView(R.id.tv_item_1_detail) TextView tv_item_1_detail;
    @BindView(R.id.tv_item_2_detail) TextView tv_item_2_detail;
    @BindView(R.id.tv_item_3_detail) TextView tv_item_3_detail;
    @BindView(R.id.tv_item_4_detail) TextView tv_item_4_detail;
    @BindView(R.id.tv_item_5_detail) TextView tv_item_5_detail;
    @BindView(R.id.tv_item_6_detail) TextView tv_item_6_detail;
    @BindView(R.id.tv_item_7_detail) TextView tv_item_7_detail;
    @BindView(R.id.tv_item_8_detail) TextView tv_item_8_detail;
    @BindView(R.id.tv_item_1_warn) TextView tv_item_1_warn;
    @BindView(R.id.tv_item_2_warn) TextView tv_item_2_warn;
    @BindView(R.id.tv_item_3_warn) TextView tv_item_3_warn;
    @BindView(R.id.tv_item_4_warn) TextView tv_item_4_warn;
    @BindView(R.id.tv_item_5_warn) TextView tv_item_5_warn;
    @BindView(R.id.tv_item_6_warn) TextView tv_item_6_warn;
    @BindView(R.id.tv_item_7_warn) TextView tv_item_7_warn;
    @BindView(R.id.tv_item_8_warn) TextView tv_item_8_warn;
    @BindView(R.id.tv_warn_text) TextView tv_warn_text;
    @BindView(R.id.tv_no_data_warn) TextView tv_no_data_warn;

    private Unbinder unbinder;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_MOTOR_BOUND:
                    setRangeArray((String) msg.obj);
                    hasRange = true;
                    break;
                case UPDATE_VALUE:
                    updateSingleMotor((DataMonitoringItem) msg.obj);
                    updatePrompt();
                    counter++;
                    break;
                case UPDATE_CHART:
                    updateLineChart((CurveData[]) msg.obj);
                    counter++;
                    break;
                case NO_NEWEST_DATA:
                    updateNoDataPrompt();
                    break;
                default:
                    break;
            }

            if (counter == 2) {
                dismissLoadingDialog();
                Toast.makeText(MonitoringDetailActivity.this,
                        "可以长按放大图表哟!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring_detail);

        unbinder = ButterKnife.bind(this);
        initUIArray();
        initSpinner();
        initLineChart();
    }


    @Override
    protected void onResume() {
        super.onResume();

        hasRange = false;
        endRequest = false;
        counter = 0;
        getIntentExtraData();
        if (RequestSender.checkNetworkAvailable(this)) {
            showLoadingDialog("查询中");
            requestCmptRange();
            requestSingleMotor();
            requestLineChart();
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
    public boolean onLongClick(View v) {
        if (v == lineChart) {
            float up = Float.MIN_VALUE, down = Float.MIN_VALUE;
            for (int i = 0; i < attrs.length; i++) {
                if (attrs[i].equals(spinnerItem)) {
                    up = Float.valueOf(ups[i]);
                    if (downs[i] != null && !downs[i].equals("null")) {
                        down = Float.valueOf(downs[i]);
                    }
                }
            }

            Intent intent = new Intent(this, ChartActivity.class);
            intent.putExtra("guid", guid);
            intent.putExtra("mid", mid);
            intent.putExtra("motorName", motorName);
            intent.putExtra("spinnerItem", spinnerItem);
            intent.putExtra("down", down);
            intent.putExtra("up", up);
            startActivity(intent);
        }
        return true;
    }

    @Override
    public void beforeFinish(){}

    @Override
    public void setTitle(){ titleBar.setText(getText(R.string.monitoring_detail));}

    private void initUIArray() {
        testPoints = new TextView[]{tv_item_1_detail, tv_item_2_detail,
                tv_item_3_detail, tv_item_4_detail, tv_item_5_detail,
                tv_item_6_detail, tv_item_7_detail, tv_item_8_detail};
        warns = new TextView[] {tv_item_1_warn, tv_item_2_warn,
                tv_item_3_warn, tv_item_4_warn, tv_item_5_warn,
                tv_item_6_warn, tv_item_7_warn, tv_item_8_warn};
    }

    private void getIntentExtraData() {
        Intent intent = getIntent();
        guid = intent.getIntExtra("guid", -1);
        mid = intent.getIntExtra("mid", -1);
        motorName = intent.getStringExtra("motorName");
        if (guid == -1 || mid == -1 || motorName == null) {
            endRequest = true;
        }
    }

    private void initSpinner() {
        spinnerList = new ArrayList<>(8);
        LinkedList<String> temp = new LinkedList<>();
        String[] attrs = getIntent().getStringArrayExtra("attrs");
        for (String item: attrs) {
            if (item.length() < 10) {
                spinnerList.add(item);
            } else {
                temp.add(item);
            }
        }
        spinnerList.addAll(temp);

        if (spinnerList.size() != 0) {
            spinnerItem = spinnerList.get(0);
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerList);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (spinnerList.size() != 0) {
                    spinnerItem = spinnerList.get(position);
                }
                chartThread.interrupt();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void requestCmptRange() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = RequestManager.getCmptRange;
                String content = "{\"cmptName\":\""  + motorName + "\"}";
                String response = RequestSender.postRequest(url, content);
                ResponseMessage message = null;
                if (response != null) {
                    message = ResponseParser.parseResponse(response);
                }
                if (message != null && message.isSuccess()) {
                    Message msg = Message.obtain();
                    msg.obj = message.getData();
                    msg.what = GET_MOTOR_BOUND;
                    handler.sendMessage(msg);
                } else if (message != null) {
                    if (LoggingConfigure.LOGGING) {
                        Log.d(TAG, "requestCmptRange() { "
                                + "errorCode: " + message.getErrorCode()
                                + "errorString: " + message.getErrorString()
                                + "}");
                    }
                }
            }
        }).start();
    }

    private void setRangeArray(String dataJson) {
        if (dataJson == null || dataJson.isEmpty())
            return;

        try {
            JSONObject data = new JSONObject(dataJson);
            JSONArray attrArray = data.getJSONArray("attrs");
            JSONArray downArray = data.getJSONArray("down");
            JSONArray upArray = data.getJSONArray("up");
            JSONArray unitArray = data.getJSONArray("unit");

            int length = attrArray.length();
            attrs = new String[length];
            downs = new String[length];
            ups = new String[length];
            units = new String[length];
            for (int i = 0; i < length; i++) {
                attrs[i] = attrArray.getString(i);
                downs[i] = downArray.getString(i);
                ups[i] = upArray.getString(i);
                units[i] = unitArray.getString(i);
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    private void requestSingleMotor() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!hasRange) ;

                String url = RequestManager.singleMotorData;
                String content = "{\"guid\":" + guid
                        + ",\"mid\":" + mid
                        + ",\"motorName\":\"" + motorName +"\""
                        + "}";
                while (!endRequest) {
                    String response = RequestSender.postRequest(url, content);
                    if (!endRequest) {
                        ResponseMessage message = null;
                        if (response != null) {
                            message = ResponseParser.parseResponse(response);
                        }
                        if (message != null && message.isSuccess()) {
                            DataMonitoringItem item = parseSingleMotor(message.getData());
                            if (item != null) {
                                Message msg = Message.obtain();
                                msg.what = UPDATE_VALUE;
                                msg.obj = item;
                                handler.sendMessage(msg);
                            }
                        } else if (message != null) {
                            if (LoggingConfigure.LOGGING) {
                                Log.d(TAG, "requestSingleMotor() { "
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

    private DataMonitoringItem parseSingleMotor(String dataJson) {
        if (dataJson == null || dataJson.isEmpty())
            return null;

        try {
            JSONObject data = new JSONObject(dataJson);
            int mid_ = data.getInt("mid");
            if (mid_ == mid) {
                String motorName = data.getString("motorName");
                JSONArray attrs = data.getJSONArray("attrs");
                JSONArray values = data.getJSONArray("values");
                JSONArray statuses = data.getJSONArray("status");

                String[] attrsArray = new String[attrs.length()];
                String[] valuesArray = new String[values.length()];
                status = new boolean[statuses.length()];

                for (int i = 0; i < this.attrs.length; i++) {
                    attrsArray[i] = this.attrs[i];

                    for (int j = 0; j < attrs.length(); j++) {
                        if (attrs.getString(j).equals(this.attrs[i])) {
                            status[i] = statuses.getInt(j) == 1;
                            valuesArray[i] = values.getString(j);
                            break;
                        }
                    }
                }

                return new DataMonitoringItem(mid_,
                        motorName, false, attrsArray, valuesArray);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void requestLineChart() {
        chartThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!hasRange) ;

                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

                String url = RequestManager.getRangeData;
                while (!endRequest) {
                    Date date = new Date();
                    String end = format.format(date);
                    date.setTime(date.getTime() - 1000 * 60 * ChartConfigure.POINT_MINUTE); //2分钟
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
                            Message msg = Message.obtain();
                            msg.what = NO_NEWEST_DATA;
                            handler.sendMessage(msg);
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
        });
        chartThread.start();
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
        String[] timeArray = time.split(" ");
        if (timeArray.length != 2) {
            return "00:00:00";
        } else {
            return timeArray[1];
        }
    }

    private void updateSingleMotor(DataMonitoringItem item) {
        if (item == null) {
            Toast.makeText(this, "no data!", Toast.LENGTH_SHORT).show();
            return;
        }

        tv_motor_name_detail.setText(item.getMotorName());

        int shortStart = 0, longStart = 6;
        int shortEnd = 6, longEnd = 8;
        String[] attrs = item.getAttrs();
        String[] values = item.getValues();

        for (int i = 0; i < attrs.length; i++) {
            String text = attrs[i] + ": " + values[i];
            if (attrs[i].length() < 10) {
                testPoints[shortStart].setVisibility(View.VISIBLE);
                testPoints[shortStart++].setText(text);
            } else {
                testPoints[longStart].setVisibility(View.VISIBLE);
                testPoints[longStart++].setText(text);
            }
        }

        while (shortStart < shortEnd) {
            testPoints[shortStart++].setVisibility(View.GONE);
        }
        while (longStart < longEnd) {
            testPoints[longStart++].setVisibility(View.GONE);
        }
    }

    private void updatePrompt() {
        int length = attrs.length;
        int shortStart = 0, longStart = 6;
        int shortEnd = 6, longEnd = 8;
        for (int i = 0; i < length; i++) {
            if (!status[i]) continue;
            tv_warn_text.setVisibility(View.VISIBLE);

            String text;
            if (downs[i] == null || downs[i].equals("null")) {
                text = attrs[i] + "异常 (/"  + " - " + ups[i] + units[i] + ")";
            } else {
                text = attrs[i] + "异常 (" + downs[i] + " - " + ups[i] + units[i] + ")";
            }

            if (attrs[i].length() < 10) {
                warns[shortStart].setVisibility(View.VISIBLE);
                warns[shortStart++].setText(text);
            } else {
                warns[longStart].setVisibility(View.VISIBLE);
                warns[longStart++].setText(text);
            }
        }

        while (shortStart < shortEnd) {
            warns[shortStart++].setVisibility(View.GONE);
        }
        while (longStart < longEnd) {
            warns[longStart++].setVisibility(View.GONE);
        }
    }

    private void updateNoDataPrompt() {
        dismissLoadingDialog();
        String text = "最近" + ChartConfigure.POINT_MINUTE + "分钟无数据!";
        tv_no_data_warn.setText(text);
        tv_no_data_warn.setVisibility(View.VISIBLE);
    }

    private void initLineChart() {
        entries = new ArrayList<>();
        labels = new ArrayList<>();
        colors = new ArrayList<>();

        lineChart.setOnLongClickListener(this);
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

    private void updateLineChart(CurveData[] points) {
        tv_no_data_warn.setVisibility(View.GONE);

        if (!LoggingConfigure.LOGGING) {
            for (CurveData point : points) {
                Log.d(TAG, point.time + " " + point.value);
            }
        }

        entries.clear();
        labels.clear();
        colors.clear();

        float up = Float.MIN_VALUE, down = Float.MIN_VALUE;
        for (int i = 0; i < attrs.length; i++) {
            if (attrs[i].equals(spinnerItem)) {
                up = Float.valueOf(ups[i]);
                if (downs[i] != null && !downs[i].equals("null")) {
                    down = Float.valueOf(downs[i]);
                }
            }
        }

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
                if (value < down || value > up) {
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
