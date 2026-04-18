package com.ricardo.jesyonkoli.ui.portaria;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import java.io.OutputStream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.ricardo.jesyonkoli.R;
import com.ricardo.jesyonkoli.data.adapter.EncomendaAdapter;
import com.ricardo.jesyonkoli.data.model.Encomenda;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class HistoricoActivity extends AppCompatActivity {

    private RecyclerView recyclerHistorico;
    private EditText etSearchGlobal;
    private Button btnExportPdf;

    private FirebaseFirestore db;
    private String condominioId;

    private final List<Encomenda> listaCompleta = new ArrayList<>();
    private final List<Encomenda> listaFiltrada = new ArrayList<>();

    private EncomendaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        View root = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    systemBars.bottom
            );

            return insets;
        });

        recyclerHistorico = findViewById(R.id.recyclerHistorico);
        etSearchGlobal = findViewById(R.id.etSearchGlobal);
        btnExportPdf = findViewById(R.id.btnExportPdf);

        db = FirebaseFirestore.getInstance();

        condominioId = getIntent().getStringExtra("condominioId");

        if (condominioId == null || condominioId.trim().isEmpty()) {
            Toast.makeText(this, "Condomínio não informado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        adapter = new EncomendaAdapter(listaFiltrada, encomenda -> {
            if (encomenda == null || encomenda.getId() == null) return;

            Intent intent = new Intent(HistoricoActivity.this, DetalheEncomendaActivity.class);
            intent.putExtra("encomendaId", encomenda.getId());
            intent.putExtra("condominioId", condominioId);
            startActivity(intent);
        });

        recyclerHistorico.setLayoutManager(new LinearLayoutManager(this));
        recyclerHistorico.setAdapter(adapter);

        etSearchGlobal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrar(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnExportPdf.setOnClickListener(v -> exportarPDF());
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarHistorico();
    }

    private void carregarHistorico() {
        db.collection("encomendas")
                .whereEqualTo("status", "RETIRADA")
                .whereEqualTo("condominioId", condominioId)
                .get()
                .addOnSuccessListener(query -> {

                    listaCompleta.clear();

                    for (QueryDocumentSnapshot doc : query) {
                        Encomenda e = doc.toObject(Encomenda.class);
                        e.setId(doc.getId());
                        listaCompleta.add(e);
                    }

                    listaFiltrada.clear();
                    listaFiltrada.addAll(listaCompleta);

                    adapter.notifyDataSetChanged();

                    if (listaCompleta.isEmpty()) {
                        Toast.makeText(this, "Nenhum histórico encontrado.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void filtrar(String texto) {
        listaFiltrada.clear();

        String search = texto.trim().toLowerCase();

        for (Encomenda e : listaCompleta) {
            String unidade = e.getUnidade() != null ? e.getUnidade().toLowerCase() : "";
            String morador = e.getDestinatario() != null ? e.getDestinatario().toLowerCase() : "";
            String descricao = e.getDescricao() != null ? e.getDescricao().toLowerCase() : "";

            if (unidade.contains(search)
                    || morador.contains(search)
                    || descricao.contains(search)) {
                listaFiltrada.add(e);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void exportarPDF() {
        Document document = null;

        try {
            String fileName = "historico_encomendas_" + System.currentTimeMillis() + ".pdf";

            OutputStream outputStream;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                if (uri == null) {
                    Toast.makeText(this, "Erro ao criar arquivo no Downloads", Toast.LENGTH_LONG).show();
                    return;
                }

                outputStream = getContentResolver().openOutputStream(uri);

                if (outputStream == null) {
                    Toast.makeText(this, "Erro ao abrir arquivo para escrita", Toast.LENGTH_LONG).show();
                    return;
                }

            } else {
                File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                File file = new File(folder, fileName);
                outputStream = new FileOutputStream(file);
            }

            document = new Document();
            PdfWriter.getInstance(document, outputStream);

            document.open();

            document.add(new Paragraph("HISTÓRICO DE ENCOMENDAS\n\n"));

            for (Encomenda e : listaFiltrada) {
                String linha =
                        "Unidade: " + (e.getUnidade() != null ? e.getUnidade() : "--") + "\n" +
                                "Morador: " + (e.getDestinatario() != null ? e.getDestinatario() : "--") + "\n" +
                                "Descrição: " + (e.getDescricao() != null ? e.getDescricao() : "--") + "\n" +
                                "----------------------------------------\n";

                document.add(new Paragraph(linha));
            }

            document.close();

            Toast.makeText(this, "PDF salvo em Downloads", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            if (document != null && document.isOpen()) {
                document.close();
            }
            Toast.makeText(this, "Erro ao gerar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}