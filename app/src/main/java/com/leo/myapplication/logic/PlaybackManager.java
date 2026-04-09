package com.leo.myapplication.logic;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;

import com.leo.myapplication.models.Song;

import java.io.IOException;
import java.util.List;

/**
 * Gestionnaire Singleton responsable du lecteur multimédia et de la file d'attente globale.
 * <p>
 * Pilote le {@link MediaPlayer} natif d'Android et communique avec l'interface graphique
 * via l'interface {@link OnSongChangedListener}.
 * </p>
 */
public class PlaybackManager {
    private static PlaybackManager instance;
    private MediaPlayer mediaPlayer;
    private IPlaybackQueue queue;
    private OnSongChangedListener listener;

    /**
     * Interface d'écoute pour réagir aux changements de morceau.
     */
    public interface OnSongChangedListener {
        /**
         * Appelée lorsque le morceau actuel change.
         *
         * @param newSong Le nouveau morceau à lire.
         * @param isNext  {@code true} s'il s'agit du morceau suivant, {@code false} pour le précédent.
         */
        void onSongChanged(Song newSong, boolean isNext);
    }

    private PlaybackManager() {
        this.queue = new ListPlaybackQueue();
    }

    /**
     * Récupère l'instance unique du gestionnaire de lecture.
     *
     * @return L'instance Singleton {@link PlaybackManager}.
     */
    public static synchronized PlaybackManager getInstance() {
        if (instance == null) instance = new PlaybackManager();
        return instance;
    }

    /**
     * Définit l'écouteur qui sera notifié lors du changement de piste.
     *
     * @param listener L'instance implémentant {@link OnSongChangedListener}.
     */
    public void setOnSongChangedListener(OnSongChangedListener listener) {
        this.listener = listener;
    }

    /**
     * Initialise la file d'attente avec une nouvelle liste de chansons.
     *
     * @param songs      La liste des chansons à lire.
     * @param startIndex L'index de la chanson par laquelle commencer.
     */
    public void initQueue(List<Song> songs, int startIndex) {
        queue.setQueue(songs, startIndex);
    }

    /**
     * Passe au morceau suivant dans la file d'attente et notifie les écouteurs.
     */
    public void skipNext() {
        Song next = queue.getNext();
        if (next != null && listener != null) {
            listener.onSongChanged(next, true);
        }
    }

    /**
     * Revient au morceau précédent dans la file d'attente et notifie les écouteurs.
     */
    public void skipPrevious() {
        Song prev = queue.getPrevious();
        if (prev != null && listener != null) {
            listener.onSongChanged(prev, false);
        }
    }

    /**
     * Prépare et lance la lecture audio depuis une URL distante.
     *
     * @param url              L'URL du fichier audio.
     * @param preparedListener Callback déclenché lorsque le média est prêt à être joué.
     */
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
            Log.e("PlaybackManager", "Erreur audio", e);
        }
    }

    /**
     * Alterne entre les états "Lecture" et "Pause".
     */
    public void togglePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.pause();
            else mediaPlayer.start();
        }
    }

    /**
     * Vérifie si un média est actuellement en cours de lecture.
     *
     * @return {@code true} si l'audio joue, {@code false} sinon.
     */
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    /**
     * Récupère le morceau actuellement défini dans la file d'attente.
     *
     * @return L'objet {@link Song} en cours.
     */
    public Song getCurrentSong() { return queue.getCurrent(); }
}