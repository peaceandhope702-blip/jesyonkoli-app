package com.ricardo.jesyonkoli.ui.auth;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.ricardo.jesyonkoli.R;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView btnRegister, tvEsqueciSenha;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitylogin);

        auth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvEsqueciSenha = findViewById(R.id.tvEsqueciSenha);

        btnLogin.setOnClickListener(v -> loginUser());

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        tvEsqueciSenha.setOnClickListener(v -> {
            mostrarDialogResetSenha();
        });
    }

    // 🔵 SNACKBAR GLOBAL (ANLÈ)
    // 🔵 SNACKBAR GLOBAL (AK ANCHOR VIEW)
    private void showMessage(String message) {

        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                message,
                Snackbar.LENGTH_LONG);

        // 👉 FIX: pa kache pa klavye (li kole sou bouton an)
        snackbar.setAnchorView(btnLogin);

        snackbar.show();
    }

    // 🔵 VERIFYE INTERNET
    private boolean temInternet() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    // 🔵 LOGIN
    private void loginUser() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Informe o e-mail.");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("E-mail inválido.");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Informe a senha.");
            etPassword.requestFocus();
            return;
        }

        if (!temInternet()) {
            showMessage("Sem conexão com internet.");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);

                    showMessage("Login realizado com sucesso.");

                    RoleGate.routeUser(LoginActivity.this);
                })
                .addOnFailureListener(e -> {

                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);

                    String message;

                    if (e instanceof com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                        message = "E-mail ou senha incorretos.";
                    } else if (e instanceof com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                        message = "Usuário não encontrado.";
                    } else if (e instanceof com.google.firebase.FirebaseNetworkException) {
                        message = "Sem conexão com internet.";
                    } else {
                        message = "Erro ao fazer login. Tente novamente.";
                    }

                    showMessage(message);
                });
    }

    // 🔵 DIALOG RESET PASSWORD
    private void mostrarDialogResetSenha() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Redefinir senha");

        View view = getLayoutInflater().inflate(R.layout.dialog_reset_password, null);
        builder.setView(view);

        EditText etEmailReset = view.findViewById(R.id.etEmailReset);

        // 👉 autofill email (UX pro)
        etEmailReset.setText(etEmail.getText().toString());

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("Enviar", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

            String email = etEmailReset.getText().toString().trim();

            if (email.isEmpty()) {
                etEmailReset.setError("Digite seu email");
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmailReset.setError("Email inválido");
                return;
            }

            if (!temInternet()) {
                showMessage("Sem conexão com internet.");
                return;
            }

            // 🔴 BLOKE BOUTON
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

            enviarResetSenha(email, dialog);
        });
    }

    // 🔵 RESET PASSWORD
    private void enviarResetSenha(String email, AlertDialog dialog) {

        progressBar.setVisibility(View.VISIBLE);

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {

                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {

                        showMessage("Se o email estiver cadastrado, você receberá instruções.");

                        dialog.dismiss();

                    } else {

                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

                        showMessage("Erro ao enviar email. Tente novamente.");
                    }
                });
    }
}