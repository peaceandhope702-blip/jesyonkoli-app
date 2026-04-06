package com.ricardo.jesyonkoli.ui.portaria;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ricardo.jesyonkoli.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NovaEncomendaActivity extends AppCompatActivity {

    private EditText etUnidade;
    private EditText etDescricao;
    private TextView tvDestinatario;
    private Button btnBuscarMorador, btnFoto, btnSalvar;
    private ImageView imgPreview;
    private String condominioId;

    private FirebaseFirestore db;

    private String moradorId = null;
    private String destinatarioNome = null;

    private Bitmap fotoBitmap;

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Bundle extras = result.getData().getExtras();

                            if (extras != null) {
                                fotoBitmap = (Bitmap) extras.get("data");

                                if (fotoBitmap != null) {
                                    imgPreview.setImageBitmap(fotoBitmap);
                                    Toast.makeText(this, "Foto capturada com sucesso", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Erro ao obter imagem da câmera", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "Nenhum dado retornado pela câmera", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Captura de foto cancelada", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            abrirCameraDiretamente();
                        } else {
                            Toast.makeText(this,
                                    "Permissão da câmera negada. Autorize para tirar foto da encomenda.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_encomenda);

        db = FirebaseFirestore.getInstance();

        etUnidade = findViewById(R.id.etUnidade);
        etDescricao = findViewById(R.id.etDescricao);
        tvDestinatario = findViewById(R.id.tvDestinatario);
        imgPreview = findViewById(R.id.imgPreview);

        btnBuscarMorador = findViewById(R.id.btnBuscarMorador);
        btnFoto = findViewById(R.id.btnFoto);
        btnSalvar = findViewById(R.id.btnSalvar);

        btnBuscarMorador.setOnClickListener(v -> buscarMoradorPorUnidade());
        btnFoto.setOnClickListener(v -> verificarPermissaoEAbrirCamera());
        btnSalvar.setOnClickListener(v -> salvarEncomenda());
        condominioId = getIntent().getStringExtra("condominioId");

        if (condominioId == null || condominioId.trim().isEmpty()) {
            Toast.makeText(this, "Condomínio não informado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    private void verificarPermissaoEAbrirCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            abrirCameraDiretamente();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void abrirCameraDiretamente() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(intent);
        } else {
            Toast.makeText(this, "Nenhum aplicativo de câmera encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    private void buscarMoradorPorUnidade() {
        String unidade = etUnidade.getText().toString().trim();

        if (unidade.isEmpty()) {
            Toast.makeText(this, "Digite a unidade", Toast.LENGTH_SHORT).show();
            return;
        }

        moradorId = null;
        destinatarioNome = null;
        tvDestinatario.setText("Buscando morador...");

        db.collection("users")
                .whereEqualTo("role", "MORADOR")
                .whereEqualTo("status", "ATIVO")
                .whereEqualTo("unidade", unidade)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        tvDestinatario.setText("Morador não encontrado");
                        Toast.makeText(this, "Nenhum morador ativo encontrado para esta unidade", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (queryDocumentSnapshots.size() > 1) {
                        tvDestinatario.setText("Mais de um morador encontrado");
                        Toast.makeText(this, "Erro de dados: mais de um morador ativo nesta unidade", Toast.LENGTH_LONG).show();
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        moradorId = document.getId();

                        String nome = document.getString("nome");
                        if (nome == null || nome.trim().isEmpty()) {
                            nome = "Sem nome cadastrado";
                        }

                        destinatarioNome = nome;
                        tvDestinatario.setText(destinatarioNome);
                        Toast.makeText(this, "Morador encontrado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    tvDestinatario.setText("Erro ao buscar morador");
                    Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void salvarEncomenda() {
        String unidade = etUnidade.getText().toString().trim();
        String descricao = etDescricao.getText().toString().trim();



        if (unidade.isEmpty()) {
            Toast.makeText(this, "Digite a unidade", Toast.LENGTH_SHORT).show();
            return;
        }

        if (descricao.isEmpty()) {
            Toast.makeText(this, "Digite a descrição", Toast.LENGTH_SHORT).show();
            return;
        }

        if (moradorId == null || destinatarioNome == null) {
            Toast.makeText(this, "Busque o morador antes de salvar", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fotoBitmap == null) {
            Toast.makeText(this, "Tire uma foto da encomenda", Toast.LENGTH_SHORT).show();
            return;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String portariaUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> encomenda = new HashMap<>();
        encomenda.put("unidade", unidade);
        encomenda.put("moradorId", moradorId);
        encomenda.put("destinatario", destinatarioNome);
        encomenda.put("descricao", descricao);
        encomenda.put("status", "PENDENTE");
        encomenda.put("createdAt", FieldValue.serverTimestamp());
        encomenda.put("createdBy", portariaUid);
        encomenda.put("condominioId", condominioId);

        registrarEncomendaLocal(encomenda);
    }

    private void registrarEncomendaLocal(Map<String, Object> encomenda) {
        if (fotoBitmap == null) {
            Toast.makeText(this, "Tire uma foto da encomenda", Toast.LENGTH_SHORT).show();
            return;
        }

        String fotoLocalPath = salvarFotoLocalmente(fotoBitmap);

        if (fotoLocalPath == null) {
            Toast.makeText(this, "Erro ao salvar foto no telefone", Toast.LENGTH_LONG).show();
            return;
        }

        encomenda.put("fotoLocalPath", fotoLocalPath);

        db.collection("encomendas")
                .add(encomenda)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Encomenda registrada com sucesso", Toast.LENGTH_LONG).show();
                    limparCampos();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao salvar encomenda: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private String salvarFotoLocalmente(Bitmap bitmap) {
        File diretorio = new File(getFilesDir(), "encomendas");

        if (!diretorio.exists()) {
            boolean created = diretorio.mkdirs();
            if (!created) {
                return null;
            }
        }

        String nomeArquivo = "encomenda_" + System.currentTimeMillis() + ".jpg";
        File arquivo = new File(diretorio, nomeArquivo);

        try (FileOutputStream fos = new FileOutputStream(arquivo)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);
            fos.flush();
            return arquivo.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void limparCampos() {
        etUnidade.setText("");
        etDescricao.setText("");
        tvDestinatario.setText("Nenhum morador carregado");
        imgPreview.setImageDrawable(null);

        moradorId = null;
        destinatarioNome = null;
        fotoBitmap = null;
    }
}