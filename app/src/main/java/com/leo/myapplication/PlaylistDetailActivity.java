package com.leo.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PlaylistDetailActivity extends AppCompatActivity {

    private Playlist currentPlaylist;
    private RecyclerView songsRecyclerView;
    private SongAdapter songAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<Song> loadedSongs = new ArrayList<>();
    private MiniPlayerController miniPlayerController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        ImageButton backButton = findViewById(R.id.back_button);
        TextView playlistNameText = findViewById(R.id.playlist_name);
        ImageView playlistImage = findViewById(R.id.playlist_image);
        songsRecyclerView = findViewById(R.id.songs_recycler_view);

        miniPlayerController = new MiniPlayerController(this);

        backButton.setOnClickListener(v -> finish());

        songsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        songAdapter = new SongAdapter();
        songAdapter.setShowFullHearts(true);
        songsRecyclerView.setAdapter(songAdapter);

        songAdapter.setOnRemoveSongListener(song -> removeSongFromPlaylist(song));

        songAdapter.setOnSongClickListener(song -> {
            int index = loadedSongs.indexOf(song);

            PlaybackManager.getInstance().initQueue(new ArrayList<>(loadedSongs), index != -1 ? index : 0);
            CurrentSongManager.getInstance().playSong(song, () -> miniPlayerController.updateUI());

            Intent intent = new Intent(PlaylistDetailActivity.this, MusicDisplayActivity.class);
            intent.putExtra("SONG_DATA", song);
            startActivity(intent);
        });

        currentPlaylist = getIntent().getParcelableExtra("PLAYLIST_DATA");

        if (currentPlaylist != null) {
            playlistNameText.setText(currentPlaylist.getName());

            if ("liked_songs".equals(currentPlaylist.getId())) {
                playlistImage.setImageResource(R.drawable.heart_full);
            } else if (currentPlaylist.getImageUrl() != null && !currentPlaylist.getImageUrl().isEmpty()) {
                Picasso.get().load(currentPlaylist.getImageUrl()).into(playlistImage);
            } else {
                playlistImage.setImageResource(R.drawable.playlist_icon);
            }

            fetchSongsForPlaylist();
        }
    }

    private void fetchSongsForPlaylist() {
        if (currentPlaylist.getSongIds() == null || currentPlaylist.getSongIds().isEmpty()) {
            Toast.makeText(this, "Cette playlist est vide", Toast.LENGTH_SHORT).show();
            loadedSongs.clear();
            songAdapter.setSongList(loadedSongs);
            return;
        }

        loadedSongs.clear();
        for (String songId : currentPlaylist.getSongIds()) {
            // Sécurité anti-crash si un ID est null dans Firebase
            if (songId == null || songId.trim().isEmpty()) continue;

            db.collection("songs").document(songId).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    Song song = doc.toObject(Song.class);
                    if (song != null) {
                        song.setId(doc.getId());
                        loadedSongs.add(song);
                        songAdapter.setSongList(new ArrayList<>(loadedSongs));
                    }
                }
            }).addOnFailureListener(e -> {
                Log.e("PlaylistDetail", "Erreur récupération musique: " + songId, e);
            });
        }
    }

    private void removeSongFromPlaylist(Song song) {
        // Empêche un crash supplémentaire si la musique n'a pas d'ID
        if (auth.getCurrentUser() == null || song == null || song.getId() == null) return;

        db.collection("users").document(auth.getCurrentUser().getUid())
                .collection("playlists").document(currentPlaylist.getId())
                .update("songIds", FieldValue.arrayRemove(song.getId()))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Musique retirée", Toast.LENGTH_SHORT).show();
                    loadedSongs.remove(song);
                    songAdapter.setSongList(new ArrayList<>(loadedSongs));
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (miniPlayerController != null) miniPlayerController.updateUI();
    }
}