package com.example.sensorreader

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SensorDetailActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var sensor: Sensor? = null
    private lateinit var tvValues: TextView
    private lateinit var tvStatus: TextView

    companion object {
        const val EXTRA_SENSOR_TYPE = "sensor_type"
        const val EXTRA_SENSOR_NAME = "sensor_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_detail)

        val sensorType = intent.getIntExtra(EXTRA_SENSOR_TYPE, Sensor.TYPE_ACCELEROMETER)
        val sensorName = intent.getStringExtra(EXTRA_SENSOR_NAME) ?: getString(R.string.unknown_sensor)

        supportActionBar?.title = sensorName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tvValues = findViewById(R.id.tvSensorValues)
        tvStatus = findViewById(R.id.tvStatus)

        val tvName = findViewById<TextView>(R.id.tvDetailSensorName)
        tvName.text = sensorName

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(sensorType)

        if (sensor == null) {
            tvStatus.text = getString(R.string.sensor_not_available)
            tvValues.text = "--"
        } else {
            val tvVendor = findViewById<TextView>(R.id.tvDetailVendor)
            val tvResolution = findViewById<TextView>(R.id.tvDetailResolution)
            val tvRange = findViewById<TextView>(R.id.tvDetailRange)
            sensor?.let { s ->
                tvVendor.text = getString(R.string.vendor_format, s.vendor)
                tvResolution.text = getString(R.string.resolution_format, s.resolution)
                tvRange.text = getString(R.string.range_format, s.maximumRange)
            }
            tvStatus.text = getString(R.string.listening)
        }
    }

    override fun onResume() {
        super.onResume()
        sensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val sb = StringBuilder()
        event.values.forEachIndexed { index, value ->
            val axis = when (index) {
                0 -> "X"
                1 -> "Y"
                2 -> "Z"
                else -> "[$index]"
            }
            sb.appendLine("$axis: ${String.format("%.6f", value)}")
        }
        tvValues.text = sb.toString().trimEnd()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        val accuracyText = when (accuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> getString(R.string.accuracy_high)
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> getString(R.string.accuracy_medium)
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> getString(R.string.accuracy_low)
            SensorManager.SENSOR_STATUS_UNRELIABLE -> getString(R.string.accuracy_unreliable)
            else -> getString(R.string.accuracy_unknown)
        }
        tvStatus.text = getString(R.string.accuracy_format, accuracyText)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
