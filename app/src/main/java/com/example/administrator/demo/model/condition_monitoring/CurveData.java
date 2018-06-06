package com.example.administrator.demo.model.condition_monitoring;

/**
 * Created by Administrator on 2018/6/2 0002.
 */

public class CurveData {
    public String time;
    public double value;

    @Override
    public String toString() {
        return "CurveData{" +
                "time='" + time + '\'' +
                ", value=" + value +
                '}';
    }
}
