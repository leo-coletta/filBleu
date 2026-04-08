package com.leo.myapplication;

import java.util.List;

public interface IPlaybackQueue {
    void setQueue(List<Song> songs, int startIndex);
    Song getNext();
    Song getPrevious();
    Song getCurrent();
    int getCurrentIndex();
}