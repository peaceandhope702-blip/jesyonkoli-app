package com.ricardo.jesyonkoli.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.ricardo.jesyonkoli.R;
import com.ricardo.jesyonkoli.data.adapter.UsuarioAdapter;
import com.ricardo.jesyonkoli.data.model.UsuarioModel;

import java.util.ArrayList;
import java.util.List;

public class UsuariosActivity extends AppCompatActivity {

    private RecyclerView recyclerUsuarios;
    private EditText etPesquisarUsuario;

    private UsuarioAdapter adapter;
    private final List<UsuarioModel> listaUsuarios = new ArrayList<>();
    private final List<UsuarioModel> listaFiltrada = new ArrayList<>();

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios);

        recyclerUsuarios = findViewById(R.id.recyclerUsuarios);
        etPesquisarUsuario = findViewById(R.id.etPesquisarUsuario);

        db = FirebaseFirestore.getInstance();

        adapter = new UsuarioAdapter(
                this,
                listaFiltrada,
                usuario -> {
                    // Clique no card inteiro, se quiser usar depois
                },
                (usuario, novoStatus) -> {
                    if (usuario == null || usuario.getId() == null || usuario.getId().trim().isEmpty()) {
                        Toast.makeText(this, "Usuário inválido.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (novoStatus == null || novoStatus.trim().isEmpty()) {
                        Toast.makeText(this, "Status inválido.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if ("MORADOR".equalsIgnoreCase(usuario.getPerfil())
                            && "ATIVO".equalsIgnoreCase(novoStatus)) {

                        String unidadeId = usuario.getUnitId();

                        if (unidadeId == null || unidadeId.trim().isEmpty()) {
                            Toast.makeText(this, "Usuário sem unitId definido.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ativarMoradorExclusivo(usuario.getId(), unidadeId);
                    } else {
                        atualizarStatusUsuario(usuario.getId(), novoStatus);
                    }
                }
        );

        recyclerUsuarios.setLayoutManager(new LinearLayoutManager(this));
        recyclerUsuarios.setAdapter(adapter);

        etPesquisarUsuario.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Não usado
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarUsuarios(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Não usado
            }
        });

        carregarUsuarios();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarUsuarios();
    }

    private void ativarMoradorExclusivo(String userId, String unidadeId) {
        db.collection("users")
                .whereEqualTo("role", "MORADOR")
                .whereEqualTo("unitId", unidadeId)
                .whereEqualTo("status", "ATIVO")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    WriteBatch batch = db.batch();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        if (!doc.getId().equals(userId)) {
                            batch.update(doc.getReference(), "status", "INATIVO");
                        }
                    }

                    DocumentReference novoUserRef = db.collection("users").document(userId);
                    batch.update(novoUserRef, "status", "ATIVO");

                    batch.commit()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Morador ativado com sucesso!", Toast.LENGTH_SHORT).show();
                                carregarUsuarios();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Erro ao ativar morador: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao buscar moradores ativos: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void atualizarStatusUsuario(String userId, String novoStatus) {
        db.collection("users")
                .document(userId)
                .update("status", novoStatus)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Status atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    carregarUsuarios();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao atualizar status: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void carregarUsuarios() {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaUsuarios.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        UsuarioModel usuario = new UsuarioModel();

                        usuario.setId(doc.getId());
                        usuario.setNome(doc.getString("nome"));
                        usuario.setEmail(doc.getString("email"));
                        usuario.setUnidade(doc.getString("unidade"));
                        usuario.setUnitId(doc.getString("unitId"));
                        usuario.setStatus(doc.getString("status"));
                        usuario.setPerfil(doc.getString("role"));

                        listaUsuarios.add(usuario);
                    }

                    filtrarUsuarios(etPesquisarUsuario.getText().toString().trim());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar usuários: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void filtrarUsuarios(@NonNull String texto) {
        listaFiltrada.clear();

        if (texto.trim().isEmpty()) {
            listaFiltrada.addAll(listaUsuarios);
        } else {
            String busca = texto.toLowerCase().trim();

            for (UsuarioModel usuario : listaUsuarios) {
                String nome = usuario.getNome() != null ? usuario.getNome().toLowerCase() : "";
                String email = usuario.getEmail() != null ? usuario.getEmail().toLowerCase() : "";
                String unidade = usuario.getUnidade() != null ? usuario.getUnidade().toLowerCase() : "";
                String perfil = usuario.getPerfil() != null ? usuario.getPerfil().toLowerCase() : "";

                if (nome.contains(busca)
                        || email.contains(busca)
                        || unidade.contains(busca)
                        || perfil.contains(busca)) {
                    listaFiltrada.add(usuario);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }
}