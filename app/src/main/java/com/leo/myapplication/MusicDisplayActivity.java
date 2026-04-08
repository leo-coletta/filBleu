package com.leo.myapplication;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class MusicDisplayActivity extends AppCompatActivity {

    private TextView songTextView;
    private TextView artistTextView;
    private ImageView musicImageView;
    private ImageButton playPauseButton;
    private Song currentSong;
    private SeekBar seekBar;
    private TextView currentTimeText;
    private TextView totalTimeText;

    private Handler handler = new Handler();
    private Runnable updater;
    private PlaybackManager manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_display);

        // 1. Initialisation des vues
        songTextView = findViewById(R.id.music_name);
        artistTextView = findViewById(R.id.artist_name);
        musicImageView = findViewById(R.id.music_image);
        playPauseButton = findViewById(R.id.play_pause_button);
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

        homeButton.setOnClickListener(click -> {
            Intent intentH = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intentH);
        });
        // Boutons de navigation


        // 2. Initialisation du Manager (CORRECTION DU CRASH)
        // On utilise la variable de classe "manager" directement
        manager = PlaybackManager.getInstance();

        // 3. Configuration des écouteurs
        manager.setOnSongChangedListener((newSong, isNext) -> {
            runOnUiThread(() -> animateMusicChange(newSong, isNext));
        });

        nextButton.setOnClickListener(v -> manager.skipNext());
        previousButton.setOnClickListener(v -> manager.skipPrevious());

        // On utilise la méthode qui parle au CurrentSongManager
        setupPlayPauseLogic();

        // Navigation standard
        homeButton.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        searchButton.setOnClickListener(v -> startActivity(new Intent(this, ResearchActivity.class)));
        libraryButton.setOnClickListener(v -> startActivity(new Intent(this, LibraryActivity.class)));
        backButton.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        Song selectedSong = null;

        // On vérifie de façon sécurisée si une musique a été envoyée
        if (intent != null && intent.hasExtra("SONG_DATA")) {
            selectedSong = intent.getParcelableExtra("SONG_DATA");
        }

        Song currentlyPlaying = CurrentSongManager.getInstance().getCurrentSong();

        // CAS 1 : On a cliqué sur une NOUVELLE musique depuis une liste
        if (selectedSong != null && (currentlyPlaying == null || !currentlyPlaying.getId().equals(selectedSong.getId()))) {
            currentSong = selectedSong;

            // Mise à jour de l'affichage
            updateUI(currentSong);

            // On lance la musique et on configure la SeekBar
            CurrentSongManager.getInstance().playSong(currentSong, () -> {
                playPauseButton.setImageResource(R.drawable.pause);
                int totalDuration = CurrentSongManager.getInstance().getDuration();
                seekBar.setMax(totalDuration);
                totalTimeText.setText(createTimeLabel(totalDuration));
                updateSeekBar();
            });

            // (Si tu utilises PlaybackManager en parallèle pour les files d'attente,
            // assure-toi qu'il ne lance pas un 2ème MediaPlayer ici)
        }
        // CAS 2 : On ouvre via le mini-player (selectedSong == null) OU on clique sur la musique DÉJÀ en cours
        else if (currentlyPlaying != null) {
            currentSong = currentlyPlaying;

            // On restaure simplement l'affichage sans relancer la musique
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
        }

        // --- FIN DE LA CORRECTION ---
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && updater != null) {
            handler.removeCallbacks(updater);
        }
    }

    private void animateMusicChange(Song song, boolean isNext) {
        float exitDestination = isNext ? -1000f : 1000f;
        float exitTarget = isNext ? -1000f : 1000f;
        float entryStart = isNext ? 1000f : -1000f;

        // Sortie : Slide + Fade
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
                // Mise à jour pendant que l'image est invisible
                updateUI(song);
                startPlayback(song);

                musicImageView.setTranslationX(entryStart);

                // Entrée : Slide + Fade
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

    private void updateUI(Song song) {
        if (song == null) return;
        songTextView.setText(song.getTitle());
        artistTextView.setText(song.getArtist());
        if (song.getImageUrl() != null && !song.getImageUrl().isEmpty()) {
            Picasso.get().load(song.getImageUrl()).into(musicImageView);
        }
    }

    private void startPlayback(Song song) {
        if (song != null && song.getAudioUrl() != null) {
            // 1. Mise à jour de la musique en cours dans le Singleton global
            CurrentSongManager.getInstance().setCurrentSong(song);

            // 2. Lancement de la musique via CurrentSongManager pour que la SeekBar suive
            CurrentSongManager.getInstance().playSong(song, () -> {
                playPauseButton.setImageResource(R.drawable.pause);

                // Réinitialisation de la SeekBar pour la nouvelle musique
                int totalDuration = CurrentSongManager.getInstance().getDuration();
                seekBar.setMax(totalDuration);
                totalTimeText.setText(createTimeLabel(totalDuration));
                updateSeekBar();
            });
        }
    }

    private void updatePlayPauseIcon() {
        if (manager != null) {
            playPauseButton.setImageResource(manager.isPlaying() ? R.drawable.pause : R.drawable.play);
        }
    }
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

    private String createTimeLabel(int time) {
        String timeLabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeLabel = min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;
    }

    private void setupSeekBar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentTimeText.setText(createTimeLabel(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Optionnel : mettre en pause le rafraîchissement pendant que l'utilisateur glisse le doigt
                handler.removeCallbacks(updater);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Applique la nouvelle position au MediaPlayer
                CurrentSongManager.getInstance().seekTo(seekBar.getProgress());
                updateSeekBar();
            }
        });
    }

    private void updateSeekBar() {
        if (CurrentSongManager.getInstance().getCurrentSong() != null) {
            int currentPosition = CurrentSongManager.getInstance().getCurrentPosition();
            seekBar.setProgress(currentPosition);
            currentTimeText.setText(createTimeLabel(currentPosition));

            // Crée une boucle qui s'exécute toutes les 500 millisecondes
            updater = new Runnable() {
                @Override
                public void run() {
                    updateSeekBar();
                }
            };
            handler.postDelayed(updater, 500);
        }
    }

}