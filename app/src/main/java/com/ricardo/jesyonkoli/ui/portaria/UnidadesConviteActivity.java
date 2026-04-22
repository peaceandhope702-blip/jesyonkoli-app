package com.ricardo.jesyonkoli.ui.portaria;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
import com.ricardo.jesyonkoli.data.adapter.UnidadeConviteAdapter;
import com.ricardo.jesyonkoli.data.model.UnidadeConviteModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnidadesConviteActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private EditText etSearch;

    private UnidadeConviteAdapter adapter;
    private final List<UnidadeConviteModel> lista = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String condominioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unidades_convite);

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

        recycler = findViewById(R.id.recyclerUnidades);
        etSearch = findViewById(R.id.etSearch);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UnidadeConviteAdapter(unidade -> {
            gerarCodigo(unidade);
        });

        recycler.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupSearch();
        loadUser();
    }

    private void loadUser() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        condominioId = doc.getString("condominioId");

                        if (condominioId != null) {
                            loadUnits();
                        }
                    }
                });
    }

    private void loadUnits() {
        db.collection("units")
                .whereEqualTo("condominioId", condominioId)
                .get()
                .addOnSuccessListener(unitsQuery -> {

                    List<UnidadeConviteModel> tempList = new ArrayList<>();

                    for (DocumentSnapshot doc : unitsQuery.getDocuments()) {

                        String unitId = doc.getId();
                        String unidade = doc.getString("unidade");
                        String status = doc.getString("status");

                        UnidadeConviteModel model =
                                new UnidadeConviteModel(unitId, unidade, status);

                        tempList.add(model);
                    }

                    loadMoradoresAtivos(tempList);
                });
    }

    private void loadMoradoresAtivos(List<UnidadeConviteModel> unidades) {

        db.collection("users")
                .whereEqualTo("condominioId", condominioId)
                .whereEqualTo("role", "MORADOR")
                .whereEqualTo("ativo", true)
                .get()
                .addOnSuccessListener(usersQuery -> {

                    Map<String, Boolean> mapaMorador = new HashMap<>();

                    for (DocumentSnapshot doc : usersQuery.getDocuments()) {
                        String unitId = doc.getString("unitId");

                        if (unitId != null) {
                            mapaMorador.put(unitId, true);
                        }
                    }

                    for (UnidadeConviteModel u : unidades) {
                        u.setTemMoradorAtivo(
                                mapaMorador.containsKey(u.getUnitId())
                        );
                    }

                    loadCodigosAtivos(unidades);
                });
    }

    private void loadCodigosAtivos(List<UnidadeConviteModel> unidades) {

        db.collection("invitationCodes")
                .whereEqualTo("condominioId", condominioId)
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(codesQuery -> {

                    Map<String, Boolean> mapaCodigo = new HashMap<>();

                    for (DocumentSnapshot doc : codesQuery.getDocuments()) {
                        String unitId = doc.getString("unitId");

                        if (unitId != null) {
                            mapaCodigo.put(unitId, true);
                        }
                    }

                    for (UnidadeConviteModel u : unidades) {
                        u.setTemCodigoAtivo(
                                mapaCodigo.containsKey(u.getUnitId())
                        );
                    }

                    lista.clear();
                    lista.addAll(unidades);
                    adapter.setData(lista);
                });
    }

    private void gerarCodigo(UnidadeConviteModel unidade) {

        String unitId = unidade.getUnitId();

        db.collection("invitationCodes")
                .whereEqualTo("unitId", unitId)
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(query -> {

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        doc.getReference().update("active", false);
                    }

                    String code = generateCode();

                    Map<String, Object> data = new HashMap<>();
                    data.put("code", code);
                    data.put("unitId", unitId);
                    data.put("unidade", unidade.getUnidade());
                    data.put("condominioId", condominioId);
                    data.put("active", true);
                    data.put("used", false);
                    data.put("createdAt", System.currentTimeMillis());
                    data.put("createdBy", auth.getCurrentUser().getUid());
                    data.put("createdByRole", "PORTARIA");

                    db.collection("invitationCodes")
                            .document(code)
                            .set(data)
                            .addOnSuccessListener(docRef -> {

                                copyToClipboard(code);

                                showCodeDialog(code, unidade.getUnidade());
                                loadUnits();

                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                });
    }

    private String generateCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            int index = (int) (Math.random() * chars.length());
            code.append(chars.charAt(index));
        }

        return code.toString();
    }

    private void showCodeDialog(String code, String unidade) {

        new AlertDialog.Builder(this)
                .setTitle("Código gerado")
                .setMessage("Unidade: " + unidade + "\nCódigo: " + code)

                .setPositiveButton("Copiar", (dialog, which) -> {
                    copyToClipboard(code);
                })

                .setNeutralButton("Partager", (dialog, which) -> {
                    shareCode(code, unidade);
                })

                .setNegativeButton("Fechar", null)

                .show();
    }

    private void copyToClipboard(String code) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Código", code);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "Código copiado!", Toast.LENGTH_SHORT).show();
    }

    private void shareCode(String code, String unidade) {

        String message = "Olá 👋\n\n" +
                "Unidade: " + unidade + "\n" +
                "Código de acesso: " + code + "\n\n" +
                "Use este código para criar sua conta no aplicativo.";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, message);

        startActivity(Intent.createChooser(intent, "Partager via"));
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int i,int i1,int i2){}
            @Override public void afterTextChanged(Editable editable){}

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                adapter.filter(s.toString());
            }
        });
    }
}