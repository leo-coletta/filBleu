package com.leo.myapplication;

public class Song {
    private String title;
    private String artist;
    private String audio_url;
    private String image_url;

    public Song() {

    }

    public Song(String title, String artist, String audio_url, String image_url) {
        this.title = title;
        this.artist = artist;
        this.audio_url = audio_url;
        this.image_url = image_url;
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

    public String getAudio_url() {
        return audio_url;
    }

    public void setAudio_url(String audio_url) {
        this.audio_url = audio_url;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }
}
