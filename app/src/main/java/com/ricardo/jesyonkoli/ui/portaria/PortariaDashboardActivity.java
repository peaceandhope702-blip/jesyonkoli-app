package com.ricardo.jesyonkoli.ui.portaria;

import android.content.Intent;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ricardo.jesyonkoli.R;
import com.ricardo.jesyonkoli.ui.auth.LoginActivity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;

public class PortariaDashboardActivity extends AppCompatActivity {

    private static final String TAG = "PORTARIA_DASH";

    private TextView  tvMenu;
    private TextView tvPendentesCount, tvNovasCount, tvRetiradasCount,tvAlertTitle,  tvTituloDashboard;

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

        View root = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()).toPlatformInsets();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.setPadding(
                        v.getPaddingLeft(),
                        v.getPaddingTop(),
                        v.getPaddingRight(),
                        systemBars.bottom
                );
            }

            return insets;
        });

        db = FirebaseFirestore.getInstance();

        // TEXTS
        tvTituloDashboard = findViewById(R.id.tvTituloDashboard);

        tvMenu = findViewById(R.id.tvMenu);

        tvPendentesCount = findViewById(R.id.tvPendentesCount);
        tvNovasCount = findViewById(R.id.tvNovasCount);
        tvRetiradasCount = findViewById(R.id.tvRetiradasCount);
        tvAlertTitle = findViewById(R.id.tvAlertTitle);

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
                        tvTituloDashboard.setText("Condomínio- " + nome);
                    } else {
                        tvTituloDashboard.setText("PORTARIA");
                    }
                });
    }

    // 🔥 NAVIGATION MENU BAS
    private void configurarNavegacao() {

        FloatingActionButton fab = findViewById(R.id.fabNova);
        fab.setOnClickListener(v -> abrirNova());

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

            View view = getLayoutInflater().inflate(R.layout.bottom_sheet_menu, null);

            BottomSheetDialog dialog = new BottomSheetDialog(this);
            dialog.setContentView(view);

            // Bouton yo
            LinearLayout btnAdicionar = view.findViewById(R.id.btnAdicionar);
            LinearLayout btnConfig = view.findViewById(R.id.btnConfig);
            LinearLayout btnAjuda = view.findViewById(R.id.btnAjuda);
            LinearLayout btnSair = view.findViewById(R.id.btnSair);

            btnAdicionar.setOnClickListener(v1 -> {
                startActivity(new Intent(this, UnidadesConviteActivity.class));
                dialog.dismiss();
            });

            btnConfig.setOnClickListener(v1 -> {
                // TODO abrir configurações
                dialog.dismiss();
            });

            btnAjuda.setOnClickListener(v1 -> {
                // TODO abrir suporte
                dialog.dismiss();
            });

            btnSair.setOnClickListener(v1 -> {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                dialog.dismiss();
            });

            dialog.show();
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

        View alert = findViewById(R.id.alertContainer);

        if (totalPendentes > 0) {

            alert.setVisibility(View.VISIBLE);
            tvAlertTitle.setText(totalPendentes + " encomendas aguardando retirada");

        } else if (totalNovasHoje > 0) {

            alert.setVisibility(View.VISIBLE);
            tvAlertTitle.setText("Hoje chegaram " + totalNovasHoje + " encomendas");

        } else if (totalRetiradas > 0) {

            alert.setVisibility(View.VISIBLE);
            tvAlertTitle.setText(totalRetiradas + " encomendas já foram retiradas");

        } else {

            alert.setVisibility(View.GONE); // 🔥 kle a
        }
    }
}