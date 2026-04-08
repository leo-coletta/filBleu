package com.leo.myapplication;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_display);

        ImageButton searchButton = findViewById(R.id.search_button);
        ImageButton libraryButton = findViewById(R.id.playlists_button);
        ImageButton homeButton = findViewById(R.id.home_button);
        ImageButton backButton = findViewById(R.id.back_page_button);
        songTextView = findViewById(R.id.music_name);
        artistTextView = findViewById(R.id.artist_name);
        musicImageView = findViewById(R.id.music_image);

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

        Intent intent = getIntent();

        if (intent != null && intent.hasExtra("SONG_DATA")) {
            currentSong = intent.getParcelableExtra("SONG_DATA");

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
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
        } catch (IOException e) {
            Log.e("AudioError", "Erreur de chargement du flux audio", e);
        }
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