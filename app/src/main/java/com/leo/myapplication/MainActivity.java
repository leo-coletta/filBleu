package com.leo.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "Fil Bleu + " + getClass().getSimpleName();
    private ImageView recommended1, recommended2;
    private RecyclerView recentTracksRecyclerView;
    private SongAdapter songAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        ImageButton searchButton = findViewById(R.id.search_button);
        ImageButton libraryButton = findViewById(R.id.playlists_button);
        ImageButton profileButton = findViewById(R.id.profile_button);
        Button musicButton = findViewById(R.id.music_display_button);
        
        recommended1 = findViewById(R.id.recomended1);
        recommended2 = findViewById(R.id.recommended2);
        
        recentTracksRecyclerView = findViewById(R.id.recent_tracks_recycler_view);
        recentTracksRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        songAdapter = new SongAdapter();
        recentTracksRecyclerView.setAdapter(songAdapter);

        searchButton.setOnClickListener( click -> {
            Intent intent = new Intent( getApplicationContext(), ResearchActivity.class);
            startActivity(intent);
        });

        libraryButton.setOnClickListener( click -> {
            Intent intent = new Intent( getApplicationContext(), LibraryActivity.class);
            startActivity(intent);
        });

        profileButton.setOnClickListener(click -> {
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(intent);
        });

        musicButton.setOnClickListener( click -> {
            Intent intent = new Intent( getApplicationContext(), MusicDisplayActivity.class);
            startActivity(intent);
        });

        songAdapter.setOnSongClickListener(song -> {
            List<Song> toutesLesMusiques = songAdapter.getSongList();

            int indexActuel = toutesLesMusiques.indexOf(song);
            PlaybackManager.getInstance().initQueue(toutesLesMusiques, indexActuel);

            Intent intent = new Intent(MainActivity.this, MusicDisplayActivity.class);
            intent.putExtra("SONG_DATA", song);
            startActivity(intent);
        });

        db.collection("songs")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Song> songList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Song song = document.toObject(Song.class);
                            song.setId(document.getId());
                            songList.add(song);
                        }
                        
                        if (!songList.isEmpty()) {
                            displayRecommended(songList);
                            songAdapter.setSongList(songList);
                        }
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private void displayRecommended(List<Song> songs) {
        if (!songs.isEmpty()) {
            Song s1 = songs.get(0);
            if (s1.getImageUrl() != null && !s1.getImageUrl().isEmpty()) {
                Picasso.get().load(s1.getImageUrl()).into(recommended1);
            }
            recommended1.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, MusicDisplayActivity.class);
                intent.putExtra("SONG_DATA", s1);
                startActivity(intent);
            });
        }
        
        if (songs.size() >= 2) {
            Song s2 = songs.get(1);
            if (s2.getImageUrl() != null && !s2.getImageUrl().isEmpty()) {
                Picasso.get().load(s2.getImageUrl()).into(recommended2);
            }
            recommended2.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, MusicDisplayActivity.class);
                intent.putExtra("SONG_DATA", s2);
                startActivity(intent);
            });
        }
    }
}
