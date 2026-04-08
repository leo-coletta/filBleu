package com.leo.myapplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation basée sur une liste de {@link IPlaybackQueue}.
 * Gère une file de lecture cyclique (boucle automatiquement à la fin ou au début).
 */
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
        currentIndex = (currentIndex + 1) % songList.size();
        return getCurrent();
    }

    @Override
    public Song getPrevious() {
        if (songList.isEmpty()) return null;
        currentIndex = (currentIndex - 1 + songList.size()) % songList.size();
        return getCurrent();
    }

    @Override
    public Song getCurrent() {
        return (currentIndex >= 0 && currentIndex < songList.size()) ? songList.get(currentIndex) : null;
    }

    @Override
    public int getCurrentIndex() {
        return currentIndex;
    }
}