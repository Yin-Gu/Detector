package com.example.administrator.demo.test;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.administrator.demo.R;
import com.example.administrator.demo.conf.LoggingConfigure;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/4/30 0030.
 */

public class TestLineChartLaunch extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "TestLineChartLaunch";

    private LineChart lineChart;

    private List<Entry> entries;    //点集
    private List<String> labels;    //X轴标签

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_line_chart_launch);

        lineChart = findViewById(R.id.line_chart_test_launch);
        lineChart.setOnClickListener(this);
        initLineChart();
        TestLineChart.ChartPoint[] points = getData();
        updateLineChart(points);
    }

    @Override
    public void onClick(View v) {
        if (v == lineChart) {
            Intent intent = new Intent(this, TestLineChart.class);
            startActivity(intent);
            overridePendingTransition(R.anim.next_enter, R.anim.current_exit);
        }
    }

    private void initLineChart() {
        //是否隐藏图表描述
        lineChart.getDescription().setEnabled(false);
        lineChart.setPinchZoom(true);
        //默认Y轴有两个，左右各一个
        //设置右轴不显示
        lineChart.getAxisRight().setEnabled(false);
        lineChart.setOnClickListener(this);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(Color.LTGRAY);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(12);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setDrawGridLines(true);
        yAxis.setGridColor(Color.LTGRAY);
        yAxis.setTextColor(Color.BLACK);
        yAxis.setTextSize(12);

        entries = new ArrayList<>();
        labels = new ArrayList<>();
    }

    private TestLineChart.ChartPoint[] getData() {
        int length = 100;
        TestLineChart.ChartPoint[] chartPoints = new TestLineChart.ChartPoint[length];
        for (int i = 0; i < length; i++) {
            chartPoints[i] = new TestLineChart.ChartPoint();
            chartPoints[i].time = i + "time";
            chartPoints[i].value = (int) (Math.random() * 201);
        }

        return chartPoints;
    }

    private void updateLineChart(TestLineChart.ChartPoint[] points) {
        Log.d(TAG, "updateLineChart");

        if (LoggingConfigure.LOGGING) {
            for (TestLineChart.ChartPoint point : points) {
                Log.d(TAG, point.time + " " + point.value);
            }
        }

        entries.clear();
        labels.clear();
        int maxValue = Integer.MIN_VALUE;
        for (int i = 0; i < points.length; i++) {
            int value = points[i].value;
            maxValue = value > maxValue ? value : maxValue;
            entries.add(new Entry(i * 2.0f, value));
            labels.add(points[i].time);
        }

        LineDataSet dataSet = new LineDataSet(entries, "hello");
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        LineData lineData = new LineData(dataSet);
        lineData.setValueTextSize(10);

        lineChart.setData(lineData);
        lineChart.animateX(1000);

        float maxXRange = 40;
        lineChart.setVisibleXRangeMaximum(maxXRange);
        lineChart.moveViewToX(maxValue - maxXRange);
    }
}
