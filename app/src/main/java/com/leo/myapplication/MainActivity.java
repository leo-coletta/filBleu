package com.leo.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
    private LinearLayout recommendedContainer;
    private RecyclerView recentTracksRecyclerView;
    private MiniPlayerController miniPlayerController;
    private SongAdapter recentTracksAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        ImageButton searchButton = findViewById(R.id.search_button);
        ImageButton libraryButton = findViewById(R.id.playlists_button);
        ImageButton profileButton = findViewById(R.id.profile_button);

        miniPlayerController = new MiniPlayerController(this);

        // Récupération du conteneur du HorizontalScrollView
        recommendedContainer = findViewById(R.id.recommended_container);

        // Configuration unique du RecyclerView pour l'historique
        recentTracksRecyclerView = findViewById(R.id.recent_tracks_recycler_view);
        recentTracksRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recentTracksAdapter = new SongAdapter();
        recentTracksRecyclerView.setAdapter(recentTracksAdapter);

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

        recentTracksAdapter.setOnSongClickListener(song -> {
            List<Song> toutesLesMusiques = recentTracksAdapter.getSongList();
            int indexActuel = toutesLesMusiques.indexOf(song);

            // Initialisation de la file d'attente globale
            PlaybackManager.getInstance().initQueue(toutesLesMusiques, indexActuel);

            Intent intent = new Intent(MainActivity.this, MusicDisplayActivity.class);
            intent.putExtra("SONG_DATA", song);
            startActivity(intent);
        }); // Accolade fermante corrigée ici

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
                        }
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private void displayRecommended(List<Song> songs) {
        // On s'assure que le conteneur est vide avant d'ajouter
        recommendedContainer.removeAllViews();

        // Calcul de la taille (150dp) et de la marge (15dp) en pixels pour l'écran actuel
        float density = getResources().getDisplayMetrics().density;
        int sizePx = (int) (150 * density);
        int marginPx = (int) (15 * density);

        for (Song song : songs) {
            ImageView imageView = new ImageView(this);

            // Configuration des paramètres de mise en page (taille et marges)
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(sizePx, sizePx);
            layoutParams.setMarginEnd(marginPx);
            imageView.setLayoutParams(layoutParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            // Chargement de l'image
            if (song.getImageUrl() != null && !song.getImageUrl().isEmpty()) {
                Picasso.get().load(song.getImageUrl()).placeholder(R.drawable.music_image_placeholder).into(imageView);
            } else {
                imageView.setImageResource(R.drawable.music_image_placeholder);
            }

            // Gestion du clic pour lancer la musique
            imageView.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, MusicDisplayActivity.class);
                intent.putExtra("SONG_DATA", song);
                startActivity(intent);
            });

            // Ajout de l'image construite au layout
            recommendedContainer.addView(imageView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (miniPlayerController != null) {
            miniPlayerController.updateUI();
        }

        List<Song> recentSongs = CurrentSongManager.getInstance().getRecentSongs();
        if (recentTracksAdapter != null) {
            recentTracksAdapter.setSongList(recentSongs);
        }
    }
}
