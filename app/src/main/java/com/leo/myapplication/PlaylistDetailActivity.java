package com.leo.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class PlaylistDetailActivity extends AppCompatActivity {

    private Playlist playlist;
    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);

        db = FirebaseFirestore.getInstance();
        playlist = getIntent().getParcelableExtra("PLAYLIST_DATA");

        ImageView imageView = findViewById(R.id.playlist_detail_image);
        TextView nameTextView = findViewById(R.id.playlist_detail_name);

        if (playlist != null) {
            nameTextView.setText(playlist.getName());
            if (playlist.getImageUrl() != null && !playlist.getImageUrl().isEmpty()) {
                Picasso.get().load(playlist.getImageUrl()).into(imageView);
            }
        }

        recyclerView = findViewById(R.id.playlist_songs_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SongAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnSongClickListener(song -> {
            Intent intent = new Intent(this, MusicDisplayActivity.class);
            intent.putExtra("SONG_DATA", song);
            startActivity(intent);
        });

        fetchPlaylistSongs();
    }

    private void fetchPlaylistSongs() {
        if (playlist == null || playlist.getSongIds() == null || playlist.getSongIds().isEmpty()) {
            return;
        }

        List<Song> songList = new ArrayList<>();
        for (String songId : playlist.getSongIds()) {
            db.collection("songs").document(songId).get().addOnSuccessListener(documentSnapshot -> {
                Song song = documentSnapshot.toObject(Song.class);
                if (song != null) {
                    songList.add(song);
                    adapter.setSongList(new ArrayList<>(songList));
                }
            });
        }
    }
}
