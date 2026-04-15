package com.ricardo.jesyonkoli.ui.morador;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class MoradorHomeActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private ProgressBar progress;
    private TextView tvEmpty, tvLogout, tvSaudacao, tvResumo;
    private TextView tvPendentesCount, tvHistoricoCount;
    private Button btnPendentes, btnHistorico;
    private EditText etBuscar;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private final List<Encomenda> listaPendentes = new ArrayList<>();
    private final List<Encomenda> listaHistorico = new ArrayList<>();
    private final List<Encomenda> listaExibida = new ArrayList<>();

    private MoradorEncomendaAdapter adapter;

    private boolean mostrandoPendentes = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_morador_home);

        recycler = findViewById(R.id.recyclerEncomendas);
        progress = findViewById(R.id.progress);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvLogout = findViewById(R.id.tvLogout);
        tvSaudacao = findViewById(R.id.tvSaudacao);
        tvResumo = findViewById(R.id.tvResumo);
        tvPendentesCount = findViewById(R.id.tvPendentesCount);
        tvHistoricoCount = findViewById(R.id.tvHistoricoCount);
        btnPendentes = findViewById(R.id.btnPendentes);
        btnHistorico = findViewById(R.id.btnHistorico);
        etBuscar = findViewById(R.id.etBuscar);

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

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        btnPendentes.setOnClickListener(v -> {
            mostrandoPendentes = true;
            etBuscar.setText("");
            mostrarPendentes();
        });

        btnHistorico.setOnClickListener(v -> {
            mostrandoPendentes = false;
            etBuscar.setText("");
            mostrarHistorico();
        });

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarEncomendas(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        tvLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(MoradorHomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        configurarSaudacao();
        carregarEncomendas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarEncomendas();
    }

    private void configurarSaudacao() {
        FirebaseUser user = auth.getCurrentUser();

        if (user != null && user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            String email = user.getEmail();
            String nome = email.split("@")[0];
            tvSaudacao.setText("Olá, " + nome);
        } else {
            tvSaudacao.setText("Olá!");
        }
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
                    atualizarResumo();

                    if (mostrandoPendentes) {
                        mostrarPendentes();
                    } else {
                        mostrarHistorico();
                    }
                })
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(
                            MoradorHomeActivity.this,
                            "Erro ao carregar encomendas: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void atualizarResumo() {
        tvPendentesCount.setText(String.valueOf(listaPendentes.size()));
        tvHistoricoCount.setText(String.valueOf(listaHistorico.size()));

        if (!listaPendentes.isEmpty()) {
            tvResumo.setText("Você tem " + listaPendentes.size() + " encomenda(s) pendente(s).");
        } else if (!listaHistorico.isEmpty()) {
            tvResumo.setText("Você já retirou " + listaHistorico.size() + " encomenda(s).");
        } else {
            tvResumo.setText("Nenhuma encomenda encontrada no momento.");
        }
    }

    private void mostrarPendentes() {
        listaExibida.clear();
        listaExibida.addAll(listaPendentes);
        adapter.notifyDataSetChanged();

        btnPendentes.setAlpha(1f);
        btnHistorico.setAlpha(0.7f);

        atualizarEstadoLista();
    }

    private void mostrarHistorico() {
        listaExibida.clear();
        listaExibida.addAll(listaHistorico);
        adapter.notifyDataSetChanged();

        btnHistorico.setAlpha(1f);
        btnPendentes.setAlpha(0.7f);

        atualizarEstadoLista();
    }

    private void filtrarEncomendas(String texto) {
        String busca = normalizar(texto);

        listaExibida.clear();

        List<Encomenda> base = mostrandoPendentes ? listaPendentes : listaHistorico;

        if (busca.isEmpty()) {
            listaExibida.addAll(base);
        } else {
            for (Encomenda encomenda : base) {
                String unidade = normalizar(encomenda.getUnidade());
                String destinatario = normalizar(encomenda.getDestinatario());
                String descricao = normalizar(encomenda.getDescricao());

                if (unidade.contains(busca)
                        || destinatario.contains(busca)
                        || descricao.contains(busca)) {
                    listaExibida.add(encomenda);
                }
            }
        }

        adapter.notifyDataSetChanged();
        atualizarEstadoLista();
    }

    private void atualizarEstadoLista() {
        if (listaExibida.isEmpty()) {
            if (etBuscar.getText().toString().trim().isEmpty()) {
                if (mostrandoPendentes) {
                    tvEmpty.setText("Nenhuma encomenda pendente.");
                } else {
                    tvEmpty.setText("Nenhuma encomenda no histórico.");
                }
            } else {
                tvEmpty.setText("Nenhum resultado encontrado.");
            }
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private String normalizar(String texto) {
        if (texto == null) {
            return "";
        }

        String textoNormalizado = texto.trim().toLowerCase();

        return Normalizer.normalize(textoNormalizado, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}