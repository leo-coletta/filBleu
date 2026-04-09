package com.leo.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.leo.myapplication.controllers.MiniPlayerController;
import com.leo.myapplication.logic.PlaybackManager;
import com.leo.myapplication.R;
import com.leo.myapplication.models.Song;
import com.leo.myapplication.adapters.SongAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Activité dédiée à la recherche de morceaux de musique.
 * <p>
 * Charge le catalogue musical depuis Firebase Firestore et permet un filtrage en temps réel
 * par titre ou artiste grâce à une barre de recherche {@link SearchView}.
 * Initialise également la file de lecture ({@link PlaybackManager}) au clic sur un résultat.
 * </p>
 */
public class ResearchActivity extends AppCompatActivity {

    private SongAdapter songAdapter;
    private List<Song> allSongs = new ArrayList<>();
    private MiniPlayerController miniPlayerController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_research);
        miniPlayerController = new MiniPlayerController(this);

        ImageButton homeButton = findViewById(R.id.home_button);
        ImageButton libraryButton = findViewById(R.id.playlists_button);
        ImageButton profileButton = findViewById(R.id.profile_button);

        RecyclerView recyclerView = findViewById(R.id.results_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        songAdapter = new SongAdapter();
        recyclerView.setAdapter(songAdapter);

        songAdapter.setOnSongClickListener(song -> {
            List<Song> currentQueue = songAdapter.getSongList();
            PlaybackManager.getInstance().initQueue(currentQueue, currentQueue.indexOf(song));

            Intent intent = new Intent(ResearchActivity.this, MusicDisplayActivity.class);
            intent.putExtra("SONG_DATA", song);
            startActivity(intent);
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("songs").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allSongs.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Song song = document.toObject(Song.class);
                    song.setId(document.getId());
                    allSongs.add(song);
                }
                filterSongs("");
            } else {
                Log.e("ResearchActivity", "Erreur lors du chargement des musiques", task.getException());
            }
        });

        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterSongs(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSongs(newText);
                return true;
            }
        });

        libraryButton.setOnClickListener(click -> startActivity(new Intent(getApplicationContext(), LibraryActivity.class)));
        profileButton.setOnClickListener(click -> startActivity(new Intent(getApplicationContext(), ProfileActivity.class)));
        homeButton.setOnClickListener(click -> startActivity(new Intent(getApplicationContext(), MainActivity.class)));
    }

    /**
     * Filtre la liste complète des musiques selon la requête saisie et met à jour l'adaptateur.
     * Recherche correspondances partielles dans le titre ou l'artiste (insensible à la casse).
     *
     * @param text La chaîne de caractères à rechercher.
     */
    private void filterSongs(String text) {
        List<Song> filteredList = new ArrayList<>();
        String query = text.toLowerCase().trim();

        for (Song song : allSongs) {
            if (song.getTitle().toLowerCase().contains(query) ||
                    song.getArtist().toLowerCase().contains(query)) {
                filteredList.add(song);
            }
        }

        songAdapter.setSongList(filteredList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (miniPlayerController != null) {
            miniPlayerController.updateUI();
        }
    }
}