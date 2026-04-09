package com.leo.myapplication.controllers;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leo.myapplication.R;
import com.leo.myapplication.activities.MusicDisplayActivity;
import com.leo.myapplication.logic.CurrentSongManager;
import com.leo.myapplication.logic.PlaybackManager;
import com.leo.myapplication.models.Song;
import com.squareup.picasso.Picasso;

/**
 * Contrôleur dédié à la gestion de la logique et de l'interface utilisateur du mini lecteur
 * de musique persistant (MiniPlayer). Ce composant est conçu pour être attaché à n'importe
 * quelle activité possédant le layout adéquat pour assurer une continuité de la lecture
 * lors de la navigation.
 */
public class MiniPlayerController {

    private AppCompatActivity activity;
    private ConstraintLayout playerLayout;
    private TextView songNameText;
    private TextView artistNameText;
    private ImageView musicImage;
    private ImageButton playPauseButton;
    private ImageButton nextButton;
    private ImageButton backButton;
    private Button musicDisplayButton;

    /**
     * Construit un nouveau contrôleur pour le mini lecteur et l'initialise.
     *
     * @param activity L'activité parente contenant le layout du mini lecteur à contrôler.
     */
    public MiniPlayerController(AppCompatActivity activity) {
        this.activity = activity;
        initViews();
        setupListeners();
    }

    /**
     * Recherche, initialise et lie les composants visuels du mini lecteur (boutons, textes, images)
     * à partir de la hiérarchie des vues de l'activité parente.
     */
    private void initViews() {
        playerLayout = activity.findViewById(R.id.music_player);

        if (playerLayout == null) return;

        songNameText = activity.findViewById(R.id.music_name);
        artistNameText = activity.findViewById(R.id.artist_name);
        musicImage = activity.findViewById(R.id.music_image);
        playPauseButton = activity.findViewById(R.id.play_pause_button);
        nextButton = activity.findViewById(R.id.next_button);
        backButton = activity.findViewById(R.id.back_button);
        musicDisplayButton = activity.findViewById(R.id.music_display_button);
    }

    /**
     * Définit les comportements à adopter lors des interactions de l'utilisateur avec
     * les boutons de contrôle du mini lecteur (lecture, pause, navigation inter-pistes
     * via la file d'attente globale) et l'ouverture de l'écran de lecture détaillé.
     */
    private void setupListeners() {
        if (playerLayout == null) return;

        musicDisplayButton.setOnClickListener(v -> {
            Song current = CurrentSongManager.getInstance().getCurrentSong();
            if (current != null) {
                Intent intent = new Intent(activity, MusicDisplayActivity.class);
                intent.putExtra("SONG_DATA", current);
                activity.startActivity(intent);
            }
        });

        playPauseButton.setOnClickListener(v -> {
            if (CurrentSongManager.getInstance().isPlaying()) {
                CurrentSongManager.getInstance().pause();
                playPauseButton.setBackgroundResource(R.drawable.play);
            } else {
                CurrentSongManager.getInstance().resume();
                playPauseButton.setBackgroundResource(R.drawable.pause);
            }
        });

        nextButton.setOnClickListener(v -> {
            PlaybackManager.getInstance().skipNext();
            Song next = PlaybackManager.getInstance().getCurrentSong();
            if (next != null) {
                CurrentSongManager.getInstance().playSong(next, () -> updateUI());
            }
        });

        backButton.setOnClickListener(v -> {
            PlaybackManager.getInstance().skipPrevious();
            Song prev = PlaybackManager.getInstance().getCurrentSong();
            if (prev != null) {
                CurrentSongManager.getInstance().playSong(prev, () -> updateUI());
            }
        });
    }

    /**
     * Synchronise dynamiquement l'interface utilisateur du mini lecteur (titre, nom de l'artiste,
     * pochette de l'album, état du bouton lecture/pause) avec l'état en temps réel du gestionnaire
     * de lecture (CurrentSongManager). Masque entièrement le conteneur si aucune piste n'est chargée.
     */
    public void updateUI() {
        if (playerLayout == null) return;

        Song current = CurrentSongManager.getInstance().getCurrentSong();

        if (current != null) {
            playerLayout.setVisibility(View.VISIBLE);
            songNameText.setText(current.getTitle());
            artistNameText.setText(current.getArtist());

            if (current.getImageUrl() != null && !current.getImageUrl().isEmpty()) {
                Picasso.get().load(current.getImageUrl()).into(musicImage);
            }

            if (CurrentSongManager.getInstance().isPlaying()) {
                playPauseButton.setBackgroundResource(R.drawable.pause);
            } else {
                playPauseButton.setBackgroundResource(R.drawable.play);
            }
        } else {
            playerLayout.setVisibility(View.GONE);
        }
    }
}