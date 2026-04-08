package com.leo.myapplication;

import android.media.MediaPlayer;
import java.util.List;

public class PlaybackManager {
    private static PlaybackManager instance;
    private MediaPlayer mediaPlayer;
    private IPlaybackQueue queue;
    private PlaybackListener listener;

    public interface PlaybackListener {
        void onSongChanged(Song newSong, boolean isNext);
    }

    private PlaybackManager() {
        queue = new ListPlaybackQueue(); // Source actuelle (liste DB)
    }

    public static synchronized PlaybackManager getInstance() {
        if (instance == null) instance = new PlaybackManager();
        return instance;
    }

    public void setListener(PlaybackListener listener) { this.listener = listener; }

    public void initQueue(List<Song> songs, int startIndex) {
        queue.setQueue(songs, startIndex);
    }

    public void skipNext() {
        Song next = queue.getNext();
        if (next != null && listener != null) {
            listener.onSongChanged(next, true);
        }
    }

    public void skipPrevious() {
        // Logique Spotify : si on a dépassé 3s, on redémarre la chanson
        // Sinon, on passe à la précédente
        Song prev = queue.getPrevious();
        if (prev != null && listener != null) {
            listener.onSongChanged(prev, false);
        }
    }

    public Song getCurrentSong() { return queue.getCurrent(); }
}