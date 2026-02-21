package com.example.sensorreader.model;

public class SensorItem {

    private String sensorName;
    private String sensorType;
    private String currentValue;
    private String unit;
    private boolean isAvailable;

    public SensorItem(String sensorName, String sensorType, String unit, boolean isAvailable) {
        this.sensorName = sensorName;
        this.sensorType = sensorType;
        this.unit = unit;
        this.isAvailable = isAvailable;
        this.currentValue = isAvailable ? "Reading..." : "Not Available";
    }

    // Getters
    public String getSensorName() { return sensorName; }
    public String getSensorType() { return sensorType; }
    public String getCurrentValue() { return currentValue; }
    public String getUnit() { return unit; }
    public boolean isAvailable() { return isAvailable; }

    // Setters
    public void setSensorName(String sensorName) { this.sensorName = sensorName; }
    public void setSensorType(String sensorType) { this.sensorType = sensorType; }
    public void setCurrentValue(String currentValue) { this.currentValue = currentValue; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setAvailable(boolean available) { isAvailable = available; }
}
