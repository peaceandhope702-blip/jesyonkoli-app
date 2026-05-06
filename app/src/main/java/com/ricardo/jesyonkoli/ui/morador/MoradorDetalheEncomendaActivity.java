package com.ricardo.jesyonkoli.ui.morador;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
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
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

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

        try {

            if (!documentSnapshot.exists()) {
                Toast.makeText(this, "Encomenda não encontrada", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            String fotoUrl = documentSnapshot.getString("fotoUrl");
            String fotoLocalPath = documentSnapshot.getString("fotoLocalPath");

            String destinatario = documentSnapshot.getString("destinatario");
            String unidade = documentSnapshot.getString("unidade");
            String descricao = documentSnapshot.getString("descricao");
            String status = documentSnapshot.getString("status");
            String assinaturaBase64 = documentSnapshot.getString("assinaturaBase64");

            Timestamp createdAt = documentSnapshot.getTimestamp("createdAt");
            Timestamp retiradaAt = documentSnapshot.getTimestamp("retiradaAt");

            tvDestinatarioDetalhe.setText(safeText(destinatario, "Não informado"));
            tvUnidadeDetalhe.setText(safeText(unidade, "Não informada"));
            tvDescricaoDetalhe.setText(safeText(descricao, "Sem descrição"));
            tvStatusDetalhe.setText(safeText(status, "PENDENTE"));
            tvCreatedAtDetalhe.setText(formatTimestamp(createdAt));

            if (retiradaAt != null) {
                tvRetiradaAtDetalhe.setVisibility(View.VISIBLE);
                tvRetiradaAtDetalhe.setText("Retirada em: " + formatTimestamp(retiradaAt));
            } else {
                tvRetiradaAtDetalhe.setVisibility(View.GONE);
            }

            // FOTO
            if (fotoUrl != null && !fotoUrl.isEmpty()) {

                imgFotoDetalhe.setVisibility(View.VISIBLE);


                Glide.with(this)
                        .load(fotoUrl)
                        .placeholder(R.drawable.ic_camera_placeholder)
                        .error(R.drawable.test_image) // ou deja genyen li
                        .into(imgFotoDetalhe);

                imgFotoDetalhe.setOnClickListener(v -> {

                    if (fotoUrl != null && !fotoUrl.isEmpty()) {
                        Intent intent = new Intent(this, FotoZoomActivity.class);
                        intent.putExtra("fotoUrl", fotoUrl);
                        startActivity(intent);
                    }

                });

            } else if (fotoLocalPath != null && !fotoLocalPath.isEmpty()) {

                File file = new File(fotoLocalPath);

                if (file.exists()) {
                    imgFotoDetalhe.setVisibility(View.VISIBLE);

                    Glide.with(this)
                            .load(file)
                            .into(imgFotoDetalhe);
                } else {
                    imgFotoDetalhe.setVisibility(View.GONE);
                }

            } else {
                imgFotoDetalhe.setVisibility(View.GONE);
            }

            // ASSINATURA
            if (assinaturaBase64 != null && !assinaturaBase64.isEmpty()) {
                try {
                    byte[] bytes = Base64.decode(assinaturaBase64, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imgAssinaturaDetalhe.setImageBitmap(bitmap);
                    imgAssinaturaDetalhe.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    imgAssinaturaDetalhe.setVisibility(View.GONE);
                }
            } else {
                imgAssinaturaDetalhe.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Toast.makeText(this, "Erro interno: " + e.getMessage(), Toast.LENGTH_LONG).show();
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