package com.leo.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "Fil Bleu + " + getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        ImageButton searchButton = findViewById(R.id.search_button);
        ImageButton libraryButton = findViewById(R.id.playlists_button);
        ImageButton profileButton = findViewById(R.id.profile_button);
        Button musicButton = findViewById(R.id.music_display_button);
        ImageView recommended1 = findViewById(R.id.recomended1);

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

        db.collection("songs")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Song> songList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Song song = document.toObject(Song.class);
                                songList.add(song);
                            }
                            if (!songList.isEmpty()) {
                                String imageUrl = songList.get(0).getImageUrl();
                                if (imageUrl != null && !imageUrl.isEmpty()) {
                                    // 4. Chargement de l'image avec Picasso
                                    Picasso.get()
                                            .load(imageUrl)
                                            .into(recommended1, new Callback() {
                                                @Override
                                                public void onSuccess() {
                                                    Log.d("MainActivity", "Image téléchargée et affichée !");
                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    Log.e("MainActivity", "Erreur Picasso : ", e);
                                                }
                                            });
                                } else {
                                    Log.e("MainActivity", "L'URL de l'image est vide dans le document Firestore.");
                                }
                            } else {
                                Log.w(TAG, "Error getting documents.", task.getException());
                            }
                        }
                    }
                });
    }
}