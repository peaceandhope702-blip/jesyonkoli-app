package com.ricardo.jesyonkoli.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ricardo.jesyonkoli.R;
import com.ricardo.jesyonkoli.ui.auth.LoginActivity;

public class AdminPanelActivity extends AppCompatActivity {

    private TextView tvAvatar;
    private TextView tvTotalEncomendas;
    private TextView tvPendentes;
    private TextView tvRetiradas;
    private TextView tvUsuarios;

    private Button btnGerenciarUsuarios;
    private Button btnGerenciarUnidades;
    private Button btnVerEncomendas;
    private Button btnConfiguracoes;
    private Button btnSair;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        db = FirebaseFirestore.getInstance();

        tvAvatar = findViewById(R.id.tvAvatar);
        tvTotalEncomendas = findViewById(R.id.tvTotalEncomendas);
        tvPendentes = findViewById(R.id.tvPendentes);
        tvRetiradas = findViewById(R.id.tvRetiradas);
        tvUsuarios = findViewById(R.id.tvUsuarios);

        btnGerenciarUsuarios = findViewById(R.id.btnGerenciarUsuarios);
        btnGerenciarUnidades = findViewById(R.id.btnGerenciarUnidades);
        btnVerEncomendas = findViewById(R.id.btnVerEncomendas);
        btnConfiguracoes = findViewById(R.id.btnConfiguracoes);
        btnSair = findViewById(R.id.btnSair);

        configurarAvatar();
        configurarBotoes();
        carregarEstatisticas();
    }

    private void configurarAvatar() {
        String email = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getEmail()
                : null;

        if (email != null && !email.trim().isEmpty()) {
            tvAvatar.setText(email.substring(0, 1).toUpperCase());
        } else {
            tvAvatar.setText("A");
        }
    }

    private void configurarBotoes() {
        btnGerenciarUsuarios.setOnClickListener(v -> {
            Intent intent = new Intent(AdminPanelActivity.this, UsuariosActivity.class);
            startActivity(intent);
        });

        btnGerenciarUnidades.setOnClickListener(v -> {
            Intent intent = new Intent(AdminPanelActivity.this, UnitsActivity.class);
            startActivity(intent);
        });

        btnVerEncomendas.setOnClickListener(v -> {
            Intent intent = new Intent(AdminPanelActivity.this,
                    com.ricardo.jesyonkoli.ui.portaria.PendentesActivity.class);
            startActivity(intent);
        });

        btnConfiguracoes.setOnClickListener(v -> {
            String[] opcoes = {
                    "Gerenciar Usuários",
                    "Gerenciar Unidades",
                    "Sair da conta"
            };

            new AlertDialog.Builder(AdminPanelActivity.this)
                    .setTitle("Configurações")
                    .setItems(opcoes, (dialog, which) -> {
                        if (which == 0) {
                            Intent intent = new Intent(AdminPanelActivity.this, UsuariosActivity.class);
                            startActivity(intent);

                        } else if (which == 1) {
                            Intent intent = new Intent(AdminPanelActivity.this, UnitsActivity.class);
                            startActivity(intent);

                        } else if (which == 2) {
                            FirebaseAuth.getInstance().signOut();

                            Intent intent = new Intent(AdminPanelActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .show();
        });

        btnSair.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(AdminPanelActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void carregarEstatisticas() {
        carregarTotalEncomendas();
        carregarPendentes();
        carregarRetiradas();
        carregarUsuarios();
    }

    private void carregarTotalEncomendas() {
        db.collection("encomendas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                        tvTotalEncomendas.setText(String.valueOf(queryDocumentSnapshots.size()))
                )
                .addOnFailureListener(e ->
                        tvTotalEncomendas.setText("0")
                );
    }

    private void carregarPendentes() {
        db.collection("encomendas")
                .whereEqualTo("status", "PENDENTE")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                        tvPendentes.setText(String.valueOf(queryDocumentSnapshots.size()))
                )
                .addOnFailureListener(e ->
                        tvPendentes.setText("0")
                );
    }

    private void carregarRetiradas() {
        db.collection("encomendas")
                .whereEqualTo("status", "RETIRADA")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                        tvRetiradas.setText(String.valueOf(queryDocumentSnapshots.size()))
                )
                .addOnFailureListener(e ->
                        tvRetiradas.setText("0")
                );
    }

    private void carregarUsuarios() {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                        tvUsuarios.setText(String.valueOf(queryDocumentSnapshots.size()))
                )
                .addOnFailureListener(e ->
                        tvUsuarios.setText("0")
                );
    }
}