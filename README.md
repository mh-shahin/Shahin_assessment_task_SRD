# Shahin Assessment Task SRD

Two Android applications built with Kotlin.

## Apps

### 1. Music Player (`MusicPlayer/`)

A fully-featured music player app that:
- Queries the device's MediaStore for audio files
- Displays a scrollable list of songs with title, artist, and duration
- Plays audio via a foreground `Service` (supports background playback)
- Supports play/pause, next track, previous track
- Shows a SeekBar with live playback progress
- Requests `READ_MEDIA_AUDIO` (API 33+) / `READ_EXTERNAL_STORAGE` at runtime

**Key files:**
- `MainActivity.kt` — UI, permissions, MediaStore query, service binding
- `MusicService.kt` — `MediaPlayer` wrapped in a foreground Service
- `SongAdapter.kt` — RecyclerView adapter with selection highlighting
- `Song.kt` — Data class for audio metadata

### 2. Sensor Reader (`SensorReader/`)

A sensor monitoring app that:
- Lists all available hardware sensors on the device (using `SensorManager.getSensorList`)
- Shows sensor name, type, and vendor for each
- Tapping a sensor opens a detail screen with live real-time readings (X, Y, Z axes)
- Displays sensor metadata: vendor, resolution, maximum range, accuracy
- Correctly registers/unregisters the `SensorEventListener` in `onResume`/`onPause`

**Key files:**
- `MainActivity.kt` — Sensor list screen
- `SensorDetailActivity.kt` — Live sensor readings
- `SensorAdapter.kt` — RecyclerView adapter
- `SensorInfo.kt` — Data class wrapping `android.hardware.Sensor`

## Requirements

- Android Studio Hedgehog or later
- Android SDK 34
- minSdk 26 (Android 8.0+)
- Gradle 8.2