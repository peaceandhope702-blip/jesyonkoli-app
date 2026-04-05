package com.ricardo.jesyonkoli.ui.morador;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ricardo.jesyonkoli.R;
import com.ricardo.jesyonkoli.data.adapter.MoradorEncomendaAdapter;
import com.ricardo.jesyonkoli.data.model.Encomenda;
import com.ricardo.jesyonkoli.ui.auth.LoginActivity;


import java.util.ArrayList;
import java.util.List;

public class MoradorHomeActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private ProgressBar progress;
    private TextView tvEmpty;
    private Button btnPendentes, btnHistorico;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Button btnLogout;

    private final List<Encomenda> listaPendentes = new ArrayList<>();
    private final List<Encomenda> listaHistorico = new ArrayList<>();
    private final List<Encomenda> listaExibida = new ArrayList<>();

    private MoradorEncomendaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_morador_home);

        recycler = findViewById(R.id.recyclerEncomendas);
        progress = findViewById(R.id.progress);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnPendentes = findViewById(R.id.btnPendentes);
        btnHistorico = findViewById(R.id.btnHistorico);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        adapter = new MoradorEncomendaAdapter(listaExibida, encomenda -> {
            if (encomenda == null || encomenda.getId() == null || encomenda.getId().trim().isEmpty()) {
                Toast.makeText(this, "ID da encomenda não encontrado.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, MoradorDetalheEncomendaActivity.class);
            intent.putExtra("encomendaId", encomenda.getId());
            startActivity(intent);



        });
        btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {

            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(MoradorHomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
        });





        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        btnPendentes.setOnClickListener(v -> mostrarPendentes());
        btnHistorico.setOnClickListener(v -> mostrarHistorico());

        carregarEncomendas();
    }

    private void carregarEncomendas() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        db.collection("encomendas")
                .whereEqualTo("moradorId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaPendentes.clear();
                    listaHistorico.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Encomenda encomenda = doc.toObject(Encomenda.class);
                        encomenda.setId(doc.getId());

                        String status = encomenda.getStatus();

                        if (status != null && status.equalsIgnoreCase("RETIRADA")) {
                            listaHistorico.add(encomenda);
                        } else {
                            listaPendentes.add(encomenda);
                        }
                    }

                    progress.setVisibility(View.GONE);
                    mostrarPendentes();
                })
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(MoradorHomeActivity.this,
                            "Erro ao carregar encomendas: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void mostrarPendentes() {
        listaExibida.clear();
        listaExibida.addAll(listaPendentes);
        adapter.notifyDataSetChanged();

        if (listaExibida.isEmpty()) {
            tvEmpty.setText("Nenhuma encomenda pendente.");
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void mostrarHistorico() {
        listaExibida.clear();
        listaExibida.addAll(listaHistorico);
        adapter.notifyDataSetChanged();

        if (listaExibida.isEmpty()) {
            tvEmpty.setText("Nenhuma encomenda no histórico.");
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }
}