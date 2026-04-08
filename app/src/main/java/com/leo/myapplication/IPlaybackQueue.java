package com.leo.myapplication;

public interface IPlaybackQueue {
    Song getNext();
    Song getPrevious();
    Song getCurrent();
    boolean hasNext();
    boolean hasPrevious();
    void setQueue(java.util.List<Song> songs, int startIndex);
}