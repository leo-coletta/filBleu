package com.leo.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LibraryActivity extends AppCompatActivity {

    private RecyclerView songsRecyclerView;
    private RecyclerView playlistsRecyclerView;
    private SongAdapter songAdapter;
    private PlaylistAdapter playlistAdapter;
    private MiniPlayerController miniPlayerController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        ImageButton searchButton = findViewById(R.id.search_button);
        ImageButton profileButton = findViewById(R.id.profile_button);
        ImageButton homeButton = findViewById(R.id.home_button);
        miniPlayerController = new MiniPlayerController(this);

        songsRecyclerView = findViewById(R.id.songs_recycler_view);
        songsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        songAdapter = new SongAdapter();
        songsRecyclerView.setAdapter(songAdapter);

        playlistsRecyclerView = findViewById(R.id.playlists_recycler_view);
        playlistsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        playlistAdapter = new PlaylistAdapter();
        playlistsRecyclerView.setAdapter(playlistAdapter);

        homeButton.setOnClickListener( click -> {
            Intent intent = new Intent( getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });

        searchButton.setOnClickListener( click -> {
            Intent intent = new Intent( getApplicationContext(), ResearchActivity.class);
            startActivity(intent);
        });

        profileButton.setOnClickListener(click -> {
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(intent);
        });

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

    private void fetchSongsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("songs").get()
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
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists").get()
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
                        Log.e("FirestoreError", "Erreur lors de la récupération des playlists", task.getException());
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (miniPlayerController != null) {
            miniPlayerController.updateUI();
        }
    }
}
