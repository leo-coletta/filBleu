package com.leo.myapplication;

public class CurrentSongManager {
    private static CurrentSongManager instance;
    private Song currentSong;

    private CurrentSongManager() {}

    public static synchronized CurrentSongManager getInstance() {
        if (instance == null) {
            instance = new CurrentSongManager();
        }
        return instance;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public void setCurrentSong(Song song) {
        this.currentSong = song;
    }
}