package com.ricardo.jesyonkoli.ui.portaria;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
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

public class HistoricoActivity extends AppCompatActivity {

    private RecyclerView recyclerHistorico;
    private TextView tvEmptyHistorico;
    private String condominioId;

    private FirebaseFirestore db;
    private final List<Encomenda> listaHistorico = new ArrayList<>();
    private EncomendaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            finish(); // 🔥 retounen sou ekran anvan an
        });

        condominioId = getIntent().getStringExtra("condominioId");

        if (condominioId == null || condominioId.trim().isEmpty()) {
            Toast.makeText(this, "Condomínio não informado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        recyclerHistorico = findViewById(R.id.recyclerHistorico);
        tvEmptyHistorico = findViewById(R.id.tvEmptyHistorico);

        db = FirebaseFirestore.getInstance();

        adapter = new EncomendaAdapter(listaHistorico, encomenda -> {
            if (encomenda == null || encomenda.getId() == null || encomenda.getId().trim().isEmpty()) {
                Toast.makeText(HistoricoActivity.this,
                        "ID da encomenda não encontrado.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent = new Intent(HistoricoActivity.this, DetalheEncomendaActivity.class);
            intent.putExtra("encomendaId", encomenda.getId());

            // NOUVO (pase condominioId)
            intent.putExtra("condominioId", condominioId);

            startActivity(intent);
        });

        recyclerHistorico.setLayoutManager(new LinearLayoutManager(this));
        recyclerHistorico.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarHistorico();
    }

    private void carregarHistorico() {
        db.collection("encomendas")
                .whereEqualTo("status", "RETIRADA")
                .whereEqualTo("condominioId", condominioId) // 🔥 SA KI TE MANKE A
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaHistorico.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Encomenda encomenda = doc.toObject(Encomenda.class);
                        encomenda.setId(doc.getId());
                        listaHistorico.add(encomenda);
                    }

                    adapter.notifyDataSetChanged();

                    if (listaHistorico.isEmpty()) {
                        tvEmptyHistorico.setVisibility(View.VISIBLE);
                    } else {
                        tvEmptyHistorico.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(
                        HistoricoActivity.this,
                        "Erro ao carregar histórico: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
    }
}