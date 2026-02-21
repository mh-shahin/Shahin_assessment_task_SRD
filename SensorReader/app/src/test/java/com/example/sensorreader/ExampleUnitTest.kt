package com.example.sensorreader

import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun sensorValueFormat_isCorrect() {
        val value = 9.81f
        val formatted = String.format("%.6f", value)
        assertEquals("9.810000", formatted)
    }
}
