package com.example.administrator.demo.ui.condition_monitoring.formatter;

import com.example.administrator.demo.conf.ChartConfigure;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.List;

/**
 * Created by Administrator on 2018/5/27 0027.
 */

public class XAxisValueFormatter implements IAxisValueFormatter {

    List<String> labels;

    public XAxisValueFormatter(List<String> labels) {
        this.labels = labels;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        if (value < 0 || value > (labels.size() - 1) * ChartConfigure.DATA_GAP) {
            return "";
        }
        return labels.get((int) (value / ChartConfigure.DATA_GAP));
    }

}
