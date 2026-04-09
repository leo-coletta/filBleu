package com.leo.myapplication.logic;

import android.media.AudioAttributes;
import android.media.MediaPlayer;

import com.leo.myapplication.models.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire Singleton responsable de la lecture d'une piste audio unique et de l'historique d'écoute.
 * <p>
 * Pilote un {@link MediaPlayer} et maintient automatiquement une liste des 10 dernières musiques écoutées.
 * </p>
 */
public class CurrentSongManager {
    private static CurrentSongManager instance;
    private Song currentSong;
    private MediaPlayer mediaPlayer;
    private List<Song> recentSongs = new ArrayList<>();

    private CurrentSongManager() {}

    /**
     * Récupère l'instance unique du gestionnaire.
     *
     * @return L'instance {@link CurrentSongManager}.
     */
    public static synchronized CurrentSongManager getInstance() {
        if (instance == null) {
            instance = new CurrentSongManager();
        }
        return instance;
    }

    /** @return La musique actuellement chargée ou en cours de lecture. */
    public Song getCurrentSong() { return currentSong; }

    /** @return La liste des musiques récemment écoutées (limite de 10). */
    public List<Song> getRecentSongs() { return recentSongs; }

    /**
     * Prépare et lance la lecture d'une nouvelle musique.
     * Met à jour l'historique des musiques récentes en évitant les doublons.
     *
     * @param song               La musique à lire.
     * @param onPreparedCallback Action à exécuter une fois le média prêt (ex: mise à jour de l'UI).
     */
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

        if (recentSongs.size() > 3) {
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

    /** Met en pause la lecture en cours. */
    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    /** Reprend la lecture si elle était en pause. */
    public void resume() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    /** @return {@code true} si la musique est en cours de lecture, {@code false} sinon. */
    public boolean isPlaying() { return mediaPlayer != null && mediaPlayer.isPlaying(); }

    /** @return La durée totale de la musique en millisecondes. */
    public int getDuration() { return mediaPlayer != null ? mediaPlayer.getDuration() : 0; }

    /** @return La position actuelle de lecture en millisecondes. */
    public int getCurrentPosition() { return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0; }

    /**
     * Déplace la tête de lecture à une position spécifique.
     *
     * @param position La nouvelle position en millisecondes.
     */
    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    /** @param song Définit manuellement la musique actuelle sans la lancer. */
    public void setCurrentSong(Song song) { this.currentSong = song; }
}