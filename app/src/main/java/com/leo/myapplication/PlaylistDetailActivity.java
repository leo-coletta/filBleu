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

/**
 * Activité spécialisée dans l'exploration et la gestion approfondie du contenu d'une
 * playlist utilisateur. Elle récupère et liste de manière asynchrone les musiques ciblées,
 * intègre des options de désélection, et coordonne le lancement contextuel de la file
 * d'attente globale de lecture.
 */
public class PlaylistDetailActivity extends AppCompatActivity {

    private Playlist currentPlaylist;
    private RecyclerView songsRecyclerView;
    private SongAdapter songAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<Song> loadedSongs = new ArrayList<>();
    private MiniPlayerController miniPlayerController;

    /**
     * Point d'entrée de l'activité. Paramètre l'architecture visuelle incluant le RecyclerView
     * des musiques, identifie la playlist courante transmise par l'Intent parent, configure
     * l'image de couverture et délègue le chargement des titres hébergés sur Firestore.
     *
     * @param savedInstanceState L'état du bundle de contexte conservé par le système Android.
     */
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

    /**
     * Itère sur la collection d'identifiants de la playlist pour effectuer une requête réseau
     * sur chaque document de musique Firestore correspondant. Filtre activement les références
     * nulles ou corrompues pour garantir l'intégrité de la liste locale (loadedSongs)
     * et l'affichage séquentiel dans l'Adapter.
     */
    private void fetchSongsForPlaylist() {
        if (currentPlaylist.getSongIds() == null || currentPlaylist.getSongIds().isEmpty()) {
            Toast.makeText(this, "Cette playlist est vide", Toast.LENGTH_SHORT).show();
            loadedSongs.clear();
            songAdapter.setSongList(loadedSongs);
            return;
        }

        loadedSongs.clear();
        for (String songId : currentPlaylist.getSongIds()) {
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

    /**
     * Intercepte l'événement de suppression généré par l'adaptateur pour extraire un ID de
     * musique hors du tableau hébergé sur Firebase en appliquant FieldValue.arrayRemove().
     * En cas de succès réseau, purge également la donnée de la mémoire locale de l'application.
     *
     * @param song L'objet Song porteur des informations ciblées pour la suppression.
     */
    private void removeSongFromPlaylist(Song song) {
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

    /**
     * Rappelé automatiquement par le système d'exploitation Android lorsque l'interface
     * utilisateur redevient visible au premier plan, forçant une synchronisation visuelle
     * préventive de l'état du contrôleur de musique réduit.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (miniPlayerController != null) miniPlayerController.updateUI();
    }
}