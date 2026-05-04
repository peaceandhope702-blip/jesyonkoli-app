package com.ricardo.jesyonkoli.ui.portaria;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import com.bumptech.glide.Glide;

// 🔥 NOUVO IMPORT (Snackbar + vibration)
import com.google.android.material.snackbar.Snackbar;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ricardo.jesyonkoli.R;

import java.io.File;
import java.util.ArrayList;


public class DetalheEncomendaActivity extends AppCompatActivity {

    private TextView tvDestinatario, tvSubInfo, tvStatus,  tvTotalEncomendas;
    private ImageView imgFotoDetalhe, imgAssinaturaDetalhe;

    private Button btnRetirarSomente;
    private Button btnRetirarTodos;
    private Button btnEditar;
    private String fotoLocalPathGlobal;

    private FirebaseFirestore db;
    private String encomendaId;
    private String statusAtual;

    // 🔥 GROUP RETIRADA
    private String unidadeAtual;
    private ArrayList<String> encomendasPendentesIds = new ArrayList<>();

    // 🔥 SNACKBAR PRO (anchor + couleur + vibration)
    private void showSnack(String message, boolean isSuccess) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);

        // 👉 anchor sou bouton prensipal
        snackbar.setAnchorView(btnRetirarSomente);

        // 🎨 COULEUR
        if (isSuccess) {
            snackbar.setBackgroundTint(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            snackbar.setBackgroundTint(getResources().getColor(android.R.color.holo_red_dark));
        }
        // 📳 VIBRATION
        try {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(
                            isSuccess ? 80 : 200,
                            VibrationEffect.DEFAULT_AMPLITUDE
                    ));
                } else {
                    vibrator.vibrate(isSuccess ? 80 : 200);
                }
            }
        } catch (Exception ignored) {}

        snackbar.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_encomenda);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvDestinatario = findViewById(R.id.tvDestinatarioDetalhe);
        tvStatus = findViewById(R.id.tvStatusDetalhe);
        imgFotoDetalhe = findViewById(R.id.imgFotoDetalhe);
        imgAssinaturaDetalhe = findViewById(R.id.imgAssinaturaDetalhe);
        tvTotalEncomendas = findViewById(R.id.tvTotalEncomendas);
        tvSubInfo = findViewById(R.id.tvSubInfo);

        btnRetirarSomente = findViewById(R.id.btnRetirarSomente);
        btnRetirarTodos = findViewById(R.id.btnRetirarTodos);
        btnEditar = findViewById(R.id.btnEditar);

        db = FirebaseFirestore.getInstance();

        encomendaId = getIntent().getStringExtra("encomendaId");

        if (encomendaId == null || encomendaId.isEmpty()) {
            showSnack("ID da encomenda não recebido", false);
            finish();
            return;
        }
        imgFotoDetalhe.setOnClickListener(v -> {

            if (fotoLocalPathGlobal != null && !fotoLocalPathGlobal.isEmpty()) {

                Intent intent = new Intent(this, VisualizarFotoActivity.class);
                intent.putExtra("fotoPath", fotoLocalPathGlobal);
                startActivity(intent);

            } else {
                showSnack("Foto indisponível", false);
            }

        });


        // 🔵 Retirar somente este
        btnRetirarSomente.setOnClickListener(v -> {

            if (encomendasPendentesIds.size() > 1) {

                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Retirar encomendas")
                        .setItems(new CharSequence[]{
                                "Somente esta",
                                "Todas da unidade"
                        }, (dialog, which) -> {

                            if (which == 0) {

                                Intent intent = new Intent(this, AssinaturaActivity.class);
                                intent.putExtra("encomendaId", encomendaId);
                                startActivity(intent);

                            } else {

                                Intent intent = new Intent(this, AssinaturaActivity.class);
                                intent.putStringArrayListExtra("listaEncomendasIds", encomendasPendentesIds);
                                startActivity(intent);

                            }

                        })
                        .show();

            } else {

                Intent intent = new Intent(this, AssinaturaActivity.class);
                intent.putExtra("encomendaId", encomendaId);
                startActivity(intent);

            }
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
                        showSnack("Encomenda não encontrada", false);
                        finish();
                        return;
                    }

                    String destinatario = documentSnapshot.getString("destinatario");
                    String unidade = documentSnapshot.getString("unidade");
                    String descricao = documentSnapshot.getString("descricao");
                    String status = documentSnapshot.getString("status");
                    String assinaturaBase64 = documentSnapshot.getString("assinaturaBase64");
                    String fotoLocalPath = documentSnapshot.getString("fotoLocalPath");
                    fotoLocalPathGlobal = fotoLocalPath;

                    int total = encomendasPendentesIds.size();
                    statusAtual = status;
                    unidadeAtual = unidade;

                    tvDestinatario.setText(destinatario != null ? destinatario : "--");
                    String subInfo = (unidade != null ? unidade : "--") +
                            " • " +
                            (descricao != null ? descricao : "--");

                    tvSubInfo.setText(subInfo);
                    tvStatus.setText(status != null ? status : "--");

                    // FOTO
                    // FOTO (GLIDE PRO FIX)
                    if (fotoLocalPath != null && !fotoLocalPath.isEmpty()) {

                        File fotoFile = new File(fotoLocalPath);

                        if (fotoFile.exists()) {

                            Glide.with(this)
                                    .load(fotoFile)
                                    .error(R.drawable.test_image) // si li pa jwenn foto
                                    .into(imgFotoDetalhe);

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
                        showSnack("Erro ao carregar detalhes", false)
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

                    // ✅ UPDATE UI ISIT LA
                    TextView tvTotalEncomendas = findViewById(R.id.tvTotalEncomendas);
                    tvTotalEncomendas.setText(total + " encomendas pendentes");

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
