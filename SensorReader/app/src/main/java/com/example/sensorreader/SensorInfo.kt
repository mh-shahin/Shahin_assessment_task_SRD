package com.example.sensorreader

import android.hardware.Sensor

data class SensorInfo(
    val sensor: Sensor,
    val name: String,
    val type: Int,
    val vendor: String,
    val typeName: String
)
