package com.example.sensorreader.manager;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Locale;

public class DeviceSensorManager implements SensorEventListener {

    private final SensorManager androidSensorManager;
    private OnSensorDataListener dataListener;

    public interface OnSensorDataListener {
        void onSensorDataUpdated(int sensorType, float[] values);
    }

    public static class SensorConfig {
        public final int type;
        public final String name;
        public final String unit;

        public SensorConfig(int type, String name, String unit) {
            this.type = type;
            this.name = name;
            this.unit = unit;
        }
    }

    public static final SensorConfig[] TARGET_SENSORS = {
            new SensorConfig(Sensor.TYPE_ACCELEROMETER,        "Accelerometer",         "m/s²"),
            new SensorConfig(Sensor.TYPE_AMBIENT_TEMPERATURE,  "Ambient Temperature",   "°C"),
            new SensorConfig(Sensor.TYPE_TEMPERATURE,          "Internal Temperature",  "°C"),
            new SensorConfig(Sensor.TYPE_GYROSCOPE,            "Gyroscope",             "rad/s"),
            new SensorConfig(Sensor.TYPE_MAGNETIC_FIELD,       "Magnetic Field",        "μT"),
            new SensorConfig(Sensor.TYPE_LIGHT,                "Light",                 "lux"),
            new SensorConfig(Sensor.TYPE_PRESSURE,             "Pressure (Barometer)",  "hPa"),
            new SensorConfig(Sensor.TYPE_PROXIMITY,            "Proximity",             "cm"),
            new SensorConfig(Sensor.TYPE_GRAVITY,              "Gravity",               "m/s²"),
            new SensorConfig(Sensor.TYPE_LINEAR_ACCELERATION,  "Linear Acceleration",   "m/s²"),
            new SensorConfig(Sensor.TYPE_RELATIVE_HUMIDITY,    "Relative Humidity",     "%"),
    };

    public DeviceSensorManager(Context context) {
        androidSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public void setOnSensorDataListener(OnSensorDataListener listener) {
        this.dataListener = listener;
    }

    public boolean isSensorAvailable(int sensorType) {
        return androidSensorManager != null && androidSensorManager.getDefaultSensor(sensorType) != null;
    }

    public void startListening() {
        if (androidSensorManager == null) return;
        for (SensorConfig config : TARGET_SENSORS) {
            Sensor sensor = androidSensorManager.getDefaultSensor(config.type);
            if (sensor != null) {
                androidSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    public void stopListening() {
        if (androidSensorManager != null) {
            androidSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (dataListener != null && event.values != null && event.values.length > 0) {
            dataListener.onSensorDataUpdated(event.sensor.getType(), event.values);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public static String formatValues(int sensorType, float[] values) {
        if (values == null || values.length == 0) return "N/A";

        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
            case Sensor.TYPE_GYROSCOPE:
            case Sensor.TYPE_MAGNETIC_FIELD:
            case Sensor.TYPE_GRAVITY:
            case Sensor.TYPE_LINEAR_ACCELERATION:
                if (values.length >= 3) {
                    return String.format(Locale.US, "X: %.2f  Y: %.2f  Z: %.2f", values[0], values[1], values[2]);
                }
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
            case Sensor.TYPE_TEMPERATURE:
                if (values[0] <= -270.0f || values[0] == 0.0f) {
                    return "Waiting...";
                }
                return String.format(Locale.US, "%.1f", values[0]);
            case Sensor.TYPE_LIGHT:
            case Sensor.TYPE_PRESSURE:
            case Sensor.TYPE_PROXIMITY:
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return String.format(Locale.US, "%.1f", values[0]);
        }
        return String.format(Locale.US, "%.2f", values[0]);
    }
}
