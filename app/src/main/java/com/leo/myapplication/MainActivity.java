package com.leo.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton searchButton = findViewById(R.id.search_button);
        ImageButton libraryButton = findViewById(R.id.playlists_button);
        ImageButton profileButton = findViewById(R.id.profile_button);
        Button musicButton = findViewById(R.id.music_display_button);

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

    }
}