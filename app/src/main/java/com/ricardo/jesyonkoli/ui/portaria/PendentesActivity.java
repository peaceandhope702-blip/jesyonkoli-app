package com.ricardo.jesyonkoli.ui.portaria;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ricardo.jesyonkoli.R;
import com.ricardo.jesyonkoli.data.adapter.EncomendaAdapter;
import com.ricardo.jesyonkoli.data.model.Encomenda;

import java.util.ArrayList;
import java.util.List;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

public class PendentesActivity extends AppCompatActivity {

    private RecyclerView recyclerPendentes;
    private EditText etSearchUnidade;

    private String condominioId;

    private FirebaseFirestore db;

    // 🔥 2 LIS
    private final List<Encomenda> listaCompleta = new ArrayList<>();
    private final List<Encomenda> listaFiltrada = new ArrayList<>();

    private EncomendaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pendentes);

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

        recyclerPendentes = findViewById(R.id.recyclerPendentes);
        etSearchUnidade = findViewById(R.id.etSearchUnidade); // 🔥 SEARCH

        db = FirebaseFirestore.getInstance();

        condominioId = getIntent().getStringExtra("condominioId");

        if (condominioId == null || condominioId.trim().isEmpty()) {
            Toast.makeText(this, "Condomínio não informado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        adapter = new EncomendaAdapter(listaFiltrada, encomenda -> {

            if (encomenda == null || encomenda.getId() == null || encomenda.getId().trim().isEmpty()) {
                Toast.makeText(PendentesActivity.this,
                        "ID da encomenda não encontrado.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent = new Intent(PendentesActivity.this, DetalheEncomendaActivity.class);
            intent.putExtra("encomendaId", encomenda.getId());
            intent.putExtra("condominioId", condominioId);

            startActivity(intent);
        });

        recyclerPendentes.setLayoutManager(new LinearLayoutManager(this));
        recyclerPendentes.setAdapter(adapter);

        // 🔥 SEARCH LISTENER
        etSearchUnidade.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrar(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarPendentes();
    }

    private void carregarPendentes() {
        db.collection("encomendas")
                .whereEqualTo("status", "PENDENTE")
                .whereEqualTo("condominioId", condominioId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    listaCompleta.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Encomenda encomenda = doc.toObject(Encomenda.class);
                        encomenda.setId(doc.getId());
                        listaCompleta.add(encomenda);
                    }

                    // 🔥 RESET FILTRE
                    listaFiltrada.clear();
                    listaFiltrada.addAll(listaCompleta);

                    adapter.notifyDataSetChanged();

                    if (listaCompleta.isEmpty()) {
                        Toast.makeText(this, "Nenhuma encomenda pendente.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(
                        PendentesActivity.this,
                        "Erro ao carregar pendentes: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
    }

    // 🔥 FILTRE
    private void filtrar(String texto) {

        listaFiltrada.clear();

        String search = texto.trim().toLowerCase();

        for (Encomenda e : listaCompleta) {

            String unidade = e.getUnidade() != null
                    ? e.getUnidade().trim().toLowerCase()
                    : "";

            if (unidade.contains(search)) {
                listaFiltrada.add(e);
            }
        }

        adapter.notifyDataSetChanged();
    }
}