package com.leo.myapplication;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import com.squareup.picasso.Picasso;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Handler;
import android.widget.SeekBar;
import androidx.appcompat.app.AppCompatActivity;


import java.io.IOException;

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

        seekBar = findViewById(R.id.music_timebar);
        currentTimeText = findViewById(R.id.music_time_played);
        totalTimeText = findViewById(R.id.music_time_remaining);

        setupSeekBar();

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
            Song selectedSong = intent.getParcelableExtra("SONG_DATA");
            Song currentlyPlaying = CurrentSongManager.getInstance().getCurrentSong();
            if (currentlyPlaying == null || !currentlyPlaying.getId().equals(selectedSong.getId())) {
                currentSong = selectedSong;
                CurrentSongManager.getInstance().playSong(currentSong, () -> {
                    // Callback exécuté quand la musique est PRÊTE
                    playPauseButton.setImageResource(R.drawable.pause);

                    // Initialisation de la durée max de la SeekBar
                    int totalDuration = CurrentSongManager.getInstance().getDuration();
                    seekBar.setMax(totalDuration);
                    totalTimeText.setText(createTimeLabel(totalDuration));

                    // Démarrage du rafraîchissement
                    updateSeekBar();
                });
            } else {
                // Musique DÉJÀ en cours : on restaure l'interface immédiatement
                currentSong = currentlyPlaying;
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

            songTextView.setText(currentSong.getTitle());
            artistTextView.setText(currentSong.getArtist());
            if (currentSong.getImageUrl() != null && !currentSong.getImageUrl().isEmpty()) {
                Picasso.get().load(currentSong.getImageUrl()).into(musicImageView);
            }
        }

        setupPlayPauseLogic();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && updater != null) {
            handler.removeCallbacks(updater);
        }
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