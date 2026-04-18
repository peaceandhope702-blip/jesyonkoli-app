package com.ricardo.jesyonkoli.ui.portaria;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ricardo.jesyonkoli.R;

import java.io.File;
import java.util.ArrayList;

public class DetalheEncomendaActivity extends AppCompatActivity {

    private TextView tvDestinatario, tvUnidade, tvDescricao, tvStatus;
    private ImageView imgFotoDetalhe, imgAssinaturaDetalhe;

    private Button btnRetirarSomente;
    private Button btnRetirarTodos;
    private Button btnEditar;

    private FirebaseFirestore db;
    private String encomendaId;
    private String statusAtual;

    // 🔥 GROUP RETIRADA
    private String unidadeAtual;
    private ArrayList<String> encomendasPendentesIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_encomenda);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvDestinatario = findViewById(R.id.tvDestinatarioDetalhe);
        tvUnidade = findViewById(R.id.tvUnidadeDetalhe);
        tvDescricao = findViewById(R.id.tvDescricaoDetalhe);
        tvStatus = findViewById(R.id.tvStatusDetalhe);
        imgFotoDetalhe = findViewById(R.id.imgFotoDetalhe);
        imgAssinaturaDetalhe = findViewById(R.id.imgAssinaturaDetalhe);

        btnRetirarSomente = findViewById(R.id.btnRetirarSomente);
        btnRetirarTodos = findViewById(R.id.btnRetirarTodos);
        btnEditar = findViewById(R.id.btnEditar);

        db = FirebaseFirestore.getInstance();

        encomendaId = getIntent().getStringExtra("encomendaId");

        if (encomendaId == null || encomendaId.isEmpty()) {
            Toast.makeText(this, "ID da encomenda não recebido", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 🔵 Retirar somente este
        btnRetirarSomente.setOnClickListener(v -> {
            Intent intent = new Intent(this, AssinaturaActivity.class);
            intent.putExtra("encomendaId", encomendaId);
            startActivity(intent);
        });

        // 🟢 Retirar todos
        btnRetirarTodos.setOnClickListener(v -> {
            Intent intent = new Intent(this, AssinaturaActivity.class);
            intent.putStringArrayListExtra("listaEncomendasIds", encomendasPendentesIds);
            startActivity(intent);
        });

        // EDIT
        btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditEncomendaActivity.class);
            intent.putExtra("encomendaId", encomendaId);
            startActivity(intent);
        });

        carregarDetalhes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarDetalhes();
    }

    private void carregarDetalhes() {
        db.collection("encomendas")
                .document(encomendaId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "Encomenda não encontrada", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    String destinatario = documentSnapshot.getString("destinatario");
                    String unidade = documentSnapshot.getString("unidade");
                    String descricao = documentSnapshot.getString("descricao");
                    String status = documentSnapshot.getString("status");
                    String assinaturaBase64 = documentSnapshot.getString("assinaturaBase64");
                    String fotoLocalPath = documentSnapshot.getString("fotoLocalPath");

                    statusAtual = status;
                    unidadeAtual = unidade;

                    tvDestinatario.setText(destinatario != null ? destinatario : "--");
                    tvUnidade.setText(unidade != null ? unidade : "--");
                    tvDescricao.setText(descricao != null ? descricao : "--");
                    tvStatus.setText(status != null ? status : "--");

                    // FOTO
                    if (fotoLocalPath != null && !fotoLocalPath.isEmpty()) {
                        File fotoFile = new File(fotoLocalPath);
                        if (fotoFile.exists()) {
                            Bitmap fotoBitmap = BitmapFactory.decodeFile(fotoFile.getAbsolutePath());
                            imgFotoDetalhe.setImageBitmap(fotoBitmap);
                        } else {
                            imgFotoDetalhe.setImageResource(android.R.color.transparent);
                        }
                    } else {
                        imgFotoDetalhe.setImageResource(android.R.color.transparent);
                    }

                    // ASSINATURA
                    if (assinaturaBase64 != null && !assinaturaBase64.isEmpty()) {
                        byte[] bytes = Base64.decode(assinaturaBase64, Base64.DEFAULT);
                        Bitmap assinaturaBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imgAssinaturaDetalhe.setImageBitmap(assinaturaBitmap);
                    } else {
                        imgAssinaturaDetalhe.setImageResource(android.R.color.transparent);
                    }

                    // 🔴 SI DEJA RETIRADA
                    if ("RETIRADA".equals(statusAtual)) {

                        btnRetirarSomente.setEnabled(false);
                        btnRetirarTodos.setEnabled(false);

                        btnRetirarSomente.setText("Já retirada");
                        btnRetirarTodos.setText("Já retirada");

                    } else {

                        btnRetirarSomente.setEnabled(true);
                    }

                    // EDIT BUTTON
                    btnEditar.setEnabled("PENDENTE".equals(statusAtual));

                    // 🔥 CHACHE LOT COLIS
                    buscarEncomendasPendentesMesmaUnidade();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar detalhes: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // 🔥 GROUP QUERY
    private void buscarEncomendasPendentesMesmaUnidade() {

        if (unidadeAtual == null) return;

        db.collection("encomendas")
                .whereEqualTo("unidade", unidadeAtual)
                .whereEqualTo("status", "PENDENTE")
                .get()
                .addOnSuccessListener(query -> {

                    encomendasPendentesIds.clear();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        encomendasPendentesIds.add(doc.getId());
                    }

                    int total = encomendasPendentesIds.size();

                    if (!"RETIRADA".equals(statusAtual)) {

                        if (total > 1) {
                            btnRetirarTodos.setEnabled(true);
                            btnRetirarTodos.setText("Retirar todos (" + total + ")");
                        } else {
                            btnRetirarTodos.setEnabled(false);
                            btnRetirarTodos.setText("Retirar todos");
                        }
                    }
                });
    }
}