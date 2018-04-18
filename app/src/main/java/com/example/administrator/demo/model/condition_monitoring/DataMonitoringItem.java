package com.example.administrator.demo.model.condition_monitoring;

public class DataMonitoringItem {
    private Integer motorId;
    private String equipmentName;
    private Boolean status;
    private String temperature;
    private String voltage;
    private String electricCurrent;

    public DataMonitoringItem(Integer motorId, String equipmentName, Boolean status, String temperature, String voltage, String electricCurrent) {
        this.motorId = motorId;
        this.equipmentName = equipmentName;
        this.status = status;
        this.temperature = temperature;
        this.voltage = voltage;
        this.electricCurrent = electricCurrent;
    }

    public Integer getMotorId() {return motorId; }

    public String getEquipmentName() {
        return equipmentName;
    }

    public Boolean getStatus() {
        return status;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getVoltage() {
        return voltage;
    }

    public String getElectricCurrent() {
        return electricCurrent;
    }

    public void setMotorId(Integer motorId) {
        this.motorId = motorId;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public void setVoltage(String voltage) {
        this.voltage = voltage;
    }

    public void setElectricCurrent(String electricCurrent) {
        this.electricCurrent = electricCurrent;
    }

    @Override
    public String toString() {
        return "DataMonitoringItem{" +
                "motorId=" + motorId +
                ", equipmentName='" + equipmentName + '\'' +
                ", status=" + status +
                ", temperature='" + temperature + '\'' +
                ", voltage='" + voltage + '\'' +
                ", electricCurrent='" + electricCurrent + '\'' +
                '}';
    }
}
