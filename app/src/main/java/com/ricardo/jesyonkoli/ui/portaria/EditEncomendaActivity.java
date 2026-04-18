package com.ricardo.jesyonkoli.ui.portaria;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ricardo.jesyonkoli.R;

import java.util.HashMap;
import java.util.Map;

public class EditEncomendaActivity extends AppCompatActivity {

    private EditText etDescricao ;
    private Button btnSalvar;

    private FirebaseFirestore db;
    private String encomendaId;
    private String statusAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_encomenda);

        etDescricao = findViewById(R.id.etDescricao);
        btnSalvar = findViewById(R.id.btnSalvar);

        db = FirebaseFirestore.getInstance();

        encomendaId = getIntent().getStringExtra("encomendaId");

        if (encomendaId == null) {
            Toast.makeText(this, "Erro ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        carregarDados();

        btnSalvar.setOnClickListener(v -> salvar());
    }

    private void carregarDados() {
        db.collection("encomendas")
                .document(encomendaId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        finish();
                        return;
                    }

                    statusAtual = doc.getString("status");

                    if (!"PENDENTE".equals(statusAtual)) {
                        Toast.makeText(this, "Pa ka modifye apre retirada", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    etDescricao.setText(doc.getString("descricao"));
                });
    }

    private void salvar() {

        String descricao = etDescricao.getText().toString().trim();
       ;

        if (descricao.isEmpty()) {
            etDescricao.setError("Obrigatório");
            return;
        }

        if (!"PENDENTE".equals(statusAtual)) {
            Toast.makeText(this, "Status pa valab", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSalvar.setEnabled(false);

        Map<String, Object> update = new HashMap<>();
        update.put("descricao", descricao);
        update.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("encomendas")
                .document(encomendaId)
                .update(update)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Atualizado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSalvar.setEnabled(true);
                    Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}