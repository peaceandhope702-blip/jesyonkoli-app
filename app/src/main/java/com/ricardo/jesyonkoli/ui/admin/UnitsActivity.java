package com.ricardo.jesyonkoli.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ricardo.jesyonkoli.R;
import com.ricardo.jesyonkoli.data.adapter.UnitAdapter;
import com.ricardo.jesyonkoli.data.model.UnitModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnitsActivity extends AppCompatActivity {
    private String condominioId;
    private RecyclerView recyclerUnits;
    private FloatingActionButton fabAddUnit;

    private final List<UnitModel> listaUnits = new ArrayList<>();
    private UnitAdapter adapter;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_units);

        recyclerUnits = findViewById(R.id.recyclerUnits);
        fabAddUnit = findViewById(R.id.fabAddUnit);

        db = FirebaseFirestore.getInstance();

        adapter = new UnitAdapter(listaUnits);
        recyclerUnits.setLayoutManager(new LinearLayoutManager(this));
        recyclerUnits.setAdapter(adapter);

        fabAddUnit.setOnClickListener(v -> mostrarDialogAdicionarUnidade());
        condominioId = getIntent().getStringExtra("condominioId");

        if (condominioId == null || condominioId.trim().isEmpty()) {
            Toast.makeText(this, "Condomínio não informado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        carregarUnits();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarUnits();
    }

    private void carregarUnits() {
        db.collection("units")
                .whereEqualTo("condominioId", condominioId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaUnits.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String unitId = doc.getId();
                        String unidade = doc.getString("unidade");
                        String status = doc.getString("status");
                        String condominioId = doc.getString("condominioId");

                        listaUnits.add(new UnitModel(unitId, unidade, status, condominioId));
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this,
                        "Erro ao carregar unidades: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
    }

    private void mostrarDialogAdicionarUnidade() {
        final EditText input = new EditText(this);
        input.setHint("Digite a unidade (ex: 102B)");

        new AlertDialog.Builder(this)
                .setTitle("Nova unidade")
                .setMessage("Informe o identificador da unidade")
                .setView(input)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String unidadeDigitada = input.getText().toString().trim();

                    if (unidadeDigitada.isEmpty()) {
                        Toast.makeText(this, "Digite uma unidade válida", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    salvarNovaUnit(unidadeDigitada);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void salvarNovaUnit(String unidadeDigitada) {
        String unitId = unidadeDigitada;

        Map<String, Object> unit = new HashMap<>();
        unit.put("unidade", unidadeDigitada);
        unit.put("status", "ATIVA");
        unit.put("createdAt", FieldValue.serverTimestamp());

        // NOUVO
        unit.put("condominioId", condominioId);

        db.collection("units")
                .document(unitId)
                .set(unit)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Unidade adicionada com sucesso", Toast.LENGTH_SHORT).show();
                    carregarUnits();
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this,
                        "Erro ao salvar unidade: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
    }
}