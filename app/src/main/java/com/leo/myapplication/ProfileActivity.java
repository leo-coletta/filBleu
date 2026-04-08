package com.leo.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            goToLogin();
            return;
        }

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
        Button musicButton = findViewById(R.id.music_display_button);
        Button logoutButton = findViewById(R.id.logout_button);

        loadUserData();

        homeButton.setOnClickListener(click -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });

        searchButton.setOnClickListener(click -> {
            Intent intent = new Intent(getApplicationContext(), ResearchActivity.class);
            startActivity(intent);
        });

        libraryButton.setOnClickListener(click -> {
            Intent intent = new Intent(getApplicationContext(), LibraryActivity.class);
            startActivity(intent);
        });

        musicButton.setOnClickListener(click -> {
            Intent intent = new Intent(getApplicationContext(), MusicDisplayActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            goToLogin();
        });

        editUsername.setOnClickListener(v -> {
            if (!usernameField.isEnabled()) {
                usernameField.setEnabled(true);
                usernameField.requestFocus();
            } else {
                usernameField.setEnabled(false);
                updateUsername(usernameField.getText().toString().trim());
            }
        });

        editEmail.setOnClickListener(v -> {
            if (!emailField.isEnabled()) {
                emailField.setEnabled(true);
                emailField.requestFocus();
            } else {
                emailField.setEnabled(false);
                updateEmail(emailField.getText().toString().trim());
            }
        });

        editPassword.setOnClickListener(v -> {
            passwordContainer.setVisibility(passwordContainer.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        savePasswordButton.setOnClickListener(v -> {
            String oldPass = oldPasswordField.getText().toString().trim();
            String newPass = newPasswordField.getText().toString().trim();

            if (oldPass.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(this, "Remplissez les deux champs", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPass.length() < 6) {
                Toast.makeText(this, "Le nouveau mot de passe est trop court", Toast.LENGTH_SHORT).show();
                return;
            }

            updatePassword(oldPass, newPass);
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadUserData() {
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        usernameField.setText(username != null ? username : "");
                    }
                });

        emailField.setText(currentUser.getEmail());
    }

    private void updateUsername(String newUsername) {
        if (newUsername.isEmpty()) return;

        db.collection("users").document(currentUser.getUid())
                .update("username", newUsername)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Nom d'utilisateur mis à jour", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show());
    }

    private void updateEmail(String newEmail) {
        if (newEmail.isEmpty() || newEmail.equals(currentUser.getEmail())) return;

        currentUser.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Lien de validation envoyé à " + newEmail, Toast.LENGTH_LONG).show();
                emailField.setText(currentUser.getEmail());
            } else {
                String errorMsg = task.getException() != null ? task.getException().getMessage() : "Erreur inconnue";
                Toast.makeText(this, "Échec : " + errorMsg, Toast.LENGTH_LONG).show();
                emailField.setText(currentUser.getEmail());
            }
        });
    }
    private void updatePassword(String oldPass, String newPass) {
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPass);

        currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                currentUser.updatePassword(newPass).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(this, "Mot de passe modifié", Toast.LENGTH_SHORT).show();
                        passwordContainer.setVisibility(View.GONE);
                        oldPasswordField.setText("");
                        newPasswordField.setText("");
                    } else {
                        Toast.makeText(this, "Erreur lors de la modification", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Ancien mot de passe incorrect", Toast.LENGTH_SHORT).show();
            }
        });
    }
}