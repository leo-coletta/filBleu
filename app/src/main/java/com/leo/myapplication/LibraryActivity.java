package com.leo.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class LibraryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        ImageButton searchButton = findViewById(R.id.search_button);
        ImageButton profileButton = findViewById(R.id.profile_button);
        ImageButton homeButton = findViewById(R.id.home_button);
        Button musicButton = findViewById(R.id.music_display_button);

        homeButton.setOnClickListener( click -> {
            Intent intent = new Intent( getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });

        searchButton.setOnClickListener( click -> {
            Intent intent = new Intent( getApplicationContext(), ResearchActivity.class);
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
