package com.leo.myapplication.activities;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.leo.myapplication.logic.CurrentSongManager;
import com.leo.myapplication.logic.PlaybackManager;
import com.leo.myapplication.R;
import com.leo.myapplication.models.Song;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activité dédiée à l'affichage immersif et détaillé de la musique en cours de lecture.
 * Elle fournit à l'utilisateur des contrôles avancés de lecture, un retour visuel sur la
 * progression via une SeekBar temporelle, ainsi que des interactions avec le système de
 * favoris et la gestion avancée des playlists intégrées à Firebase.
 */
public class MusicDisplayActivity extends AppCompatActivity {

    private TextView songTextView;
    private TextView artistTextView;
    private ImageView musicImageView;
    private ImageButton playPauseButton;
    private ImageButton likeButton;
    private Song currentSong;
    private SeekBar seekBar;
    private TextView currentTimeText;
    private TextView totalTimeText;

    private Handler handler = new Handler();
    private Runnable updater;
    private PlaybackManager manager;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    /**
     * Phase d'initialisation du cycle de vie de l'activité. Configure la vue, instancie
     * les services Firebase, lie les éléments XML au code, met en place les écouteurs
     * temporels de la SeekBar et écoute les changements d'état du gestionnaire de lecture.
     *
     * @param savedInstanceState L'état de l'activité préalablement sauvegardé, s'il existe.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_display);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        songTextView = findViewById(R.id.music_name);
        artistTextView = findViewById(R.id.artist_name);
        musicImageView = findViewById(R.id.music_image);
        playPauseButton = findViewById(R.id.play_pause_button);
        likeButton = findViewById(R.id.like_button);
        ImageButton nextButton = findViewById(R.id.next_button);
        ImageButton previousButton = findViewById(R.id.back_button);
        ImageButton searchButton = findViewById(R.id.search_button);
        ImageButton libraryButton = findViewById(R.id.playlists_button);
        ImageButton homeButton = findViewById(R.id.home_button);
        ImageButton backButton = findViewById(R.id.back_page_button);

        seekBar = findViewById(R.id.music_timebar);
        currentTimeText = findViewById(R.id.music_time_played);
        totalTimeText = findViewById(R.id.music_time_remaining);

        setupSeekBar();
        setupPlayPauseLogic();

        manager = PlaybackManager.getInstance();
        manager.setOnSongChangedListener((newSong, isNext) -> {
            runOnUiThread(() -> animateMusicChange(newSong, isNext));
        });

        nextButton.setOnClickListener(v -> manager.skipNext());
        previousButton.setOnClickListener(v -> manager.skipPrevious());
        likeButton.setOnClickListener(v -> toggleLikeAndShowMenu());

        homeButton.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        searchButton.setOnClickListener(v -> startActivity(new Intent(this, ResearchActivity.class)));
        libraryButton.setOnClickListener(v -> startActivity(new Intent(this, LibraryActivity.class)));
        backButton.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        Song selectedSong = null;

        if (intent != null && intent.hasExtra("SONG_DATA")) {
            selectedSong = intent.getParcelableExtra("SONG_DATA");
        }

        Song currentlyPlaying = CurrentSongManager.getInstance().getCurrentSong();

        if (selectedSong != null && (currentlyPlaying == null || !currentlyPlaying.getId().equals(selectedSong.getId()))) {
            currentSong = selectedSong;
            updateUI(currentSong);

            CurrentSongManager.getInstance().playSong(currentSong, () -> {
                playPauseButton.setImageResource(R.drawable.pause);
                int totalDuration = CurrentSongManager.getInstance().getDuration();
                seekBar.setMax(totalDuration);
                totalTimeText.setText(createTimeLabel(totalDuration));
                updateSeekBar();
            });
            checkIfLiked();

        } else if (currentlyPlaying != null) {
            currentSong = currentlyPlaying;
            updateUI(currentSong);

            int totalDuration = CurrentSongManager.getInstance().getDuration();
            seekBar.setMax(totalDuration);
            totalTimeText.setText(createTimeLabel(totalDuration));
            updateSeekBar();

            if (CurrentSongManager.getInstance().isPlaying()) {
                playPauseButton.setImageResource(R.drawable.pause);
            } else {
                playPauseButton.setImageResource(R.drawable.play);
            }
            checkIfLiked();
        }
    }

    /**
     * Intercepte la destruction de l'activité pour garantir l'arrêt complet des tâches
     * planifiées en arrière-plan, empêchant ainsi les fuites de mémoire liées à la SeekBar.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && updater != null) {
            handler.removeCallbacks(updater);
        }
    }

    /**
     * Orchestre l'animation de transition de la pochette de l'album lors du passage à une
     * autre piste. Combine une animation de translation hors-champ et un fondu asynchrone
     * avant d'introduire la nouvelle image depuis le côté opposé.
     *
     * @param song L'objet Song contenant les détails de la nouvelle musique à afficher.
     * @param isNext Booléen déterminant la direction du glissement (true = suivant, false = précédent).
     */
    private void animateMusicChange(Song song, boolean isNext) {
        float exitTarget = isNext ? -1000f : 1000f;
        float entryStart = isNext ? 1000f : -1000f;

        ObjectAnimator slideOut = (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.slide);
        slideOut.setTarget(musicImageView);
        slideOut.setFloatValues(0f, exitTarget);

        ObjectAnimator fadeOut = (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.fade);
        fadeOut.setTarget(musicImageView);
        fadeOut.setFloatValues(1f, 0f);

        AnimatorSet animOut = new AnimatorSet();
        animOut.playTogether(slideOut, fadeOut);

        animOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                updateUI(song);
                startPlayback(song);

                musicImageView.setTranslationX(entryStart);

                ObjectAnimator slideIn = (ObjectAnimator) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.slide);
                slideIn.setTarget(musicImageView);
                slideIn.setFloatValues(entryStart, 0f);

