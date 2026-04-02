package com.leo.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class ResearchActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_research);

        ImageButton homeButton = findViewById(R.id.home_button);

        homeButton.setOnClickListener( click -> {
            Intent intent = new Intent( getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });
    }
}