package com.ricardo.jesyonkoli.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ricardo.jesyonkoli.R;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private EditText etNome, etEmail, etPassword, etInvitationCode;

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

        Button btnRegister = findViewById(R.id.btnGoRegister);
        Button btnGoLogin = findViewById(R.id.btnLogin);

        btnRegister.setOnClickListener(v -> register());

        btnGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void register() {
        String nome = etNome.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String code = etInvitationCode.getText().toString().trim();

        if (nome.isEmpty()) {
            showToast("Informe o nome completo.");
            return;
        }

        if (email.isEmpty()) {
            showToast("Informe o e-mail.");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("E-mail inválido.");
            return;
        }

        if (pass.isEmpty()) {
            showToast("Informe a senha.");
            return;
        }

        if (pass.length() < 6) {
            showToast("A senha deve ter pelo menos 6 caracteres.");
            return;
        }

        if (code.isEmpty()) {
            showToast("Informe o código de convite.");
            return;
        }

        checkInvitationCodeAndContinue(code, nome, email, pass);
    }

    private void checkInvitationCodeAndContinue(String code, String nome, String email, String pass) {
        db.collection("invitationCodes")
                .whereEqualTo("code", code)
                .whereEqualTo("active", true)
                .limit(1)
                .get()
                .addOnSuccessListener(qs -> {
                    if (qs.isEmpty()) {
                        showToast("Código inválido ou inativo.");
                        return;
                    }

                    DocumentSnapshot doc = qs.getDocuments().get(0);

                    Long uses = doc.getLong("uses");
                    Long maxUses = doc.getLong("maxUses");

                    if (uses != null && maxUses != null && uses >= maxUses) {
                        showToast("Este código já atingiu o limite de uso.");
                        return;
                    }

                    Timestamp expireAt = doc.getTimestamp("expireAt");
                    if (expireAt != null && expireAt.toDate().before(new java.util.Date())) {
                        showToast("Este código expirou.");
                        return;
                    }

                    String unitId = doc.getString("unitId");
                    String condominioId = doc.getString("condominioId");

                    if (unitId == null || unitId.trim().isEmpty()) {
                        showToast("Unidade não definida no código de convite.");
                        return;
                    }

                    if (condominioId == null || condominioId.trim().isEmpty()) {
                        showToast("Condomínio não definido no código de convite.");
                        return;
                    }

                    verifyUnitAndContinue(nome, email, pass, unitId.trim(), condominioId.trim(), doc.getReference());
                })
                .addOnFailureListener(e ->
                        showToast("Erro ao verificar o código: " + e.getMessage())
                );
    }

    private void verifyUnitAndContinue(String nome, String email, String pass, String unitId, String condominioId, DocumentReference codeRef) {
        db.collection("units")
                .document(unitId)
                .get()
                .addOnSuccessListener(unitDoc -> {
                    if (!unitDoc.exists()) {
                        showToast("A unidade informada não existe.");
                        return;
                    }

                    String unitStatus = unitDoc.getString("status");
                    String unitCondominioId = unitDoc.getString("condominioId");

                    if (unitStatus != null && unitStatus.equalsIgnoreCase("INATIVA")) {
                        showToast("Esta unidade está inativa.");
                        return;
                    }

                    if (unitCondominioId == null || !unitCondominioId.equals(condominioId)) {
                        showToast("Esta unidade não pertence ao condomínio do convite.");
                        return;
                    }

                    createUserAndProfile(nome, email, pass, unitId, condominioId, codeRef);
                })
                .addOnFailureListener(e ->
                        showToast("Erro ao verificar a unidade: " + e.getMessage())
                );
    }

    private void createUserAndProfile(String nome, String email, String pass, String unitId, String condominioId, DocumentReference codeRef) {
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();

                    if (firebaseUser == null) {
                        showToast("Não foi possível criar o usuário.");
                        return;
                    }

                    String uid = firebaseUser.getUid();

                    desactivateOldMorador(unitId, condominioId, () -> {
                        Map<String, Object> user = new HashMap<>();
                        user.put("nome", nome);
                        user.put("email", email);
                        user.put("role", "MORADOR");
                        user.put("status", "ATIVO");
                        user.put("unitId", unitId);
                        user.put("unidade", unitId);
                        user.put("condominioId", condominioId);
                        user.put("createdAt", FieldValue.serverTimestamp());

                        db.collection("users")
                                .document(uid)
                                .set(user)
                                .addOnSuccessListener(unused ->
                                        incrementInvitationCodeUsage(codeRef, firebaseUser)
                                )
                                .addOnFailureListener(e ->
                                        rollbackAuthUser(firebaseUser, "Erro ao salvar no Firestore: " + e.getMessage())
                                );
                    });
                })
                .addOnFailureListener(e ->
                        showToast("Erro ao criar conta: " + e.getMessage())
                );
    }

    private void desactivateOldMorador(String unitId, String condominioId, Runnable onComplete) {
        db.collection("users")
                .whereEqualTo("unitId", unitId)
                .whereEqualTo("condominioId", condominioId)
                .whereEqualTo("role", "MORADOR")
                .whereEqualTo("status", "ATIVO")
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        onComplete.run();
                        return;
                    }

                    final int total = query.size();
                    final int[] done = {0};

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        doc.getReference()
                                .update("status", "INATIVO")
                                .addOnSuccessListener(unused -> {
                                    done[0]++;
                                    if (done[0] == total) {
                                        onComplete.run();
                                    }
                                })
                                .addOnFailureListener(e ->
                                        showToast("Erro ao inativar morador anterior: " + e.getMessage())
                                );
                    }
                })
                .addOnFailureListener(e ->
                        showToast("Erro ao verificar moradores ativos: " + e.getMessage())
                );
    }

    private void incrementInvitationCodeUsage(DocumentReference codeRef, FirebaseUser firebaseUser) {
        codeRef.update("uses", FieldValue.increment(1))
                .addOnSuccessListener(unused -> {
                    showToast("Conta criada com sucesso.");

                    auth.signOut();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    db.collection("users")
                            .document(firebaseUser.getUid())
                            .delete()
                            .addOnCompleteListener(task ->
                                    rollbackAuthUser(firebaseUser,
                                            "A conta quase foi criada, mas houve erro ao atualizar o código: " + e.getMessage())
                            );
                });
    }

    private void rollbackAuthUser(FirebaseUser firebaseUser, String errorMessage) {
        if (firebaseUser == null) {
            showToast(errorMessage);
            return;
        }

        firebaseUser.delete()
                .addOnCompleteListener(task -> showToast(errorMessage));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}