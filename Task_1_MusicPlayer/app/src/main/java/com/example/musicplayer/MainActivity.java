package com.example.musicplayer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.adapter.SongAdapter;
import com.example.musicplayer.manager.MusicPlayerManager;
import com.example.musicplayer.manager.MusicPlayerManager.PlayerState;
import com.example.musicplayer.model.Song;
import com.example.musicplayer.repository.MusicRepository;

import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements MusicPlayerManager.OnPlayerEventListener {

    // UI Components
    private TextView tvCurrentTitle;
    private TextView tvCurrentArtist;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;
    private ImageButton btnPlayPause;
    private ImageButton btnStop;
    private ImageButton btnNext;
    private ImageButton btnPrevious;
    private SeekBar seekBar;
    private RecyclerView rvSongList;

    // Player & Data
    private MusicPlayerManager playerManager;
    private SongAdapter songAdapter;

    // Handler for updating SeekBar every second
    private final Handler seekBarHandler = new Handler(Looper.getMainLooper());
    private final Runnable seekBarUpdater = new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
            seekBarHandler.postDelayed(this, 500); // Update every 500ms
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initPlayer();
        setupRecyclerView();
        setupClickListeners();
        setupSeekBar();
    }

    private void initViews() {
        tvCurrentTitle  = findViewById(R.id.tvCurrentTitle);
        tvCurrentArtist = findViewById(R.id.tvCurrentArtist);
        tvCurrentTime   = findViewById(R.id.tvCurrentTime);
        tvTotalTime     = findViewById(R.id.tvTotalTime);
        btnPlayPause    = findViewById(R.id.btnPlayPause);
        btnStop         = findViewById(R.id.btnStop);
        btnNext         = findViewById(R.id.btnNext);
        btnPrevious     = findViewById(R.id.btnPrevious);
        seekBar         = findViewById(R.id.seekBar);
        rvSongList      = findViewById(R.id.rvSongList);
    }

    private void initPlayer() {
        playerManager = MusicPlayerManager.getInstance();
        playerManager.init(this);
        playerManager.setOnPlayerEventListener(this);

        // Show first song info on launch
        Song firstSong = MusicRepository.getInstance().getSongAt(0);
        if (firstSong != null) {
            updateSongInfo(firstSong);
        }
    }

    private void setupRecyclerView() {
        songAdapter = new SongAdapter(
                MusicRepository.getInstance().getAllSongs(),
                (song, position) -> playerManager.playSongAt(position)
        );
        rvSongList.setLayoutManager(new LinearLayoutManager(this));
        rvSongList.setAdapter(songAdapter);
    }

    private void setupClickListeners() {
        // Play / Pause toggle
        btnPlayPause.setOnClickListener(v -> {
            if (playerManager.isPlaying()) {
                playerManager.pause();
            } else {
                playerManager.play();
            }
        });

        // Stop
        btnStop.setOnClickListener(v -> playerManager.stop());

        // Next song
        btnNext.setOnClickListener(v -> playerManager.playNext());

        // Previous song
        btnPrevious.setOnClickListener(v -> playerManager.playPrevious());
    }

    private void setupSeekBar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    playerManager.seekTo(progress);
                    tvCurrentTime.setText(formatTime(progress));
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    public void onSongChanged(Song song, int index) {
        runOnUiThread(() -> {
            updateSongInfo(song);
            songAdapter.setSelectedPosition(index);
            rvSongList.scrollToPosition(index);
        });
    }

    @Override
    public void onStateChanged(PlayerState state) {
        runOnUiThread(() -> {
            if (state == PlayerState.PLAYING) {
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                seekBarHandler.post(seekBarUpdater); // Start updating seekbar
            } else {
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
                seekBarHandler.removeCallbacks(seekBarUpdater); // Stop updating
            }
        });
    }

    @Override
    public void onPlaybackCompleted() {
        runOnUiThread(() -> seekBar.setProgress(0));
    }


    private void updateSongInfo(Song song) {
        tvCurrentTitle.setText(song.getTitle());
        tvCurrentArtist.setText(song.getArtist());
        tvTotalTime.setText(song.getDuration());
        tvCurrentTime.setText(getString(R.string.initial_time));
        seekBar.setProgress(0);
    }

    private void updateSeekBar() {
        int duration = playerManager.getDuration();
        int position = playerManager.getCurrentPosition();
        if (duration > 0) {
            seekBar.setMax(duration);
            seekBar.setProgress(position);
            tvCurrentTime.setText(formatTime(position));
            tvTotalTime.setText(formatTime(duration));
        }
    }

    private String formatTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        seekBarHandler.removeCallbacks(seekBarUpdater);
        playerManager.destroy();
    }
}
