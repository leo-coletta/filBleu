package com.leo.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText identifiantInput, passwordInput;
    private Button btnLogin;
    private TextView tvGoToCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_layout);

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

    private void attemptLogin() {
        String identifiant = identifiantInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (identifiant.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

    }
}
