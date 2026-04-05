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

import com.google.firebase.auth.FirebaseAuth;
import com.ricardo.jesyonkoli.R;
import com.ricardo.jesyonkoli.ui.auth.LoginActivity;

public class PortariaDashboardActivity extends AppCompatActivity {

    private TextView navPendentes, navNova, navHistorico, tvMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portaria_dashboard);

        navPendentes = findViewById(R.id.navPendentes);
        navNova = findViewById(R.id.navNova);
        navHistorico = findViewById(R.id.navHistorico);
        tvMenu = findViewById(R.id.tvMenu);

        // Ajoute espace en haut/bas selon téléphone
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, systemBars.bottom);
            return insets;
        });

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

        tvMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(PortariaDashboardActivity.this, tvMenu);
            popupMenu.getMenu().add("Profil");
            popupMenu.getMenu().add("Déconnexion");

            popupMenu.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();

                if (title.equals("Profil")) {
                    Toast.makeText(PortariaDashboardActivity.this,
                            "Profil ap vini pita",
                            Toast.LENGTH_SHORT).show();
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
}