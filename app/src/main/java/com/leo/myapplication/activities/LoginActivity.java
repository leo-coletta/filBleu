package com.leo.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.leo.myapplication.R;

/**
 * Activité gérant la connexion d'un utilisateur existant.
 * <p>
 * Connecte l'utilisateur via Firebase Authentication avec son email et mot de passe,
 * puis le redirige vers l'écran principal ({@link MainActivity}) en cas de succès.
 * </p>
 */
public class LoginActivity extends AppCompatActivity {

    private EditText identifiantInput, passwordInput;
    private Button btnLogin;
    private TextView tvGoToCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        identifiantInput = findViewById(R.id.identifiant_input);
        passwordInput = findViewById(R.id.password_input);
        btnLogin = findViewById(R.id.btn_login);
        tvGoToCreate = findViewById(R.id.tv_go_to_create);

        tvGoToCreate.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, AccountCreationActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    /**
     * Tente de connecter l'utilisateur en utilisant Firebase Auth.
     * Affiche un message d'erreur si les identifiants sont incorrects.
     */
    private void attemptLogin() {
        String email = identifiantInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Identifiants incorrects", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}