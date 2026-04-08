package com.leo.myapplication;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CurrentSongManager {
    private static CurrentSongManager instance;
    private Song currentSong;
    private MediaPlayer mediaPlayer;
    private List<Song> recentSongs = new ArrayList<>();

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

    public List<Song> getRecentSongs() {
        return recentSongs;
    }

    public void playSong(Song song, Runnable onPreparedCallback) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        this.currentSong = song;

        if (song.getTitle() != null) {
            recentSongs.removeIf(s -> s.getTitle().equals(song.getTitle()));
        }

        recentSongs.add(0, song);

        if (recentSongs.size() > 10) {
            recentSongs.remove(recentSongs.size() - 1);
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        try {
            mediaPlayer.setDataSource(song.getAudioUrl());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                if (onPreparedCallback != null) {
                    onPreparedCallback.run();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void resume() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }
}