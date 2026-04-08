package com.leo.myapplication;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AccountCreationActivity extends AppCompatActivity {

    private EditText identifiantInput, passwordInput, emailInput;
    private Button btnCreateAccount;
    private TextView tvGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_creation_layout);

        identifiantInput = findViewById(R.id.identifiant_input);
        passwordInput = findViewById(R.id.password_input);
        emailInput = findViewById(R.id.email_input);
        btnCreateAccount = findViewById(R.id.btn_create_account);
        tvGoToLogin = findViewById(R.id.tv_go_to_login);

        tvGoToLogin.setOnClickListener(v -> finish());

        btnCreateAccount.setOnClickListener(v -> validateAndCreate());
    }

    private void validateAndCreate() {
        String identifiant = identifiantInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();

        if (identifiant.isEmpty() || password.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Format d'email invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Le mot de passe doit contenir au moins 6 caractères", Toast.LENGTH_SHORT).show();
            return;
        }

    }
}
