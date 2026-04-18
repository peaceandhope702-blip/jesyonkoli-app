package com.ricardo.jesyonkoli.ui.portaria;

import android.content.Intent;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ricardo.jesyonkoli.R;
import com.ricardo.jesyonkoli.ui.auth.LoginActivity;

import java.util.Calendar;
import java.util.Date;

public class PortariaDashboardActivity extends AppCompatActivity {

    private static final String TAG = "PORTARIA_DASH";

    private TextView navPendentes, navNova, navHistorico, tvMenu;
    private TextView tvPendentesCount, tvNovasCount, tvRetiradasCount, tvEmpty, tvTituloDashboard;

    // 🔥 BOUTON RAPID
    private Button btnNovaEncomendaRapida, btnVerPendentesRapido, btnVerHistoricoRapido, btnMoradores;

    private FirebaseFirestore db;

    private int totalPendentes = 0;
    private int totalNovasHoje = 0;
    private int totalRetiradas = 0;

    private String condominioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portaria_dashboard);

        db = FirebaseFirestore.getInstance();

        // TEXTS
        tvTituloDashboard = findViewById(R.id.tvTituloDashboard);
        navPendentes = findViewById(R.id.navPendentes);
        navNova = findViewById(R.id.navNova);
        navHistorico = findViewById(R.id.navHistorico);
        tvMenu = findViewById(R.id.tvMenu);

        tvPendentesCount = findViewById(R.id.tvPendentesCount);
        tvNovasCount = findViewById(R.id.tvNovasCount);
        tvRetiradasCount = findViewById(R.id.tvRetiradasCount);
        tvEmpty = findViewById(R.id.tvEmpty);

        // 🔥 BUTTONS RAPID
        btnNovaEncomendaRapida = findViewById(R.id.btnNovaEncomendaRapida);
        btnVerPendentesRapido = findViewById(R.id.btnVerPendentesRapido);
        btnVerHistoricoRapido = findViewById(R.id.btnVerHistoricoRapido);
        btnMoradores = findViewById(R.id.btnMoradores);

        condominioId = getIntent().getStringExtra("condominioId");

        configurarTituloCondominio();
        configurarNavegacao();
        configurarAcoesRapidas();
        configurarMenu();

        if (condominioId != null && !condominioId.trim().isEmpty()) {
            carregarEstatisticas();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottomNav), (v, insets) -> {

            Insets systemBars = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()).toPlatformInsets();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.setPadding(
                        v.getPaddingLeft(),
                        v.getPaddingTop(),
                        v.getPaddingRight(),
                        systemBars.bottom   // 🔥 espas otomatik
                );
            }

            return insets;
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (condominioId != null && !condominioId.trim().isEmpty()) {
            carregarEstatisticas();
        }
    }

    // 🔥 TITRE CONDOMINIO
    private void configurarTituloCondominio() {

        if (condominioId != null && !condominioId.trim().isEmpty()) {
            carregarNomeCondominio(condominioId);
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            tvTituloDashboard.setText("PORTARIA");
            return;
        }

        String uid = currentUser.getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(userDoc -> {

                    if (!userDoc.exists()) {
                        tvTituloDashboard.setText("PORTARIA");
                        return;
                    }

                    String condominioIdUser = userDoc.getString("condominioId");

                    if (condominioIdUser == null) {
                        tvTituloDashboard.setText("PORTARIA");
                        return;
                    }

                    condominioId = condominioIdUser;
                    carregarNomeCondominio(condominioId);
                    carregarEstatisticas();
                });
    }

    private void carregarNomeCondominio(String condominioId) {

        db.collection("condominios")
                .document(condominioId)
                .get()
                .addOnSuccessListener(doc -> {

                    String nome = doc.getString("nome");

                    if (nome == null || nome.isEmpty()) {
                        nome = doc.getString("nomeCondominio");
                    }

                    if (nome != null) {
                        tvTituloDashboard.setText("PORTARIA - " + nome);
                    } else {
                        tvTituloDashboard.setText("PORTARIA");
                    }
                });
    }

    // 🔥 NAVIGATION MENU BAS
    private void configurarNavegacao() {

        navPendentes.setOnClickListener(v -> abrirPendentes());

        navNova.setOnClickListener(v -> abrirNova());

        navHistorico.setOnClickListener(v -> abrirHistorico());
    }

    // 🔥 ACTION RAPIDE
    private void configurarAcoesRapidas() {

        btnNovaEncomendaRapida.setOnClickListener(v -> abrirNova());

        btnVerPendentesRapido.setOnClickListener(v -> abrirPendentes());

        btnVerHistoricoRapido.setOnClickListener(v -> abrirHistorico());

        btnMoradores.setOnClickListener(v ->
                startActivity(new Intent(this, MoradoresActivity.class))
        );
    }

    // 🔥 OUVERTURES CENTRALISÉES
    private void abrirPendentes() {

        if (!validarCondominio()) return;

        Intent intent = new Intent(this, PendentesActivity.class);
        intent.putExtra("condominioId", condominioId);
        startActivity(intent);
    }

    private void abrirNova() {

        if (!validarCondominio()) return;

        Intent intent = new Intent(this, NovaEncomendaActivity.class);
        intent.putExtra("condominioId", condominioId);
        startActivity(intent);
    }

    private void abrirHistorico() {

        if (!validarCondominio()) return;

        Intent intent = new Intent(this, HistoricoActivity.class);
        intent.putExtra("condominioId", condominioId);
        startActivity(intent);
    }

    private boolean validarCondominio() {
        if (condominioId == null || condominioId.trim().isEmpty()) {
            Toast.makeText(this, "Condomínio não carregado ainda", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // 🔥 MENU 3 DOTS
    private void configurarMenu() {

        tvMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, tvMenu);

            popupMenu.getMenu().add("Perfil");
            popupMenu.getMenu().add("Novos Moradores");
            popupMenu.getMenu().add("Sair");

            popupMenu.setOnMenuItemClickListener(item -> {

                String titulo = item.getTitle().toString();

                if (titulo.equals("Novos Moradores")) {
                    startActivity(new Intent(this, UnidadesConviteActivity.class));
                }

                if (titulo.equals("Sair")) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }

                return true;
            });

            popupMenu.show();
        });
    }

    // 🔥 STATISTIQUES
    private void carregarEstatisticas() {
        carregarPendentes();
        carregarRetiradas();
        carregarNovasHoje();
    }

    private void carregarPendentes() {
        db.collection("encomendas")
                .whereEqualTo("status", "PENDENTE")
                .whereEqualTo("condominioId", condominioId)
                .get()
                .addOnSuccessListener(q -> {
                    totalPendentes = q.size();
                    tvPendentesCount.setText(String.valueOf(totalPendentes));
                    atualizarMensagem();
                });
    }

    private void carregarRetiradas() {
        db.collection("encomendas")
                .whereEqualTo("status", "RETIRADA")
                .whereEqualTo("condominioId", condominioId)
                .get()
                .addOnSuccessListener(q -> {
                    totalRetiradas = q.size();
                    tvRetiradasCount.setText(String.valueOf(totalRetiradas));
                    atualizarMensagem();
                });
    }

    private void carregarNovasHoje() {

        db.collection("encomendas")
                .whereEqualTo("condominioId", condominioId)
                .get()
                .addOnSuccessListener(query -> {

                    int countHoje = 0;

                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);

                    Date inicioDoDia = cal.getTime();

                    for (QueryDocumentSnapshot doc : query) {
                        Timestamp ts = doc.getTimestamp("createdAt");

                        if (ts != null && ts.toDate().compareTo(inicioDoDia) >= 0) {
                            countHoje++;
                        }
                    }

                    totalNovasHoje = countHoje;
                    tvNovasCount.setText(String.valueOf(totalNovasHoje));
                    atualizarMensagem();
                });
    }

    private void atualizarMensagem() {

        if (totalPendentes > 0) {
            tvEmpty.setText("Você tem " + totalPendentes + " encomenda(s) pendente(s).");
        } else if (totalNovasHoje > 0) {
            tvEmpty.setText("Hoje chegaram " + totalNovasHoje + " nova(s) encomenda(s).");
        } else if (totalRetiradas > 0) {
            tvEmpty.setText(totalRetiradas + " encomenda(s) já foram retiradas.");
        } else {
            tvEmpty.setText("Nenhuma movimentação no momento.");
        }
    }
}