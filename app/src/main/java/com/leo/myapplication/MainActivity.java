package com.leo.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton searchButton = findViewById(R.id.search_button);

        searchButton.setOnClickListener( click -> {
            Intent intent = new Intent( getApplicationContext(), ResearchActivity.class);
            startActivity(intent);
        });
    }
}