package com.example.sensorreader

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var sensorManager: SensorManager
    private val sensorList = mutableListOf<SensorInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        loadSensors()

        val tvCount = findViewById<TextView>(R.id.tvSensorCount)
        tvCount.text = getString(R.string.sensor_count_format, sensorList.size)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = SensorAdapter(sensorList) { sensorInfo ->
            val intent = Intent(this, SensorDetailActivity::class.java).apply {
                putExtra(SensorDetailActivity.EXTRA_SENSOR_TYPE, sensorInfo.type)
                putExtra(SensorDetailActivity.EXTRA_SENSOR_NAME, sensorInfo.name)
            }
            startActivity(intent)
        }
    }

    private fun loadSensors() {
        val allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        for (sensor in allSensors) {
            sensorList.add(
                SensorInfo(
                    sensor = sensor,
                    name = sensor.name,
                    type = sensor.type,
                    vendor = sensor.vendor,
                    typeName = getSensorTypeName(sensor.type)
                )
            )
        }
    }

    private fun getSensorTypeName(type: Int): String = when (type) {
        Sensor.TYPE_ACCELEROMETER -> "Accelerometer"
        Sensor.TYPE_MAGNETIC_FIELD -> "Magnetic Field"
        Sensor.TYPE_GYROSCOPE -> "Gyroscope"
        Sensor.TYPE_LIGHT -> "Light"
        Sensor.TYPE_PRESSURE -> "Pressure"
        Sensor.TYPE_PROXIMITY -> "Proximity"
        Sensor.TYPE_GRAVITY -> "Gravity"
        Sensor.TYPE_LINEAR_ACCELERATION -> "Linear Acceleration"
        Sensor.TYPE_ROTATION_VECTOR -> "Rotation Vector"
        Sensor.TYPE_RELATIVE_HUMIDITY -> "Relative Humidity"
        Sensor.TYPE_AMBIENT_TEMPERATURE -> "Ambient Temperature"
        Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED -> "Magnetic Field (Uncalibrated)"
        Sensor.TYPE_GAME_ROTATION_VECTOR -> "Game Rotation Vector"
        Sensor.TYPE_GYROSCOPE_UNCALIBRATED -> "Gyroscope (Uncalibrated)"
        Sensor.TYPE_SIGNIFICANT_MOTION -> "Significant Motion"
        Sensor.TYPE_STEP_DETECTOR -> "Step Detector"
        Sensor.TYPE_STEP_COUNTER -> "Step Counter"
        Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> "Geomagnetic Rotation Vector"
        Sensor.TYPE_HEART_RATE -> "Heart Rate"
        Sensor.TYPE_POSE_6DOF -> "Pose 6DOF"
        Sensor.TYPE_STATIONARY_DETECT -> "Stationary Detect"
        Sensor.TYPE_MOTION_DETECT -> "Motion Detect"
        Sensor.TYPE_HEART_BEAT -> "Heart Beat"
        Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT -> "Low Latency Off-Body Detect"
        Sensor.TYPE_ACCELEROMETER_UNCALIBRATED -> "Accelerometer (Uncalibrated)"
        else -> "Type $type"
    }
}
