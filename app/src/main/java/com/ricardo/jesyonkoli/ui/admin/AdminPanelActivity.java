package com.ricardo.jesyonkoli.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ricardo.jesyonkoli.R;
import com.ricardo.jesyonkoli.ui.auth.LoginActivity;

public class AdminPanelActivity extends AppCompatActivity {

    private static final String TAG = "ADMIN_PANEL";

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
    private String condominioId;

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
        carregarDadosAdmin();
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

    private void carregarDadosAdmin() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = currentUser.getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Dados do admin não encontrados", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Documento do admin não existe: " + uid);
                        return;
                    }

                    String role = doc.getString("role");
                    condominioId = doc.getString("condominioId");

                    Log.d(TAG, "Role do usuário: " + role);
                    Log.d(TAG, "condominioId do admin: " + condominioId);

                    if (!"ADMIN".equals(role)) {
                        Toast.makeText(this, "Este painel é apenas para ADMIN", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    if (condominioId == null || condominioId.trim().isEmpty()) {
                        Toast.makeText(this, "Admin sem condomínio vinculado", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Admin sem condominioId");
                        tvTotalEncomendas.setText("0");
                        tvPendentes.setText("0");
                        tvRetiradas.setText("0");
                        tvUsuarios.setText("0");
                        return;
                    }

                    carregarEstatisticas();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar dados do admin", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Erro ao buscar admin", e);
                });
    }

    private void configurarBotoes() {
        btnGerenciarUsuarios.setOnClickListener(v -> {
            if (condominioId == null || condominioId.trim().isEmpty()) {
                Toast.makeText(this, "Admin sem condomínio vinculado", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(AdminPanelActivity.this, UsuariosActivity.class);
            intent.putExtra("condominioId", condominioId);
            startActivity(intent);
        });

        btnGerenciarUnidades.setOnClickListener(v -> {
            if (condominioId == null || condominioId.trim().isEmpty()) {
                Toast.makeText(this, "Admin sem condomínio vinculado", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(AdminPanelActivity.this, UnitsActivity.class);
            intent.putExtra("condominioId", condominioId);
            startActivity(intent);
        });

        btnVerEncomendas.setOnClickListener(v -> {
            if (condominioId == null || condominioId.trim().isEmpty()) {
                Toast.makeText(this, "Admin sem condomínio vinculado", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(
                    AdminPanelActivity.this,
                    com.ricardo.jesyonkoli.ui.portaria.PendentesActivity.class
            );
            intent.putExtra("condominioId", condominioId);
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
                            if (condominioId == null || condominioId.trim().isEmpty()) {
                                Toast.makeText(this, "Admin sem condomínio vinculado", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Intent intent = new Intent(AdminPanelActivity.this, UsuariosActivity.class);
                            intent.putExtra("condominioId", condominioId);
                            startActivity(intent);

                        } else if (which == 1) {
                            if (condominioId == null || condominioId.trim().isEmpty()) {
                                Toast.makeText(this, "Admin sem condomínio vinculado", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Intent intent = new Intent(AdminPanelActivity.this, UnitsActivity.class);
                            intent.putExtra("condominioId", condominioId);
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
                .whereEqualTo("condominioId", condominioId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                        tvTotalEncomendas.setText(String.valueOf(queryDocumentSnapshots.size()))
                )
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao carregar total encomendas", e);
                    tvTotalEncomendas.setText("0");
                });
    }

    private void carregarPendentes() {
        db.collection("encomendas")
                .whereEqualTo("status", "PENDENTE")
                .whereEqualTo("condominioId", condominioId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                        tvPendentes.setText(String.valueOf(queryDocumentSnapshots.size()))
                )
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao carregar pendentes", e);
                    tvPendentes.setText("0");
                });
    }

    private void carregarRetiradas() {
        db.collection("encomendas")
                .whereEqualTo("status", "RETIRADA")
                .whereEqualTo("condominioId", condominioId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                        tvRetiradas.setText(String.valueOf(queryDocumentSnapshots.size()))
                )
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao carregar retiradas", e);
                    tvRetiradas.setText("0");
                });
    }

    private void carregarUsuarios() {
        db.collection("users")
                .whereEqualTo("condominioId", condominioId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                        tvUsuarios.setText(String.valueOf(queryDocumentSnapshots.size()))
                )
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao carregar usuários", e);
                    tvUsuarios.setText("0");
                });
    }
}