package com.example.musicplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class MusicService : Service() {

    private val binder = MusicBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var songs: List<Song> = emptyList()
    private var currentIndex = 0

    var onSongChanged: ((Int) -> Unit)? = null
    var onPlayStateChanged: ((Boolean) -> Unit)? = null

    companion object {
        const val CHANNEL_ID = "MusicPlayerChannel"
        const val NOTIFICATION_ID = 1
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent): IBinder = binder

    fun setSongs(songs: List<Song>) {
        this.songs = songs
    }

    fun playSong(index: Int) {
        if (songs.isEmpty()) return
        currentIndex = index.coerceIn(0, songs.lastIndex)
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(songs[currentIndex].path)
            } catch (e: Exception) {
                Log.e("MusicService", "Failed to set data source for ${songs[currentIndex].path}", e)
                onPlayStateChanged?.invoke(false)
                release()
                mediaPlayer = null
                return
            }
            setOnPreparedListener { mp ->
                mp.start()
                onSongChanged?.invoke(currentIndex)
                onPlayStateChanged?.invoke(true)
                startForeground(NOTIFICATION_ID, buildNotification())
            }
            setOnCompletionListener { playNext() }
            prepareAsync()
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            onPlayStateChanged?.invoke(false)
        } else {
            player.start()
            onPlayStateChanged?.invoke(true)
        }
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    fun playNext() {
        if (songs.isEmpty()) return
        playSong((currentIndex + 1) % songs.size)
    }

    fun playPrevious() {
        if (songs.isEmpty()) return
        playSong((currentIndex - 1 + songs.size) % songs.size)
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun getDuration(): Int = mediaPlayer?.duration ?: 0

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun getCurrentIndex(): Int = currentIndex

    private fun buildNotification(): Notification {
        val song = if (songs.isNotEmpty()) songs[currentIndex] else null
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(song?.title ?: "Music Player")
            .setContentText(song?.artist ?: "")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Music Playback",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}
