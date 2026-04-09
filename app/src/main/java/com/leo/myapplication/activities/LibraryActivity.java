package com.leo.myapplication.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.leo.myapplication.controllers.MiniPlayerController;
import com.leo.myapplication.models.Playlist;
import com.leo.myapplication.adapters.PlaylistAdapter;
import com.leo.myapplication.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activité affichant la bibliothèque personnelle de l'utilisateur.
 * <p>
 * Récupère les playlists de l'utilisateur depuis Firestore et lui permet
 * d'en créer de nouvelles via une boîte de dialogue. Maintient également l'état
 * du mini-lecteur multimédia ({@link MiniPlayerController}).
 * </p>
 */
public class LibraryActivity extends AppCompatActivity {

    private RecyclerView playlistsRecyclerView;
    private PlaylistAdapter playlistAdapter;
    private MiniPlayerController miniPlayerController;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        miniPlayerController = new MiniPlayerController(this);

        ImageButton searchButton = findViewById(R.id.search_button);
        ImageButton profileButton = findViewById(R.id.profile_button);
        ImageButton homeButton = findViewById(R.id.home_button);
        ImageButton btnCreatePlaylist = findViewById(R.id.btn_create_playlist);

        playlistsRecyclerView = findViewById(R.id.playlists_recycler_view);
        playlistsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        playlistAdapter = new PlaylistAdapter();
        playlistsRecyclerView.setAdapter(playlistAdapter);

        homeButton.setOnClickListener(click -> startActivity(new Intent(getApplicationContext(), MainActivity.class)));
        searchButton.setOnClickListener(click -> startActivity(new Intent(getApplicationContext(), ResearchActivity.class)));
        profileButton.setOnClickListener(click -> startActivity(new Intent(getApplicationContext(), ProfileActivity.class)));

        btnCreatePlaylist.setOnClickListener(v -> showCreatePlaylistDialog());

        playlistAdapter.setOnPlaylistClickListener(playlist -> {
            Intent intent = new Intent(LibraryActivity.this, PlaylistDetailActivity.class);
            intent.putExtra("PLAYLIST_DATA", playlist);
            startActivity(intent);
        });

        fetchPlaylistsFromFirestore();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (miniPlayerController != null) {
            miniPlayerController.updateUI();
        }
    }

    /**
     * Affiche une boîte de dialogue (Dialog) permettant à l'utilisateur
     * de saisir le nom d'une nouvelle playlist, puis l'enregistre dans Firestore.
     */
    private void showCreatePlaylistDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_create_playlist);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

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

    /**
     * Interroge Firestore pour récupérer toutes les playlists associées à l'utilisateur connecté.
     */
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