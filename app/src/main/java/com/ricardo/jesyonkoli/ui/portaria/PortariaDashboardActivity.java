package com.ricardo.jesyonkoli.ui.portaria;

import android.content.Intent;
import android.os.Bundle;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ricardo.jesyonkoli.R;
import com.ricardo.jesyonkoli.ui.auth.LoginActivity;

import java.util.Calendar;
import java.util.Date;

public class PortariaDashboardActivity extends AppCompatActivity {

    private TextView navPendentes, navNova, navHistorico, tvMenu;
    private TextView tvPendentesCount, tvNovasCount, tvRetiradasCount, tvEmpty;

    private FirebaseFirestore db;

    private int totalPendentes = 0;
    private int totalNovasHoje = 0;
    private int totalRetiradas = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portaria_dashboard);

        db = FirebaseFirestore.getInstance();

        navPendentes = findViewById(R.id.navPendentes);
        navNova = findViewById(R.id.navNova);
        navHistorico = findViewById(R.id.navHistorico);
        tvMenu = findViewById(R.id.tvMenu);

        tvPendentesCount = findViewById(R.id.tvPendentesCount);
        tvNovasCount = findViewById(R.id.tvNovasCount);
        tvRetiradasCount = findViewById(R.id.tvRetiradasCount);
        tvEmpty = findViewById(R.id.tvEmpty);

        // Ajoute espace en haut/bas selon téléphone
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, systemBars.bottom);
            return insets;
        });

        configurarNavegacao();
        configurarMenu();
        carregarEstatisticas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarEstatisticas();
    }

    private void configurarNavegacao() {
        navPendentes.setOnClickListener(v -> {
            Intent intent = new Intent(PortariaDashboardActivity.this, PendentesActivity.class);
            startActivity(intent);
        });

        navNova.setOnClickListener(v -> {
            Intent intent = new Intent(PortariaDashboardActivity.this, NovaEncomendaActivity.class);
            startActivity(intent);
        });

        navHistorico.setOnClickListener(v -> {
            Intent intent = new Intent(PortariaDashboardActivity.this, HistoricoActivity.class);
            startActivity(intent);
        });
    }

    private void configurarMenu() {
        tvMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(PortariaDashboardActivity.this, tvMenu);
            popupMenu.getMenu().add("Profil");
            popupMenu.getMenu().add("Déconnexion");

            popupMenu.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();

                if (title.equals("Profil")) {
                    Toast.makeText(
                            PortariaDashboardActivity.this,
                            "Profil ap vini pita",
                            Toast.LENGTH_SHORT
                    ).show();
                    return true;
                }

                if (title.equals("Déconnexion")) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(PortariaDashboardActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                }

                return false;
            });

            popupMenu.show();
        });
    }

    private void carregarEstatisticas() {
        carregarPendentes();
        carregarRetiradas();
        carregarNovasHoje();
    }

    private void carregarPendentes() {
        db.collection("encomendas")
                .whereEqualTo("status", "PENDENTE")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    totalPendentes = queryDocumentSnapshots.size();
                    tvPendentesCount.setText(String.valueOf(totalPendentes));
                    atualizarMensagem();
                })
                .addOnFailureListener(e -> {
                    totalPendentes = 0;
                    tvPendentesCount.setText("0");
                    atualizarMensagem();
                });
    }

    private void carregarRetiradas() {
        db.collection("encomendas")
                .whereEqualTo("status", "RETIRADA")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    totalRetiradas = queryDocumentSnapshots.size();
                    tvRetiradasCount.setText(String.valueOf(totalRetiradas));
                    atualizarMensagem();
                })
                .addOnFailureListener(e -> {
                    totalRetiradas = 0;
                    tvRetiradasCount.setText("0");
                    atualizarMensagem();
                });
    }

    private void carregarNovasHoje() {
        db.collection("encomendas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int countHoje = 0;

                    Calendar hojeInicio = Calendar.getInstance();
                    hojeInicio.set(Calendar.HOUR_OF_DAY, 0);
                    hojeInicio.set(Calendar.MINUTE, 0);
                    hojeInicio.set(Calendar.SECOND, 0);
                    hojeInicio.set(Calendar.MILLISECOND, 0);

                    Date inicioDoDia = hojeInicio.getTime();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Timestamp timestamp = doc.getTimestamp("createdAt");
                        // Si lakay ou se "createdAt", chanje liy anwo a pou:
                        // Timestamp timestamp = doc.getTimestamp("createdAt");

                        if (timestamp != null && timestamp.toDate().compareTo(inicioDoDia) >= 0) {
                            countHoje++;
                        }
                    }

                    totalNovasHoje = countHoje;
                    tvNovasCount.setText(String.valueOf(totalNovasHoje));
                    atualizarMensagem();
                })
                .addOnFailureListener(e -> {
                    totalNovasHoje = 0;
                    tvNovasCount.setText("0");
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