package com.leo.myapplication;

public class Song {
    private String title;
    private String artist;
    private String audioUrl;
    private String imageUrl;

    public Song() {

    }

    public Song(String title, String artist, String audioUrl, String imageUrl) {
        this.title = title;
        this.artist = artist;
        this.audioUrl = audioUrl;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
