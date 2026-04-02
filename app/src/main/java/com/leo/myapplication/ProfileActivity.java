package com.leo.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private EditText usernameField;
    private ImageButton editUsername;
    private EditText emailField;
    private ImageButton editEmail;
    private ImageButton editPassword;

    private LinearLayout passwordContainer;
    private EditText oldPasswordField;
    private EditText newPasswordField;
    private Button savePasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        usernameField = findViewById(R.id.username_field);
        editUsername = findViewById(R.id.edit_username);
        emailField = findViewById(R.id.email_field);
        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);

        passwordContainer = findViewById(R.id.password_container);
        oldPasswordField = findViewById(R.id.old_password_field);
        newPasswordField = findViewById(R.id.new_password_field);
        savePasswordButton = findViewById(R.id.save_password_button);

        ImageButton searchButton = findViewById(R.id.search_button);
        ImageButton libraryButton = findViewById(R.id.playlists_button);
        ImageButton homeButton = findViewById(R.id.home_button);

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

        editUsername.setOnClickListener(v -> {
            usernameField.setEnabled(true);
            usernameField.requestFocus();
        });

        editEmail.setOnClickListener(v -> {
            emailField.setEnabled(true);
            emailField.requestFocus();
        });

        editPassword.setOnClickListener(v -> {
            passwordContainer.setVisibility(View.VISIBLE);
        });

        savePasswordButton.setOnClickListener(v -> {
            String oldPass = oldPasswordField.getText().toString();
            String newPass = newPasswordField.getText().toString();

            passwordContainer.setVisibility(View.GONE);
            oldPasswordField.setText("");
            newPasswordField.setText("");
        });
    }
}