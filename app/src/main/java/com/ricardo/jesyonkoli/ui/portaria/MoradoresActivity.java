package com.ricardo.jesyonkoli.ui.portaria;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ricardo.jesyonkoli.R;
import com.ricardo.jesyonkoli.data.adapter.MoradorAdapter;
import com.ricardo.jesyonkoli.data.model.UsuarioModel;

import java.util.ArrayList;
import java.util.List;

public class MoradoresActivity extends AppCompatActivity {

    private RecyclerView recyclerMoradores;
    private EditText etSearch;

    private MoradorAdapter adapter;
    private final List<UsuarioModel> lista = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String condominioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moradores);

        recyclerMoradores = findViewById(R.id.recyclerMoradores);
        etSearch = findViewById(R.id.etSearch);

        View root = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    systemBars.bottom
            );

            return insets;
        });

        recyclerMoradores.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MoradorAdapter();
        recyclerMoradores.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupSearch();
        loadUserData();
    }

    private void loadUserData() {
        if (auth.getCurrentUser() == null) {
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        condominioId = doc.getString("condominioId");

                        if (condominioId != null && !condominioId.isEmpty()) {
                            loadMoradores();
                        }
                    }
                });
    }

    private void loadMoradores() {
        db.collection("users")
                .whereEqualTo("condominioId", condominioId)
                .whereEqualTo("role", "MORADOR")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    lista.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        UsuarioModel usuario = doc.toObject(UsuarioModel.class);
                        if (usuario != null) {
                            lista.add(usuario);
                        }
                    }

                    adapter.setData(lista);
                });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // pa bezwen anyen la
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // pa bezwen anyen la
            }
        });
    }
}