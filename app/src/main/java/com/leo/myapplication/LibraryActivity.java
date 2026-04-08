package com.leo.myapplication;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryActivity extends AppCompatActivity {

    private RecyclerView songsRecyclerView;
    private RecyclerView playlistsRecyclerView;
    private SongAdapter songAdapter;
    private PlaylistAdapter playlistAdapter;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageButton searchButton = findViewById(R.id.search_button);
        ImageButton profileButton = findViewById(R.id.profile_button);
        ImageButton homeButton = findViewById(R.id.home_button);
        Button musicButton = findViewById(R.id.music_display_button);
        Button btnCreatePlaylist = findViewById(R.id.btn_create_playlist);

        songsRecyclerView = findViewById(R.id.songs_recycler_view);
        songsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        songAdapter = new SongAdapter();
        songsRecyclerView.setAdapter(songAdapter);

        playlistsRecyclerView = findViewById(R.id.playlists_recycler_view);
        playlistsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        playlistAdapter = new PlaylistAdapter();
        playlistsRecyclerView.setAdapter(playlistAdapter);

        homeButton.setOnClickListener(click -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });

        searchButton.setOnClickListener(click -> {
            Intent intent = new Intent(getApplicationContext(), ResearchActivity.class);
            startActivity(intent);
        });

        profileButton.setOnClickListener(click -> {
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(intent);
        });

        musicButton.setOnClickListener(click -> {
            Song playingSong = CurrentSongManager.getInstance().getCurrentSong();

            if (playingSong != null) {
                Intent intent = new Intent(getApplicationContext(), MusicDisplayActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Aucune musique en cours de lecture", Toast.LENGTH_SHORT).show();
            }
        });

        btnCreatePlaylist.setOnClickListener(v -> showCreatePlaylistDialog());

        songAdapter.setOnSongClickListener(song -> {
            Intent intent = new Intent(LibraryActivity.this, MusicDisplayActivity.class);
            intent.putExtra("SONG_DATA", song);
            startActivity(intent);
        });

        songAdapter.setOnFavoriteClickListener(song -> {
            Toast.makeText(LibraryActivity.this, song.getTitle() + " ajouté aux favoris", Toast.LENGTH_SHORT).show();
        });

        playlistAdapter.setOnPlaylistClickListener(playlist -> {
            Intent intent = new Intent(LibraryActivity.this, PlaylistDetailActivity.class);
            intent.putExtra("PLAYLIST_DATA", playlist);
            startActivity(intent);
        });

        fetchSongsFromFirestore();
        fetchPlaylistsFromFirestore();
    }

    private void showCreatePlaylistDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_create_playlist);

        EditText input = dialog.findViewById(R.id.playlist_name_input);
        Button btnCreate = dialog.findViewById(R.id.btn_create);

        btnCreate.setOnClickListener(v -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty() && auth.getCurrentUser() != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("name", name);
                data.put("songIds", new ArrayList<String>());

                db.collection("users").document(auth.getCurrentUser().getUid())
                        .collection("playlists").add(data)
                        .addOnSuccessListener(docRef -> {
                            dialog.dismiss();
                            fetchPlaylistsFromFirestore();
                        });
            }
        });
        dialog.show();
    }

    private void fetchSongsFromFirestore() {
        db.collection("songIds").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Song> songList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Song song = document.toObject(Song.class);
                            song.setId(document.getId());
                            songList.add(song);
                        }
                        songAdapter.setSongList(songList);
                    } else {
                        Log.e("FirestoreError", "Erreur lors de la récupération des musiques", task.getException());
                    }
                });
    }

    private void fetchPlaylistsFromFirestore() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).collection("playlists").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Playlist> playlistList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Playlist playlist = document.toObject(Playlist.class);
                            playlist.setId(document.getId());
                            playlistList.add(playlist);
                        }
                        playlistAdapter.setPlaylistList(playlistList);
                    } else {
                        Log.e("FirestoreError", "Erreur récupération playlists", task.getException());
                    }
                });
    }
}