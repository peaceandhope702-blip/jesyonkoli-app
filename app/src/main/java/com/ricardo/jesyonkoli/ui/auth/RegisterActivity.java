package com.ricardo.jesyonkoli.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar; // ✅ Snackbar
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.ricardo.jesyonkoli.R;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private EditText etNome, etEmail, etPassword, etInvitationCode;
    private Button btnGoRegister;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        etNome = findViewById(R.id.etNome);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etInvitationCode = findViewById(R.id.etInvitationCode);

        btnGoRegister = findViewById(R.id.btnGoRegister);
        progressBar = findViewById(R.id.progressBar);

        btnGoRegister.setOnClickListener(v -> register());

        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void register() {
        String nome = etNome.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String code = etInvitationCode.getText().toString().trim().toUpperCase();

        if (nome.isEmpty() || email.isEmpty() || pass.isEmpty() || code.isEmpty()) {
            show("Preencha todos os campos");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            show("Email inválido");
            return;
        }

        if (pass.length() < 6) {
            show("Senha deve ter pelo menos 6 caracteres");
            return;
        }

        checkCode(code, nome, email, pass);
    }

    private void checkCode(String code, String nome, String email, String pass) {
        db.collection("invitationCodes")
                .document(code)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        show("Código inválido");
                        return;
                    }

                    Boolean active = doc.getBoolean("active");
                    if (active == null || !active) {
                        show("Código inativo");
                        return;
                    }

                    Long uses = doc.getLong("uses");
                    Long maxUses = doc.getLong("maxUses");

                    if (uses != null && maxUses != null && uses >= maxUses) {
                        show("Código expirado");
                        return;
                    }

                    String unitId = doc.getString("unitId");
                    String condominioId = doc.getString("condominioId");

                    if (unitId == null || unitId.trim().isEmpty()
                            || condominioId == null || condominioId.trim().isEmpty()) {
                        show("Código inválido (dados incompletos)");
                        return;
                    }

                    verifyUnit(nome, email, pass, unitId, condominioId, doc.getReference());
                })
                .addOnFailureListener(e -> show("Erro ao verificar código: " + e.getMessage()));
    }

    private void verifyUnit(String nome, String email, String pass,
                            String unitId, String condominioId, DocumentReference codeRef) {

        db.collection("units")
                .document(unitId)
                .get()
                .addOnSuccessListener(unitDoc -> {

                    if (!unitDoc.exists()) {
                        show("Unidade não existe");
                        return;
                    }

                    String unidade = unitDoc.getString("unidade");
                    if (unidade == null || unidade.trim().isEmpty()) {
                        unidade = unitId;
                    }

                    createUser(nome, email, pass, unitId, unidade, condominioId, codeRef);
                })
                .addOnFailureListener(e -> show("Erro ao verificar unidade: " + e.getMessage()));
    }

    private void createUser(String nome, String email, String pass,
                            String unitId, String unidade, String condominioId, DocumentReference codeRef) {

        btnGoRegister.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> {

                    if (result.getUser() == null) {
                        show("Erro ao criar conta");

                        btnGoRegister.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                        return;
                    }

                    String uid = result.getUser().getUid();

                    Map<String, Object> user = new HashMap<>();
                    user.put("nome", nome);
                    user.put("email", email);
                    user.put("role", "MORADOR");
                    user.put("unitId", unitId);
                    user.put("unidade", unidade);
                    user.put("condominioId", condominioId);
                    user.put("status", "ATIVO");
                    user.put("createdAt", FieldValue.serverTimestamp());

                    db.collection("users")
                            .document(uid)
                            .set(user)
                            .addOnSuccessListener(unused ->
                                    inativarMoradoresAntigos(uid, condominioId, unitId, () -> {

                                        codeRef.update(
                                                        "uses", FieldValue.increment(1),
                                                        "used", true,
                                                        "active", false
                                                )
                                                .addOnSuccessListener(aVoid -> {
                                                    show("Conta criada com sucesso!");
                                                    progressBar.setVisibility(View.GONE);
                                                    RoleGate.routeUser(this);
                                                })
                                                .addOnFailureListener(e -> {
                                                    show("Conta criada, mas erro ao atualizar uso do código");

                                                    btnGoRegister.setEnabled(true);
                                                    progressBar.setVisibility(View.GONE);
                                                    RoleGate.routeUser(this);
                                                });
                                    })
                            )
                            .addOnFailureListener(e -> {
                                show("Erro ao salvar usuário: " + e.getMessage());

                                btnGoRegister.setEnabled(true);
                                progressBar.setVisibility(View.GONE);
                            });
                })
                .addOnFailureListener(e -> {
                    show("Erro ao criar conta: " + e.getMessage());

                    btnGoRegister.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void inativarMoradoresAntigos(String newUid, String condominioId, String unitId, Runnable onDone) {
        db.collection("users")
                .whereEqualTo("role", "MORADOR")
                .whereEqualTo("condominioId", condominioId)
                .whereEqualTo("unitId", unitId)
                .whereEqualTo("status", "ATIVO")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    WriteBatch batch = db.batch();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        if (!doc.getId().equals(newUid)) {
                            batch.update(doc.getReference(), "status", "INATIVO");
                            batch.update(doc.getReference(), "updatedAt", FieldValue.serverTimestamp());
                        }
                    }

                    batch.commit()
                            .addOnSuccessListener(unused -> onDone.run())
                            .addOnFailureListener(e -> show("Usuário criado, mas erro ao inativar morador antigo"));
                })
                .addOnFailureListener(e -> show("Erro ao buscar moradores antigos: " + e.getMessage()));
    }

    // ✅ Snackbar ak anchor (SOLISYON PRO)
    private void show(String msg) {
        View root = findViewById(android.R.id.content);

        Snackbar snackbar = Snackbar.make(root, msg, Snackbar.LENGTH_LONG);

        // ⭐ li ap parèt anlè bouton an, pa kole ak klavye
        snackbar.setAnchorView(btnGoRegister);

        snackbar.show();
    }
}