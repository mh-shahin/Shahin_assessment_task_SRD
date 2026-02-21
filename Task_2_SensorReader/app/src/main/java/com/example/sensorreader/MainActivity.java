package com.example.sensorreader;

import android.hardware.Sensor;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sensorreader.adapter.SensorAdapter;
import com.example.sensorreader.manager.DeviceSensorManager;
import com.example.sensorreader.model.SensorItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements DeviceSensorManager.OnSensorDataListener {

    private RecyclerView rvSensors;
    private DeviceSensorManager sensorManager;
    private SensorAdapter sensorAdapter;
    private List<SensorItem> sensorItemList;

    private Map<Integer, Integer> sensorTypeToIndexMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initSensorManager();
        buildSensorList();
        setupRecyclerView();
    }


    private void initViews() {
        rvSensors = findViewById(R.id.rvSensors);
        TextView tvDeviceInfo = findViewById(R.id.tvDeviceInfo);
        String deviceDescription = "Device: " + android.os.Build.MODEL +
                " | Android " + android.os.Build.VERSION.RELEASE;
        tvDeviceInfo.setText(deviceDescription);
    }

    private void initSensorManager() {
        sensorManager = new DeviceSensorManager(this);
        sensorManager.setOnSensorDataListener(this);
    }

    private void buildSensorList() {
        sensorItemList = new ArrayList<>();
        sensorTypeToIndexMap = new HashMap<>();

        for (int i = 0; i < DeviceSensorManager.TARGET_SENSORS.length; i++) {
            DeviceSensorManager.SensorConfig config = DeviceSensorManager.TARGET_SENSORS[i];
            boolean available = sensorManager.isSensorAvailable(config.type);

            SensorItem item = new SensorItem(
                    config.name,
                    getSensorTypeLabel(config.type),
                    config.unit,
                    available
            );
            sensorItemList.add(item);
            sensorTypeToIndexMap.put(config.type, i);
        }
    }

    private void setupRecyclerView() {
        sensorAdapter = new SensorAdapter(sensorItemList);
        rvSensors.setLayoutManager(new LinearLayoutManager(this));
        rvSensors.setAdapter(sensorAdapter);
    }

    @Override
    public void onSensorDataUpdated(int sensorType, float[] values) {
        Integer index = sensorTypeToIndexMap.get(sensorType);
        if (index == null) return;

        String formattedValue = DeviceSensorManager.formatValues(sensorType, values);
        String unit = sensorItemList.get(index).getUnit();
        String displayText = formattedValue + "  " + unit;

        runOnUiThread(() -> sensorAdapter.updateSensorValue(index, displayText));
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Start listening when app is visible
        sensorManager.startListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop listening when app goes to background — saves battery!
        sensorManager.stopListening();
    }

    private String getSensorTypeLabel(int type) {
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER:
            case Sensor.TYPE_GYROSCOPE:
            case Sensor.TYPE_GRAVITY:
            case Sensor.TYPE_LINEAR_ACCELERATION:
                return "Motion";
            case Sensor.TYPE_MAGNETIC_FIELD:
            case Sensor.TYPE_LIGHT:
            case Sensor.TYPE_PRESSURE:
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return "Environment";
            default:
                return "Other";
        }
    }
}
