package com.example.administrator.demo.test;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.example.administrator.demo.R;
import com.example.administrator.demo.conf.LoggingConfigure;
import com.example.administrator.demo.ui.condition_monitoring.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Administrator on 2018/4/29 0029.
 */

public class TestLineChart extends AppCompatActivity {

    private static final String TAG = "TestLineChart";

    private List<Entry> entries;    //点集
    private List<String> labels;    //X轴标签
    private List<Integer> colors;
    private Unbinder unbinder;

    private volatile boolean end;

    @BindView(R.id.line_chart_test) LineChart lineChart;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    updateLineChart((ChartPoint[]) msg.obj);
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
        setContentView(R.layout.activity_test_line_chart);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initLineChart();
        end = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!end) {
                    Message msg = Message.obtain();
                    msg.what = 1;
                    msg.obj = getData();
                    handler.sendMessage(msg);

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        end = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbinder.unbind();
        end = true;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.next_enter, R.anim.current_exit);
    }

    private void initLineChart() {
        entries = new ArrayList<>();
        labels = new ArrayList<>();
        colors = new ArrayList<>();

        //是否隐藏图表描述
        lineChart.getDescription().setEnabled(false);
//        lineChart.setPinchZoom(true);
        //默认Y轴有两个，左右各一个
        //设置右轴不显示
        lineChart.getAxisRight().setEnabled(false);
        lineChart.setScaleXEnabled(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(Color.LTGRAY);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(12);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new XAxisValueFormatter(labels));

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setDrawGridLines(true);
        yAxis.setGridColor(Color.LTGRAY);
        yAxis.setTextColor(Color.BLACK);
        yAxis.setTextSize(12);
    }

    private ChartPoint[] getData() {
        int length = 20;
        ChartPoint[] chartPoints = new ChartPoint[length];
        for (int i = 0; i < length; i++) {
            chartPoints[i] = new ChartPoint();
            chartPoints[i].time = "2018/" + i + "/31";
            chartPoints[i].value = (int) (Math.random() * 201);
        }

        return chartPoints;
    }

    private void updateLineChart(ChartPoint[] points) {
        Log.d(TAG, "updateLineChart");

        if (LoggingConfigure.LOGGING) {
            for (ChartPoint point : points) {
                Log.d(TAG, point.time + " " + point.value);
            }
        }

        entries.clear();
        labels.clear();
        colors.clear();
        for (int i = 0; i < points.length; i++) {
            int value = points[i].value;
            entries.add(new Entry(i * 1.0f, value));
            labels.add(points[i].time);
            if (value > 100) {
                colors.add(Color.RED);
            } else {
                colors.add(Color.BLACK);
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, "hello");
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        LineData lineData = new LineData(dataSet);
        lineData.setValueTextSize(10);
        lineData.setValueTextColors(colors);

        lineChart.setData(lineData);
        lineChart.animateX(1000);

        float maxXRange = 10;
        float maxXValue = points.length * 1.0f + 1.0f;
        lineChart.setVisibleXRangeMaximum(maxXRange);
        lineChart.getXAxis().setAxisMaximum(maxXValue);
        lineChart.moveViewToX(maxXValue);
    }

    static class ChartPoint {
        String time;
        int value;

        @Override
        public String toString() {
            return "CurveData{" +
                    "time='" + time + '\'' +
                    ", value=" + value +
                    '}';
        }
    }
}
