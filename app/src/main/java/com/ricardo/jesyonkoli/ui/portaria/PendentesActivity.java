package com.ricardo.jesyonkoli.ui.portaria;

import android.content.Intent;
import android.os.Bundle;
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

public class PendentesActivity extends AppCompatActivity {

    private RecyclerView recyclerPendentes;

    private FirebaseFirestore db;
    private final List<Encomenda> listaEncomendas = new ArrayList<>();
    private EncomendaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pendentes);

        recyclerPendentes = findViewById(R.id.recyclerPendentes);
        db = FirebaseFirestore.getInstance();

        adapter = new EncomendaAdapter(listaEncomendas, encomenda -> {
            if (encomenda == null || encomenda.getId() == null || encomenda.getId().trim().isEmpty()) {
                Toast.makeText(PendentesActivity.this,
                        "ID da encomenda não encontrado.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent = new Intent(PendentesActivity.this, DetalheEncomendaActivity.class);
            intent.putExtra("encomendaId", encomenda.getId());
            startActivity(intent);
        });

        recyclerPendentes.setLayoutManager(new LinearLayoutManager(this));
        recyclerPendentes.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarPendentes();
    }

    private void carregarPendentes() {
        db.collection("encomendas")
                .whereEqualTo("status", "PENDENTE")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaEncomendas.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Encomenda encomenda = doc.toObject(Encomenda.class);
                        encomenda.setId(doc.getId());
                        listaEncomendas.add(encomenda);
                    }

                    adapter.notifyDataSetChanged();

                    if (listaEncomendas.isEmpty()) {
                        Toast.makeText(this, "Nenhuma encomenda pendente.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(
                        PendentesActivity.this,
                        "Erro ao carregar pendentes: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
    }
}