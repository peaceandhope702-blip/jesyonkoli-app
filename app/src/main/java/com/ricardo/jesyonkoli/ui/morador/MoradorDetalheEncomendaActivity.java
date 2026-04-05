package com.ricardo.jesyonkoli.ui.morador;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ricardo.jesyonkoli.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MoradorDetalheEncomendaActivity extends AppCompatActivity {

    private TextView tvDestinatarioDetalhe;
    private TextView tvUnidadeDetalhe;
    private TextView tvDescricaoDetalhe;
    private TextView tvStatusDetalhe;
    private TextView tvCreatedAtDetalhe;
    private TextView tvRetiradaAtDetalhe;

    private ImageView imgFotoDetalhe;
    private ImageView imgAssinaturaDetalhe;

    private FirebaseFirestore db;
    private String encomendaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_morador_detalhe_encomenda);

        tvDestinatarioDetalhe = findViewById(R.id.tvDestinatarioDetalhe);
        tvUnidadeDetalhe = findViewById(R.id.tvUnidadeDetalhe);
        tvDescricaoDetalhe = findViewById(R.id.tvDescricaoDetalhe);
        tvStatusDetalhe = findViewById(R.id.tvStatusDetalhe);
        tvCreatedAtDetalhe = findViewById(R.id.tvCreatedAtDetalhe);
        tvRetiradaAtDetalhe = findViewById(R.id.tvRetiradaAtDetalhe);

        imgFotoDetalhe = findViewById(R.id.imgFotoDetalhe);
        imgAssinaturaDetalhe = findViewById(R.id.imgAssinaturaDetalhe);

        db = FirebaseFirestore.getInstance();
        encomendaId = getIntent().getStringExtra("encomendaId");

        if (encomendaId == null || encomendaId.trim().isEmpty()) {
            Toast.makeText(this, "ID da encomenda não recebido", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        carregarDetalhes();
    }

    private void carregarDetalhes() {
        db.collection("encomendas")
                .document(encomendaId)
                .get()
                .addOnSuccessListener(this::preencherTela)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar detalhes: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void preencherTela(DocumentSnapshot documentSnapshot) {
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

        Timestamp createdAt = documentSnapshot.getTimestamp("createdAt");
        Timestamp retiradaAt = documentSnapshot.getTimestamp("retiradaAt");

        tvDestinatarioDetalhe.setText("Destinatário: " + safeText(destinatario, "Não informado"));
        tvUnidadeDetalhe.setText("Unidade: " + safeText(unidade, "Não informada"));
        tvDescricaoDetalhe.setText("Descrição: " + safeText(descricao, "Sem descrição"));
        tvStatusDetalhe.setText("Status: " + safeText(status, "PENDENTE"));
        tvCreatedAtDetalhe.setText("Recebida em: " + formatTimestamp(createdAt));

        if (retiradaAt != null) {
            tvRetiradaAtDetalhe.setVisibility(View.VISIBLE);
            tvRetiradaAtDetalhe.setText("Retirada em: " + formatTimestamp(retiradaAt));
        } else {
            tvRetiradaAtDetalhe.setVisibility(View.GONE);
        }

        if (fotoLocalPath != null && !fotoLocalPath.trim().isEmpty()) {
            File fotoFile = new File(fotoLocalPath);
            if (fotoFile.exists()) {
                Bitmap fotoBitmap = BitmapFactory.decodeFile(fotoFile.getAbsolutePath());
                imgFotoDetalhe.setImageBitmap(fotoBitmap);
                imgFotoDetalhe.setVisibility(View.VISIBLE);
            } else {
                imgFotoDetalhe.setVisibility(View.GONE);
            }
        } else {
            imgFotoDetalhe.setVisibility(View.GONE);
        }

        if (assinaturaBase64 != null && !assinaturaBase64.trim().isEmpty()) {
            try {
                byte[] bytes = Base64.decode(assinaturaBase64, Base64.DEFAULT);
                Bitmap assinaturaBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imgAssinaturaDetalhe.setImageBitmap(assinaturaBitmap);
                imgAssinaturaDetalhe.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                imgAssinaturaDetalhe.setVisibility(View.GONE);
            }
        } else {
            imgAssinaturaDetalhe.setVisibility(View.GONE);
        }
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return "--/--/---- --:--";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(timestamp.toDate());
    }
}