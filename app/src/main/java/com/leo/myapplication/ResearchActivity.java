package com.leo.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class ResearchActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_research);

        ImageButton homeButton = findViewById(R.id.home_button);
        ImageButton libraryButton = findViewById(R.id.playlists_button);
        ImageButton profileButton = findViewById(R.id.profile_button);
        Button musicButton = findViewById(R.id.music_display_button);

        libraryButton.setOnClickListener( click -> {
            Intent intent = new Intent( getApplicationContext(), LibraryActivity.class);
            startActivity(intent);
        });

        profileButton.setOnClickListener(click -> {
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(intent);
        });

        homeButton.setOnClickListener( click -> {
            Intent intent = new Intent( getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });

        musicButton.setOnClickListener( click -> {
            Intent intent = new Intent( getApplicationContext(), MusicDisplayActivity.class);
            startActivity(intent);
        });
    }
}