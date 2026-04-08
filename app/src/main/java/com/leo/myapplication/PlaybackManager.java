package com.leo.myapplication;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;
import java.io.IOException;
import java.util.List;

public class PlaybackManager {
    private static PlaybackManager instance;
    private MediaPlayer mediaPlayer;
    private IPlaybackQueue queue;
    private OnSongChangedListener listener;

    public interface OnSongChangedListener {
        void onSongChanged(Song newSong, boolean isNext);
    }

    private PlaybackManager() {
        this.queue = new ListPlaybackQueue();
    }

    public static synchronized PlaybackManager getInstance() {
        if (instance == null) instance = new PlaybackManager();
        return instance;
    }

    public void setOnSongChangedListener(OnSongChangedListener listener) {
        this.listener = listener;
    }

    public void initQueue(List<Song> songs, int startIndex) {
        queue.setQueue(songs, startIndex);
    }

    public void skipNext() {
        Song next = queue.getNext();
        if (next != null && listener != null) listener.onSongChanged(next, true);
    }

    public void skipPrevious() {
        Song prev = queue.getPrevious();
        if (prev != null && listener != null) listener.onSongChanged(prev, false);
    }

    public Song getCurrentSong() { return queue.getCurrent(); }

    public void play(String url, MediaPlayer.OnPreparedListener preparedListener) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA).build());
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(preparedListener);
        } catch (IOException e) {
            Log.e("PlaybackManager", "Error", e);
        }
    }

    public void togglePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.pause();
            else mediaPlayer.start();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }
}