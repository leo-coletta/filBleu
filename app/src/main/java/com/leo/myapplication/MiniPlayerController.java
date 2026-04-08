package com.leo.myapplication;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.squareup.picasso.Picasso;

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

    public MiniPlayerController(AppCompatActivity activity) {
        this.activity = activity;
        initViews();
        setupListeners();
    }

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

        // Activation des boutons suivant et précédent
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