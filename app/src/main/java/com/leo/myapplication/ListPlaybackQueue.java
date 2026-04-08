package com.leo.myapplication;

import java.util.ArrayList;
import java.util.List;

public class ListPlaybackQueue implements IPlaybackQueue {
    private List<Song> songList = new ArrayList<>();
    private int currentIndex = 0;

    @Override
    public void setQueue(List<Song> songs, int startIndex) {
        this.songList = (songs != null) ? songs : new ArrayList<>();
        this.currentIndex = (startIndex >= 0 && startIndex < songList.size()) ? startIndex : 0;
    }

    @Override
    public Song getNext() {
        if (songList.isEmpty()) return null;
        // Comportement "Loop" : si fin de liste, revient au début
        currentIndex = (currentIndex + 1) % songList.size();
        return getCurrent();
    }

    @Override
    public Song getPrevious() {
        if (songList.isEmpty()) return null;
        // Si début de liste, va à la fin
        currentIndex = (currentIndex - 1 + songList.size()) % songList.size();
        return getCurrent();
    }

    @Override
    public Song getCurrent() {
        return (currentIndex >= 0 && currentIndex < songList.size()) ? songList.get(currentIndex) : null;
    }

    @Override
    public boolean hasNext() { return !songList.isEmpty(); }
    @Override
    public boolean hasPrevious() { return !songList.isEmpty(); }
}