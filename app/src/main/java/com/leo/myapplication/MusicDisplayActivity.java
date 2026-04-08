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
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicDisplayActivity extends AppCompatActivity {

    private TextView songTextView;
    private TextView artistTextView;
    private ImageView musicImageView;
    private Song currentSong;
    private MediaPlayer mediaPlayer;
    private ImageButton playPauseButton;
    private ImageButton likeButton;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_display);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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
        likeButton = findViewById(R.id.like_button);

        homeButton.setOnClickListener(click -> {
            Intent intentH = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intentH);
        });

        searchButton.setOnClickListener(click -> {
            Intent intentS = new Intent(getApplicationContext(), ResearchActivity.class);
            startActivity(intentS);
        });

        libraryButton.setOnClickListener(click -> {
            Intent intentL = new Intent(getApplicationContext(), LibraryActivity.class);
            startActivity(intentL);
        });

        backButton.setOnClickListener(click -> finish());

        nextButton.setOnClickListener(click -> animateMusicChange(R.drawable.music_image_placeholder, true));

        previousButton.setOnClickListener(click -> animateMusicChange(R.drawable.music_image_placeholder, false));

        Intent intent = getIntent();

        if (intent != null && intent.hasExtra("SONG_DATA")) {
            currentSong = intent.getParcelableExtra("SONG_DATA");
            CurrentSongManager.getInstance().setCurrentSong(currentSong);

            if (currentSong != null) {
                songTextView.setText(currentSong.getTitle());
                artistTextView.setText(currentSong.getArtist());

                if (currentSong.getImageUrl() != null && !currentSong.getImageUrl().isEmpty()) {
                    Picasso.get().load(currentSong.getImageUrl()).into(musicImageView);
                }

                if (currentSong.getAudioUrl() != null && !currentSong.getAudioUrl().isEmpty()) {
                    initMediaPlayer(currentSong.getAudioUrl());
                }

                checkIfLiked();
            }
        }

        setupPlayPauseLogic();

        likeButton.setOnClickListener(v -> toggleLikeAndShowMenu());
    }

    private void checkIfLiked() {
        if (auth.getCurrentUser() == null || currentSong == null) return;

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

    private void toggleLikeAndShowMenu() {
        if (auth.getCurrentUser() == null || currentSong == null) return;
        String uid = auth.getCurrentUser().getUid();
        String songId = currentSong.getId();

        DocumentReference likedRef = db.collection("users").document(uid).collection("playlists").document("liked_songs");

        likedRef.get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                // Création auto de la playlist par défaut
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

    private void showPlaylistsMenu(String songId) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.dialog_playlists);
        LinearLayout container = dialog.findViewById(R.id.playlists_container);

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

                                // Ajoute l'image si la playlist n'en a pas et n'est pas liked_songs
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