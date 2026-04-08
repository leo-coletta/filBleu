package com.leo.myapplication;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import com.squareup.picasso.Picasso;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import java.io.IOException;

public class MusicDisplayActivity extends AppCompatActivity {

    private TextView songTextView;
    private TextView artistTextView;
    private ImageView musicImageView;
    private Song currentSong;
    private MediaPlayer mediaPlayer;
    private ImageButton playPauseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_display);

        ImageButton searchButton = findViewById(R.id.search_button);
        ImageButton libraryButton = findViewById(R.id.playlists_button);
        ImageButton homeButton = findViewById(R.id.home_button);
        ImageButton backButton = findViewById(R.id.back_page_button);

        ImageButton nextButton = findViewById(R.id.next_button);
        ImageButton previousButton = findViewById(R.id.back_button);
        songTextView = findViewById(R.id.music_name);
        artistTextView = findViewById(R.id.artist_name);
        musicImageView = findViewById(R.id.music_image);
        playPauseButton = findViewById(R.id.play_pause_button);

        homeButton.setOnClickListener( click -> {
            Intent intentH = new Intent( getApplicationContext(), MainActivity.class);
            startActivity(intentH);
        });

        searchButton.setOnClickListener( click -> {
            Intent intentS = new Intent( getApplicationContext(), ResearchActivity.class);
            startActivity(intentS);
        });

        libraryButton.setOnClickListener( click -> {
            Intent intentL = new Intent( getApplicationContext(), LibraryActivity.class);
            startActivity(intentL);
        });

        backButton.setOnClickListener( click -> {
            finish();
        });

        nextButton.setOnClickListener(click -> {
            animateMusicChange(R.drawable.music_image_placeholder, true);
        });

        previousButton.setOnClickListener(click -> {
            animateMusicChange(R.drawable.music_image_placeholder, false);
        });

        Intent intent = getIntent();

        if (intent != null && intent.hasExtra("SONG_DATA")) {
            currentSong = intent.getParcelableExtra("SONG_DATA");

            CurrentSongManager.getInstance().setCurrentSong(currentSong);

            // Mise à jour de l'interface utilisateur
            if (currentSong != null) {
                songTextView.setText(currentSong.getTitle());
                artistTextView.setText(currentSong.getArtist());

                if (currentSong.getImageUrl() != null && !currentSong.getImageUrl().isEmpty()) {
                    Picasso.get().load(currentSong.getImageUrl()).into(musicImageView);
                }

                if (currentSong.getAudioUrl() != null && !currentSong.getAudioUrl().isEmpty()) {
                    initMediaPlayer(currentSong.getAudioUrl());
                }
            }
        }

        setupPlayPauseLogic();

    }

    private void animateMusicChange(int newImageResource, boolean isNext) {
        float exitDestination = isNext ? -1000f : 1000f;
        float entryStart = isNext ? 1000f : -1000f;

        ObjectAnimator slideOut = (ObjectAnimator) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.slide);
        slideOut.setTarget(musicImageView);
        slideOut.setFloatValues(0f, exitDestination);

        ObjectAnimator fadeOut = (ObjectAnimator) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.fade);
        fadeOut.setTarget(musicImageView);
        fadeOut.setFloatValues(1f, 0f);

        AnimatorSet animOut = new AnimatorSet();
        animOut.playTogether(slideOut, fadeOut);

        animOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                musicImageView.setImageResource(newImageResource);

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

    private void initMediaPlayer(String url) {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                if (playPauseButton != null) {
                    playPauseButton.setImageResource(R.drawable.pause);
                }
            });
        } catch (IOException e) {
            Log.e("AudioError", "Erreur de chargement du flux audio", e);
        }
    }

    private void setupPlayPauseLogic() {
        playPauseButton.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playPauseButton.setImageResource(R.drawable.play);
                } else {
                    mediaPlayer.start();
                    playPauseButton.setImageResource(R.drawable.pause);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}