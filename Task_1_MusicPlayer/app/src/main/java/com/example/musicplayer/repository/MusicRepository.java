package com.example.musicplayer.repository;

import com.example.musicplayer.R;
import com.example.musicplayer.model.Song;

import java.util.ArrayList;
import java.util.List;


public class MusicRepository {

    private static MusicRepository instance;
    private final List<Song> songList;

    // Private constructor for Singleton pattern
    private MusicRepository() {
        songList = new ArrayList<>();
        loadSongs();
    }

    public static MusicRepository getInstance() {
        if (instance == null) {
            instance = new MusicRepository();
        }
        return instance;
    }

    private void loadSongs() {
        songList.add(new Song(1, "Acoustic Breeze", "Benjamin Mossotti", "2:30", R.raw.music1));
        songList.add(new Song(2, "Creative Minds", "Benjamin Mossotti", "2:13", R.raw.music2));
        songList.add(new Song(3, "Sunny", "Benjamin Mossotti", "2:20", R.raw.music3));
    }

    public List<Song> getAllSongs() {
        return songList;
    }

    public Song getSongAt(int index) {
        if (index >= 0 && index < songList.size()) {
            return songList.get(index);
        }
        return null;
    }

    public int getSongCount() {
        return songList.size();
    }
}