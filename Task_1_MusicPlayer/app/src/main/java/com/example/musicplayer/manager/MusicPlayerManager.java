package com.example.musicplayer.manager;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.example.musicplayer.model.Song;
import com.example.musicplayer.repository.MusicRepository;

import java.lang.ref.WeakReference;


public class MusicPlayerManager {

    private static final String TAG = "MusicPlayerManager";

    // Player states
    public enum PlayerState {
        IDLE, PLAYING, PAUSED, STOPPED
    }

    private static MusicPlayerManager instance;

    private WeakReference<Context> contextRef;
    private MediaPlayer mediaPlayer;
    private final MusicRepository musicRepository;
    private int currentSongIndex = 0;
    private PlayerState currentState = PlayerState.IDLE;
    private OnPlayerEventListener eventListener;

    public interface OnPlayerEventListener {
        void onSongChanged(Song song, int index);
        void onStateChanged(PlayerState state);
        void onPlaybackCompleted();
    }

    private MusicPlayerManager() {
        musicRepository = MusicRepository.getInstance();
    }

    public static synchronized MusicPlayerManager getInstance() {
        if (instance == null) {
            instance = new MusicPlayerManager();
        }
        return instance;
    }

    public void init(Context context) {
        this.contextRef = new WeakReference<>(context.getApplicationContext());
    }

    public void setOnPlayerEventListener(OnPlayerEventListener listener) {
        this.eventListener = listener;
    }

    public void play() {
        if (currentState == PlayerState.PAUSED && mediaPlayer != null) {
            mediaPlayer.start();
            currentState = PlayerState.PLAYING;
            notifyStateChanged();
            Log.d(TAG, "Resumed: " + getCurrentSong().getTitle());
        } else {
            loadAndPlay(currentSongIndex);
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            currentState = PlayerState.PAUSED;
            notifyStateChanged();
            Log.d(TAG, "Paused: " + getCurrentSong().getTitle());
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            releasePlayer();
        }
        currentState = PlayerState.STOPPED;
        notifyStateChanged();
        Log.d(TAG, "Stopped");
    }


    public void playNext() {
        currentSongIndex = (currentSongIndex + 1) % musicRepository.getSongCount();
        loadAndPlay(currentSongIndex);
        Log.d(TAG, "Next song: " + getCurrentSong().getTitle());
    }


    public void playPrevious() {
        currentSongIndex = (currentSongIndex - 1 + musicRepository.getSongCount())
                % musicRepository.getSongCount();
        loadAndPlay(currentSongIndex);
        Log.d(TAG, "Previous song: " + getCurrentSong().getTitle());
    }

    public void playSongAt(int index) {
        if (index >= 0 && index < musicRepository.getSongCount()) {
            currentSongIndex = index;
            loadAndPlay(currentSongIndex);
        }
    }

    private void loadAndPlay(int index) {
        releasePlayer();

        Song song = musicRepository.getSongAt(index);
        Context context = (contextRef != null) ? contextRef.get() : null;
        if (song == null || context == null) return;

        mediaPlayer = MediaPlayer.create(context, song.getResourceId());

        if (mediaPlayer == null) {
            Log.e(TAG, "Failed to create MediaPlayer for: " + song.getTitle());
            return;
        }

        // Auto-advance to next song when current song ends
        mediaPlayer.setOnCompletionListener(mp -> {
            currentState = PlayerState.STOPPED;
            notifyPlaybackCompleted();
            playNext(); // Auto-play next
        });

        mediaPlayer.start();
        currentState = PlayerState.PLAYING;

        notifySongChanged(song);
        notifyStateChanged();

        Log.d(TAG, "Now playing: " + song.getTitle());
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            } catch (IllegalStateException e) {
                // Ignore if already stopped
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    public PlayerState getCurrentState() { return currentState; }
    public int getCurrentSongIndex() { return currentSongIndex; }

    public Song getCurrentSong() {
        return musicRepository.getSongAt(currentSongIndex);
    }

    public int getCurrentPosition() {
        return (mediaPlayer != null) ? mediaPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return (mediaPlayer != null) ? mediaPlayer.getDuration() : 0;
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }


    public void seekTo(int milliseconds) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(milliseconds);
        }
    }

    private void notifySongChanged(Song song) {
        if (eventListener != null) {
            eventListener.onSongChanged(song, currentSongIndex);
        }
    }

    private void notifyStateChanged() {
        if (eventListener != null) {
            eventListener.onStateChanged(currentState);
        }
    }

    private void notifyPlaybackCompleted() {
        if (eventListener != null) {
            eventListener.onPlaybackCompleted();
        }
    }

    public void destroy() {
        releasePlayer();
        currentState = PlayerState.IDLE;
    }
}
