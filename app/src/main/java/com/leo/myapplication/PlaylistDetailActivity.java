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

import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PlaylistDetailActivity extends AppCompatActivity {

    private Playlist currentPlaylist;
    private RecyclerView songsRecyclerView;
    private SongAdapter songAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);

        db = FirebaseFirestore.getInstance();

        ImageButton backButton = findViewById(R.id.back_button);
        TextView playlistNameText = findViewById(R.id.playlist_name);
        ImageView playlistImage = findViewById(R.id.playlist_image);
        songsRecyclerView = findViewById(R.id.songs_recycler_view);

        backButton.setOnClickListener(v -> finish());

        songAdapter = new SongAdapter();
        songAdapter.setShowFullHearts(true);
        songsRecyclerView.setAdapter(songAdapter);

        if (currentPlaylist != null) {
            playlistNameText.setText(currentPlaylist.getName());

            if (currentPlaylist.getId().equals("liked_songs")) {
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
            return;
        }

        List<Song> songsList = new ArrayList<>();

        // On récupère chaque musique via son ID
        for (String songId : currentPlaylist.getSongIds()) {
            db.collection("songs").document(songId).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    Song song = doc.toObject(Song.class);
                    if (song != null) {
                        song.setId(doc.getId());
                        songsList.add(song);
                        songAdapter.setSongList(songsList);
                    }
                }
            }).addOnFailureListener(e -> {
                Log.e("PlaylistDetail", "Erreur récupération musique: " + songId, e);
            });
        }
    }
}