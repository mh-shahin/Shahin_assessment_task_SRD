package com.example.musicplayer.model;

public class Song {

    private int id;
    private String title;
    private String artist;
    private String duration;
    private int resourceId; // Reference to res/raw/ music file

    public Song(int id, String title, String artist, String duration, int resourceId) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.resourceId = resourceId;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getDuration() { return duration; }
    public int getResourceId() { return resourceId; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setArtist(String artist) { this.artist = artist; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setResourceId(int resourceId) { this.resourceId = resourceId; }
}