                ObjectAnimator fadeIn = (ObjectAnimator) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.fade);
                fadeIn.setTarget(musicImageView);
                fadeIn.setFloatValues(0f, 1f);

                AnimatorSet animIn = new AnimatorSet();
                animIn.playTogether(slideIn, fadeIn);
                animIn.start();
            }
        });
        animOut.start();
    }

    /**
     * Met à jour les éléments textuels et graphiques de la vue avec les données
     * fraîchement chargées d'une musique spécifique, et relance la vérification des favoris.
     *
     * @param song La musique dont les métadonnées doivent être affichées à l'écran.
     */
    private void updateUI(Song song) {
        if (song == null) return;
        currentSong = song;
        songTextView.setText(song.getTitle());
        artistTextView.setText(song.getArtist());
        if (song.getImageUrl() != null && !song.getImageUrl().isEmpty()) {
            Picasso.get().load(song.getImageUrl()).into(musicImageView);
        }
        checkIfLiked();
    }

    /**
     * Transmet l'instruction de lecture de la musique actuelle au gestionnaire audio
     * global et s'assure que la durée de la SeekBar correspond au flux audio reçu.
     *
     * @param song La piste musicale cible contenant l'URL audio valide.
     */
    private void startPlayback(Song song) {
        if (song != null && song.getAudioUrl() != null) {
            CurrentSongManager.getInstance().setCurrentSong(song);
            CurrentSongManager.getInstance().playSong(song, () -> {
                playPauseButton.setImageResource(R.drawable.pause);
                int totalDuration = CurrentSongManager.getInstance().getDuration();
                seekBar.setMax(totalDuration);
                totalTimeText.setText(createTimeLabel(totalDuration));
                updateSeekBar();
            });
        }
    }

    /**
     * Paramètre l'action principale du bouton central de contrôle pour alterner en
     * temps réel entre les états de lecture active et de pause.
     */
    private void setupPlayPauseLogic() {
        playPauseButton.setOnClickListener(v -> {
            if (CurrentSongManager.getInstance().isPlaying()) {
                CurrentSongManager.getInstance().pause();
                playPauseButton.setImageResource(R.drawable.play);
            } else {
                CurrentSongManager.getInstance().resume();
                playPauseButton.setImageResource(R.drawable.pause);
            }
        });
    }

    /**
     * Fonction utilitaire convertissant un temps absolu fourni par le lecteur audio
     * en une chaîne de caractères formatée en minutes et secondes (MM:SS).
     *
     * @param time Le temps total évalué en millisecondes.
     * @return La représentation textuelle du temps pour l'affichage utilisateur.
     */
    private String createTimeLabel(int time) {
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;
        String timeLabel = min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;
        return timeLabel;
    }

    /**
     * Établit les interfaces d'écoute pour la SeekBar afin de gérer le déplacement manuel
     * du curseur de progression par l'utilisateur, en interrompant temporairement le flux
     * asynchrone lors du toucher.
     */
    private void setupSeekBar() {
        if(seekBar == null) return;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && currentTimeText != null) {
                    currentTimeText.setText(createTimeLabel(progress));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(updater);
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                CurrentSongManager.getInstance().seekTo(seekBar.getProgress());
                updateSeekBar();
            }
        });
    }

    /**
     * Lance un processus récurrent posté sur le thread principal pour lire la position
     * actuelle de la musique dans le gestionnaire et actualiser visuellement la SeekBar.
     */
    private void updateSeekBar() {
        if (CurrentSongManager.getInstance().getCurrentSong() != null && seekBar != null) {
            int currentPosition = CurrentSongManager.getInstance().getCurrentPosition();
            seekBar.setProgress(currentPosition);
            if(currentTimeText != null) {
                currentTimeText.setText(createTimeLabel(currentPosition));
            }
            updater = this::updateSeekBar;
            handler.postDelayed(updater, 500);
        }
    }

    /**
     * Interroge de manière asynchrone la base de données Firestore de l'utilisateur connecté
     * pour savoir si la musique affichée réside actuellement dans sa playlist système des favoris.
     */
    private void checkIfLiked() {
        if (auth.getCurrentUser() == null || currentSong == null) return;
        likeButton.setBackgroundResource(R.drawable.heart);
        db.collection("users").document(auth.getCurrentUser().getUid())
                .collection("playlists").document("liked_songs").get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<String> songIds = (List<String>) doc.get("songIds");
                        if (songIds != null && songIds.contains(currentSong.getId())) {
                            likeButton.setBackgroundResource(R.drawable.heart_full);
                        }
                    }
                });
    }

    /**
     * Agit comme le déclencheur principal du bouton "Cœur". Si la piste n'a jamais été likée,
     * elle l'ajoute immédiatement à la playlist "Titres likés". Si elle l'est déjà, elle déploie
     * un menu étendu pour une gestion sélective au sein des multiples playlists personnalisées.
     */
    private void toggleLikeAndShowMenu() {
        if (auth.getCurrentUser() == null || currentSong == null) return;
        String uid = auth.getCurrentUser().getUid();
        String songId = currentSong.getId();

        DocumentReference likedRef = db.collection("users").document(uid).collection("playlists").document("liked_songs");

        likedRef.get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                Map<String, Object> data = new HashMap<>();
                data.put("name", "Titres likés");
                data.put("songIds", Arrays.asList(songId));
                likedRef.set(data);
                likeButton.setBackgroundResource(R.drawable.heart_full);
            } else {
                List<String> songIds = (List<String>) doc.get("songIds");
                if (songIds != null && songIds.contains(songId)) {
                    showPlaylistsMenu(songId);
                } else {
                    likedRef.update("songIds", FieldValue.arrayUnion(songId));
                    likeButton.setBackgroundResource(R.drawable.heart_full);
                }
            }
        });
    }

    /**
     * Construit et affiche une interface superposée (BottomSheetDialog) récupérant en temps
     * réel toutes les playlists Firestore de l'utilisateur. Chaque option permet d'ajouter
     * ou de soustraire la musique ciblée via une mise à jour atomique de la base de données.
     *
     * @param songId L'identifiant unique de la musique servant de référence pour Firestore.
     */
    private void showPlaylistsMenu(String songId) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.dialog_playlists);
        LinearLayout container = dialog.findViewById(R.id.playlists_container);

        if(container == null) return;

        db.collection("users").document(auth.getCurrentUser().getUid())
                .collection("playlists").get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        CheckBox cb = new CheckBox(this);
                        cb.setText(doc.getString("name"));
                        cb.setTextColor(getResources().getColor(R.color.white));

                        List<String> songs = (List<String>) doc.get("songIds");
                        cb.setChecked(songs != null && songs.contains(songId));

                        cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked) {
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("songIds", FieldValue.arrayUnion(songId));

                                if (!doc.getId().equals("liked_songs") && (doc.getString("imageUrl") == null || doc.getString("imageUrl").isEmpty())) {
                                    updates.put("imageUrl", currentSong.getImageUrl());
                                }

                                doc.getReference().update(updates);
                            } else {
                                doc.getReference().update("songIds", FieldValue.arrayRemove(songId));
                                if ("liked_songs".equals(doc.getId())) {
                                    likeButton.setBackgroundResource(R.drawable.heart);
                                }
                            }
                        });
                        container.addView(cb);
                    }
                });

        dialog.show();
    }
}