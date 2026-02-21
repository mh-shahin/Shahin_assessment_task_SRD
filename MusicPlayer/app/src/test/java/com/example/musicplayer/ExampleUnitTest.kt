package com.example.musicplayer

import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun song_formatDuration_isCorrect() {
        val durationMs = 185000L // 3 minutes 5 seconds
        val minutes = (durationMs / 1000) / 60
        val seconds = (durationMs / 1000) % 60
        val formatted = String.format("%d:%02d", minutes, seconds)
        assertEquals("3:05", formatted)
    }
}
