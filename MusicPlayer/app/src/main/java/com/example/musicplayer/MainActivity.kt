package com.example.musicplayer

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private var musicService: MusicService? = null
    private var isBound = false
    private lateinit var adapter: SongAdapter
    private val songs = mutableListOf<Song>()
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var tvCurrentTitle: TextView
    private lateinit var tvCurrentArtist: TextView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var recyclerView: RecyclerView

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    private var progressRunnable: Runnable? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
            musicService?.setSongs(songs)
            setupServiceCallbacks()
            startProgressUpdate()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        checkPermissions()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        tvCurrentTitle = findViewById(R.id.tvCurrentTitle)
        tvCurrentArtist = findViewById(R.id.tvCurrentArtist)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
        seekBar = findViewById(R.id.seekBar)

        adapter = SongAdapter(songs) { index ->
            musicService?.playSong(index)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnPlayPause.setOnClickListener {
            if (musicService?.isPlaying() == true) {
                musicService?.togglePlayPause()
            } else if (songs.isNotEmpty()) {
                val currentIndex = musicService?.getCurrentIndex() ?: 0
                // getDuration() == 0 means no song has been loaded yet
                if (musicService?.getDuration() == 0) {
                    musicService?.playSong(currentIndex)
                } else {
                    musicService?.togglePlayPause()
                }
            }
        }

        btnPrevious.setOnClickListener { musicService?.playPrevious() }
        btnNext.setOnClickListener { musicService?.playNext() }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) musicService?.seekTo(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun checkPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            loadSongs()
            bindMusicService()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            loadSongs()
            bindMusicService()
        } else {
            Toast.makeText(this, "Permission denied. Cannot load songs.", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadSongs() {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val cursor: Cursor? = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection, selection, null, sortOrder
        )

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (it.moveToNext()) {
                songs.add(
                    Song(
                        id = it.getLong(idCol),
                        title = it.getString(titleCol) ?: "Unknown Title",
                        artist = it.getString(artistCol) ?: "Unknown Artist",
                        duration = it.getLong(durationCol),
                        path = it.getString(dataCol) ?: ""
                    )
                )
            }
        }

        adapter.notifyDataSetChanged()

        if (songs.isEmpty()) {
            Toast.makeText(this, "No music files found on device.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bindMusicService() {
        val intent = Intent(this, MusicService::class.java)
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun setupServiceCallbacks() {
        musicService?.onSongChanged = { index ->
            runOnUiThread {
                if (index < songs.size) {
                    val song = songs[index]
                    tvCurrentTitle.text = song.title
                    tvCurrentArtist.text = song.artist
                    adapter.setSelected(index)
                    seekBar.max = musicService?.getDuration() ?: 0
                }
            }
        }
        musicService?.onPlayStateChanged = { isPlaying ->
            runOnUiThread {
                btnPlayPause.setImageResource(
                    if (isPlaying) android.R.drawable.ic_media_pause
                    else android.R.drawable.ic_media_play
                )
            }
        }
    }

    private fun startProgressUpdate() {
        progressRunnable = object : Runnable {
            override fun run() {
                if (isBound && !isFinishing) {
                    val position = musicService?.getCurrentPosition() ?: 0
                    val duration = musicService?.getDuration() ?: 0
                    seekBar.max = duration
                    seekBar.progress = position
                }
                handler.postDelayed(this, 500)
            }
        }
        handler.post(progressRunnable!!)
    }

    override fun onDestroy() {
        progressRunnable?.let { handler.removeCallbacks(it) }
        handler.removeCallbacksAndMessages(null)
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        super.onDestroy()
    }
}
