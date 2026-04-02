package com.leo.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MusicDisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_display);

        ImageButton searchButton = findViewById(R.id.search_button);
        ImageButton libraryButton = findViewById(R.id.playlists_button);
        ImageButton homeButton = findViewById(R.id.home_button);
        ImageButton backButton = findViewById(R.id.back_page_button);

        Intent oldIntent = getIntent();

        homeButton.setOnClickListener( click -> {
            Intent intent = new Intent( getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });

        searchButton.setOnClickListener( click -> {
            Intent intent = new Intent( getApplicationContext(), ResearchActivity.class);
            startActivity(intent);
        });

        libraryButton.setOnClickListener( click -> {
            Intent intent = new Intent( getApplicationContext(), LibraryActivity.class);
            startActivity(intent);
        });

        backButton.setOnClickListener( click -> {
            Intent intent = new Intent( getApplicationContext(), oldIntent.getClass());
            startActivity(intent);
        });

    }

}