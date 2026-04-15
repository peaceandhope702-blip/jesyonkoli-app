package com.ricardo.jesyonkoli.ui.portaria;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
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

        tvTituloDashboard = findViewById(R.id.tvTituloDashboard);
        navPendentes = findViewById(R.id.navPendentes);
        navNova = findViewById(R.id.navNova);
        navHistorico = findViewById(R.id.navHistorico);
        tvMenu = findViewById(R.id.tvMenu);

        tvPendentesCount = findViewById(R.id.tvPendentesCount);
        tvNovasCount = findViewById(R.id.tvNovasCount);
        tvRetiradasCount = findViewById(R.id.tvRetiradasCount);
        tvEmpty = findViewById(R.id.tvEmpty);

        condominioId = getIntent().getStringExtra("condominioId");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, systemBars.bottom);
            return insets;
        });

        configurarTituloCondominio();
        configurarNavegacao();
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

    private void configurarTituloCondominio() {
        Log.d(TAG, "=== configurarTituloCondominio INICIO ===");
        tvTituloDashboard.setText("Carregando...");

        if (condominioId != null && !condominioId.trim().isEmpty()) {
            Log.d(TAG, "condominioId veio pela Intent: " + condominioId);
            carregarNomeCondominio(condominioId);
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Log.e(TAG, "currentUser == null");
            tvTituloDashboard.setText("Olá 👋");
            return;
        }

        String uid = currentUser.getUid();
        Log.d(TAG, "UID logado: " + uid);

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(userDoc -> {
                    Log.d(TAG, "users/{uid} exists? " + userDoc.exists());

                    if (!userDoc.exists()) {
                        Log.e(TAG, "Documento do usuário não encontrado");
                        tvTituloDashboard.setText("PORTARIA- ");
                        return;
                    }

                    Log.d(TAG, "Dados do usuário: " + userDoc.getData());

                    String condominioIdUser = userDoc.getString("condominioId");
                    Log.d(TAG, "condominioId no user: " + condominioIdUser);

                    if (condominioIdUser == null || condominioIdUser.trim().isEmpty()) {
                        Log.e(TAG, "condominioId vazio no user");
                        tvTituloDashboard.setText("PORTARIA-");
                        return;
                    }

                    condominioId = condominioIdUser;
                    carregarNomeCondominio(condominioId);
                    carregarEstatisticas();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao buscar usuário", e);
                    tvTituloDashboard.setText("PORTARIA-");
                });
    }
    private void carregarNomeCondominio(String condominioId) {
        Log.d(TAG, "Buscando condomínio: " + condominioId);

        db.collection("condominios")
                .document(condominioId)
                .get()
                .addOnSuccessListener(doc -> {
                    Log.d(TAG, "condominio exists? " + doc.exists());

                    if (!doc.exists()) {
                        Log.e(TAG, "Documento do condomínio não existe");
                        tvTituloDashboard.setText("PORTARIA-");
                        return;
                    }

                    Log.d(TAG, "Dados do condomínio: " + doc.getData());

                    String nome = doc.getString("nome");
                    Log.d(TAG, "nome lido: " + nome);

                    if (nome == null || nome.trim().isEmpty()) {
                        nome = doc.getString("nomeCondominio");
                        Log.d(TAG, "nomeCondominio fallback: " + nome);
                    }

                    if (nome != null && !nome.trim().isEmpty()) {
                        String titulo = "PORTARIA- " + nome ;
                        tvTituloDashboard.setText(titulo);
                        Log.d(TAG, "Título final aplicado: " + titulo);
                    } else {
                        Log.e(TAG, "Nenhum nome encontrado no documento");
                        tvTituloDashboard.setText("PORTARIA- ");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao buscar condomínio", e);
                    tvTituloDashboard.setText("PORTARIA- ");
                });
    }

    private void configurarNavegacao() {
        navPendentes.setOnClickListener(v -> {
            if (condominioId == null || condominioId.trim().isEmpty()) {
                Toast.makeText(this, "Condomínio não carregado ainda", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, PendentesActivity.class);
            intent.putExtra("condominioId", condominioId);
            startActivity(intent);
        });

        navNova.setOnClickListener(v -> {
            if (condominioId == null || condominioId.trim().isEmpty()) {
                Toast.makeText(this, "Condomínio não carregado ainda", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, NovaEncomendaActivity.class);
            intent.putExtra("condominioId", condominioId);
            startActivity(intent);
        });

        navHistorico.setOnClickListener(v -> {
            if (condominioId == null || condominioId.trim().isEmpty()) {
                Toast.makeText(this, "Condomínio não carregado ainda", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, HistoricoActivity.class);
            intent.putExtra("condominioId", condominioId);
            startActivity(intent);
        });
    }

    private void configurarMenu() {
        tvMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, tvMenu);
            popupMenu.getMenu().add("Perfil");
            popupMenu.getMenu().add("Sair");

            popupMenu.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();

                if (title.equals("Perfil")) {
                    Toast.makeText(this, "Perfil em breve", Toast.LENGTH_SHORT).show();
                    return true;
                }

                if (title.equals("Sair")) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
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
        if (condominioId == null || condominioId.trim().isEmpty()) {
            Log.w(TAG, "carregarEstatisticas chamado sem condominioId");
            return;
        }

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
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao carregar pendentes", e);
                    totalPendentes = 0;
                    tvPendentesCount.setText("0");
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
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao carregar retiradas", e);
                    totalRetiradas = 0;
                    tvRetiradasCount.setText("0");
                    atualizarMensagem();
                });
    }

    private void carregarNovasHoje() {
        db.collection("encomendas")
                .whereEqualTo("condominioId", condominioId)
                .get()
                .addOnSuccessListener(query -> {
                    int countHoje = 0;

                    Calendar hojeInicio = Calendar.getInstance();
                    hojeInicio.set(Calendar.HOUR_OF_DAY, 0);
                    hojeInicio.set(Calendar.MINUTE, 0);
                    hojeInicio.set(Calendar.SECOND, 0);
                    hojeInicio.set(Calendar.MILLISECOND, 0);

                    Date inicioDoDia = hojeInicio.getTime();

                    for (QueryDocumentSnapshot doc : query) {
                        Timestamp timestamp = doc.getTimestamp("createdAt");

                        if (timestamp != null && timestamp.toDate().compareTo(inicioDoDia) >= 0) {
                            countHoje++;
                        }
                    }

                    totalNovasHoje = countHoje;
                    tvNovasCount.setText(String.valueOf(totalNovasHoje));
                    atualizarMensagem();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao carregar novas de hoje", e);
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