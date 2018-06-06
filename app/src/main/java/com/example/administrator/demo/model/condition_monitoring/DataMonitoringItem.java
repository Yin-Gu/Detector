package com.example.administrator.demo.model.condition_monitoring;

import java.util.Arrays;

public class DataMonitoringItem {
    private Integer motorId;
    private String motorName;
    private Boolean status;
    private String[] attrs;
    private String[] values;

    public DataMonitoringItem(Integer motorId, String motorName, Boolean status, String[] attrs, String[] values) {
        this.motorId = motorId;
        this.motorName = motorName;
        this.status = status;
        this.attrs = attrs;
        this.values = values;
    }

    public Integer getMotorId() {
        return motorId;
    }

    public void setMotorId(Integer motorId) {
        this.motorId = motorId;
    }

    public String getMotorName() {
        return motorName;
    }

    public void setMotorName(String motorName) {
        this.motorName = motorName;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String[] getAttrs() {
        return attrs;
    }

    public void setAttrs(String[] attrs) {
        this.attrs = attrs;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "DataMonitoringItem{" +
                "motorId=" + motorId +
                ", motorName='" + motorName + '\'' +
                ", status=" + status +
                ", attrs=" + Arrays.toString(attrs) +
                ", values=" + Arrays.toString(values) +
                '}';
    }
}
