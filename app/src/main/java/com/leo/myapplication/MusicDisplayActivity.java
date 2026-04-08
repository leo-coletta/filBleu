package com.leo.myapplication;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Picasso;

public class MusicDisplayActivity extends AppCompatActivity {

    private TextView songTextView;
    private TextView artistTextView;
    private ImageView musicImageView;
    private ImageButton playPauseButton;
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

        // Boutons de navigation
        ImageButton searchButton = findViewById(R.id.search_button);
        ImageButton libraryButton = findViewById(R.id.playlists_button);
        ImageButton homeButton = findViewById(R.id.home_button);
        ImageButton backButton = findViewById(R.id.back_page_button);

        // 2. Initialisation du Manager (CORRECTION DU CRASH)
        // On utilise la variable de classe "manager" directement
        manager = PlaybackManager.getInstance();

        // 3. Configuration des écouteurs
        manager.setOnSongChangedListener((newSong, isNext) -> {
            runOnUiThread(() -> animateMusicChange(newSong, isNext));
        });

        nextButton.setOnClickListener(v -> manager.skipNext());
        previousButton.setOnClickListener(v -> manager.skipPrevious());

        playPauseButton.setOnClickListener(v -> {
            manager.togglePlayPause();
            updatePlayPauseIcon();
        });

        // Navigation standard
        homeButton.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        searchButton.setOnClickListener(v -> startActivity(new Intent(this, ResearchActivity.class)));
        libraryButton.setOnClickListener(v -> startActivity(new Intent(this, LibraryActivity.class)));
        backButton.setOnClickListener(v -> finish());

        // 4. Gestion de la musique entrante (Intent)
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("SONG_DATA")) {
            Song incomingSong = intent.getParcelableExtra("SONG_DATA");
            if (incomingSong != null) {
                CurrentSongManager.getInstance().setCurrentSong(incomingSong);
                updateUI(incomingSong);
                startPlayback(incomingSong);
            }
        } else {
            // Si on ouvre la vue sans intent, on affiche ce qui joue déjà
            Song current = manager.getCurrentSong();
            if (current != null) {
                updateUI(current);
                updatePlayPauseIcon();
            }
        }
    }

    private void animateMusicChange(Song song, boolean isNext) {
        if (song == null) return;

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
            manager.play(song.getAudioUrl(), mp -> {
                mp.start();
                updatePlayPauseIcon();
            });
        }
    }

    private void updatePlayPauseIcon() {
        if (manager != null) {
            playPauseButton.setImageResource(manager.isPlaying() ? R.drawable.pause : R.drawable.play);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // On ne release pas le manager ici car c'est un singleton qui doit continuer à jouer en background
    }
}