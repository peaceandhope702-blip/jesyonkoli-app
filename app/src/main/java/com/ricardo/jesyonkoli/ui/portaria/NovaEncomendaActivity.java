package com.ricardo.jesyonkoli.ui.portaria;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.content.Context;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.os.VibrationEffect;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ricardo.jesyonkoli.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.FileOutputStream;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class NovaEncomendaActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private Uri imageUri;
    private String fotoLocalPath;
    private ImageView imgFoto;
    private EditText etBuscarMorador;
    private EditText etDescricao;
    private RecyclerView rvMoradores;
    private MaterialButton btnSalvar;
    private ChipGroup chipGroupMorador;
    private TextView tvLoadingFoto;

    private FirebaseFirestore db;
    private String condominioId;

    private String moradorId = null;
    private String destinatarioNome = null;
    private String unitIdEncontrada = null;
    private String unidadeSelecionada = null;

    private Bitmap fotoBitmap;
    private ProgressBar progressFoto;
    private boolean salvando = false;
    private boolean selecionandoMorador = false;

    private MoradorAdapter moradorAdapter;
    private final List<MoradorItem> todosMoradores = new ArrayList<>();
    private final List<MoradorItem> moradoresFiltrados = new ArrayList<>();

    // DEBOUNCE
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // 🔥 SNACKBAR PRO (couleur + vibration + anchor)
    private void showSnack(String message, boolean isSuccess) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);


        // anchor sou bouton Salvar
        snackbar.setAnchorView(btnSalvar);

        // 🎨 COULEUR
        if (isSuccess) {
            snackbar.setBackgroundTint(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            snackbar.setBackgroundTint(getResources().getColor(android.R.color.holo_red_dark));
        }

        // 📳 VIBRATION
        try {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            if (vibrator != null) {
                if (Build.VERSION.SDK_INT >= 26) { // ✅ FIX
                    vibrator.vibrate(
                            VibrationEffect.createOneShot(
                                    isSuccess ? 80 : 200,
                                    VibrationEffect.DEFAULT_AMPLITUDE
                            )
                    );
                } else {
                    vibrator.vibrate(isSuccess ? 80 : 200);
                }
            }
        } catch (Exception ignored) {}

        snackbar.show();
    }
    private void setLoadingState(boolean isLoading) {

        if (isLoading) {
            btnSalvar.setEnabled(false);
            btnSalvar.setText("Salvando...");
            btnSalvar.setIconResource(android.R.drawable.ic_popup_sync);

        } else {
            btnSalvar.setEnabled(true);
            btnSalvar.setText("Salvar Encomenda");
            btnSalvar.setIcon(null);
        }
    }

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        try {

                            if (result.getResultCode() != RESULT_OK) {
                                progressFoto.setVisibility(View.GONE);
                                imgFoto.setEnabled(true);
                                return;
                            }

                            File file = new File(fotoLocalPath);

                            if (file.exists()) {

                                // ✅ Glide pou affichage rapid
                                Glide.with(this)
                                        .load(file)
                                        .thumbnail(1.0f)
                                        .centerCrop()
                                        .into(imgFoto);

                                // ✅ kenbe bitmap pou validation ou
                                fotoBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                                showSnack("Foto capturada em alta qualidade", true);

                            } else {
                                showSnack("Erro ao capturar foto", false);
                            }

                            progressFoto.setVisibility(View.GONE);
                            imgFoto.setEnabled(true);

                        } catch (Exception e) {
                            progressFoto.setVisibility(View.GONE);
                            imgFoto.setEnabled(true);
                            showSnack("Erro ao processar foto", false);
                        }
                    }
            );

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) abrirCameraDiretamente();
                        else showSnack("Permissão da câmera negada", false);
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_encomenda);

        db = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.toolbar);
        imgFoto = findViewById(R.id.imgFoto);
        etBuscarMorador = findViewById(R.id.etBuscarMorador);
        etDescricao = findViewById(R.id.etDescricao);
        rvMoradores = findViewById(R.id.rvMoradores);
        progressFoto = findViewById(R.id.progressFoto);
        btnSalvar = findViewById(R.id.btnSalvar);
        chipGroupMorador = findViewById(R.id.chipGroupMorador);

        toolbar.setNavigationOnClickListener(v -> finish());

        condominioId = getIntent().getStringExtra("condominioId");

        if (condominioId == null || condominioId.trim().isEmpty()) {
            showSnack("Condomínio não informado", false);
            finish();
            return;
        }

        configurarRecyclerView();
        configurarBuscaAutomatica();

        imgFoto.setOnClickListener(v -> {

            // BLOKE klik
            imgFoto.setEnabled(false);

            // montre loading
            progressFoto.setVisibility(View.VISIBLE);
            tvLoadingFoto = findViewById(R.id.tvLoadingFoto);

            verificarPermissaoEAbrirCamera();
        });

        btnSalvar.setOnClickListener(v -> salvarEncomenda());

        carregarMoradores();
    }

    private void configurarRecyclerView() {
        moradorAdapter = new MoradorAdapter(moradoresFiltrados, morador -> {
            selecionandoMorador = true;

            moradorId = morador.id;
            destinatarioNome = morador.nome;
            unitIdEncontrada = morador.unitId;
            unidadeSelecionada = morador.unidade;

            etBuscarMorador.setText("");
            rvMoradores.setVisibility(View.GONE);

            selecionandoMorador = false;
            mostrarChipSelecionado(destinatarioNome);
        });

        rvMoradores.setLayoutManager(new LinearLayoutManager(this));
        rvMoradores.setAdapter(moradorAdapter);
    }

    private void configurarBuscaAutomatica() {
        etBuscarMorador.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> {

                    if (selecionandoMorador || salvando) return;

                    // 🔥 FIX: SI MORADOR DEJA CHWAZI → PA TOUCHE
                    if (moradorId != null) return;

                    moradorId = null;
                    destinatarioNome = null;
                    unitIdEncontrada = null;
                    unidadeSelecionada = null;

                    filtrarMoradores(s.toString());
                };

                handler.postDelayed(searchRunnable, 300);
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void mostrarChipSelecionado(String nome) {
        chipGroupMorador.removeAllViews();

        Chip chip = new Chip(this);
        chip.setText(nome);
        chip.setCloseIconVisible(true);

        chip.setOnCloseIconClickListener(v -> {
            chipGroupMorador.removeAllViews();

            moradorId = null;
            destinatarioNome = null;
            unitIdEncontrada = null;
            unidadeSelecionada = null;

            etBuscarMorador.setText("");
        });

        chipGroupMorador.addView(chip);
    }

    private void filtrarMoradores(String texto) {
        moradoresFiltrados.clear();

        String busca = texto.trim().toLowerCase(Locale.ROOT);

        if (busca.isEmpty()) {
            rvMoradores.setVisibility(View.GONE);
            moradorAdapter.notifyDataSetChanged();
            return;
        }

        for (MoradorItem m : todosMoradores) {
            String nome = m.nome.toLowerCase(Locale.ROOT);
            String unidade = m.unidade.toLowerCase(Locale.ROOT);

            if (nome.contains(busca) || unidade.contains(busca)) {
                moradoresFiltrados.add(m);
            }
        }

        moradorAdapter.notifyDataSetChanged();
        rvMoradores.setVisibility(moradoresFiltrados.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void carregarMoradores() {
        db.collection("users")
                .whereEqualTo("role", "MORADOR")
                .whereEqualTo("status", "ATIVO")
                .whereEqualTo("condominioId", condominioId)
                .get()
                .addOnSuccessListener(snap -> {
                    todosMoradores.clear();

                    snap.forEach(doc -> {
                        todosMoradores.add(new MoradorItem(
                                doc.getId(),
                                doc.getString("nome"),
                                doc.getString("unidade"),
                                doc.getString("unitId"),
                                doc.getString("email")
                        ));
                    });
                });
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

        try {
            File dir = new File(getFilesDir(), "fotos");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, "foto_" + System.currentTimeMillis() + ".jpg");

            fotoLocalPath = file.getAbsolutePath();

            imageUri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    file
            );

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

            cameraLauncher.launch(intent);

        } catch (Exception e) {
            showSnack("Erro ao abrir câmera", false);
        }
    }

    private void salvarEncomenda() {
        if (salvando) return;

        String descricao = etDescricao.getText().toString().trim();

        if (moradorId == null || descricao.isEmpty() || fotoBitmap == null) {
            showSnack("Preencha todos os campos", false);
            return;
        }

        salvando = true;
        setLoadingState(true); // 🔥 UX loading

        // 🔥 1. CRIAR ID ANTES
        String encomendaId = db.collection("encomendas").document().getId();

        // 🔥 2. REFERENCIA STORAGE
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("encomendas/" + condominioId + "/" + encomendaId + ".jpg");

        // 🔥 VALIDATION PATH
        if (fotoLocalPath == null || fotoLocalPath.isEmpty()) {
            showSnack("Foto inválida", false);
            salvando = false;
            setLoadingState(false);
            return;
        }

        // 🔥 VALIDATION URI
        if (imageUri == null) {
            showSnack("Imagem não encontrada", false);
            salvando = false;
            setLoadingState(false);
            return;
        }

        // 🔥 KONPRESYON
        File originalFile = new File(fotoLocalPath);

        if (!originalFile.exists()) {
            showSnack("Arquivo não encontrado", false);
            salvando = false;
            setLoadingState(false);
            return;
        }

        File compressedFile = compressImage(originalFile);

        // 🔥 VERIFYE KONPRESYON
        if (compressedFile == null || !compressedFile.exists()) {
            showSnack("Erro ao comprimir imagem", false);
            salvando = false;
            setLoadingState(false);
            return;
        }

        // 🔥 UPLOAD
        UploadTask uploadTask = storageRef.putFile(Uri.fromFile(compressedFile));

        uploadTask
                .addOnSuccessListener(taskSnapshot ->
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(downloadUrl -> {

                            // 🔥 4. FIRESTORE DATA
                            Map<String, Object> encomenda = new HashMap<>();
                            encomenda.put("unidade", unidadeSelecionada);
                            encomenda.put("unitId", unitIdEncontrada);
                            encomenda.put("moradorId", moradorId);
                            encomenda.put("destinatario", destinatarioNome);
                            encomenda.put("descricao", descricao);
                            encomenda.put("status", "PENDENTE");
                            encomenda.put("createdAt", FieldValue.serverTimestamp());
                            encomenda.put("createdBy", FirebaseAuth.getInstance().getUid());
                            encomenda.put("condominioId", condominioId);

                            // ✅ URL FOTO CLOUD
                            encomenda.put("fotoUrl", downloadUrl.toString());

                            // (opsyonèl)
                            encomenda.put("fotoLocalPath", fotoLocalPath);

                            db.collection("encomendas")
                                    .document(encomendaId)
                                    .set(encomenda)
                                    .addOnSuccessListener(r -> {

                                        showSnack("Encomenda salva com sucesso", true);
                                        limparCampos();

                                        // 🔥 RESET
                                        moradorId = null;
                                        destinatarioNome = null;
                                        unitIdEncontrada = null;
                                        unidadeSelecionada = null;
                                        fotoBitmap = null;
                                        fotoLocalPath = null;
                                        imageUri = null;

                                        salvando = false;
                                        setLoadingState(false);
                                    })
                                    .addOnFailureListener(e -> {
                                        showSnack("Erro ao salvar dados", false);
                                        salvando = false;
                                        setLoadingState(false);
                                    });

                        })
                )
                .addOnFailureListener(e -> {
                    showSnack("Erro: " + e.getMessage(), false);
                    e.printStackTrace();
                    salvando = false;
                    setLoadingState(false);
                });
    }

    private void limparCampos() {
        etBuscarMorador.setText("");
        etDescricao.setText("");
        chipGroupMorador.removeAllViews();
        imgFoto.setImageResource(R.drawable.ic_camera_placeholder);

        moradoresFiltrados.clear();
        moradorAdapter.notifyDataSetChanged();
        rvMoradores.setVisibility(View.GONE);
    }

    private File compressImage(File originalFile) {
        try {

            // 🔥 1. OPTIONS pou evite OOM
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeFile(originalFile.getAbsolutePath(), options);

            // 🔥 2. REDUI SIZE (ex: max 1280px)
            int maxSize = 1280;
            int scale = 1;

            while (options.outWidth / scale > maxSize || options.outHeight / scale > maxSize) {
                scale *= 2;
            }

            options.inSampleSize = scale;
            options.inJustDecodeBounds = false;

            Bitmap bitmap = BitmapFactory.decodeFile(originalFile.getAbsolutePath(), options);

            if (bitmap == null) {
                return originalFile;
            }

            // 🔥 3. CREATE FILE
            File compressedFile = new File(getCacheDir(),
                    "compressed_" + System.currentTimeMillis() + ".jpg");

            FileOutputStream out = new FileOutputStream(compressedFile);

            // 🔥 4. COMPRESS (quality + size)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);

            out.flush();
            out.close();

            // 🔥 5. LIBERE MEMOIRE
            bitmap.recycle();

            return compressedFile;

        } catch (Exception e) {
            e.printStackTrace();
            return originalFile;
        }
    }

    private static class MoradorItem {
        String id, nome, unidade, unitId, email;

        MoradorItem(String id, String nome, String unidade, String unitId, String email) {
            this.id = id;
            this.nome = nome;
            this.unidade = unidade;
            this.unitId = unitId;
            this.email = email;
        }
    }

    private static class MoradorAdapter extends RecyclerView.Adapter<MoradorAdapter.ViewHolder> {

        interface OnMoradorClickListener {
            void onMoradorClick(MoradorItem morador);
        }

        private final List<MoradorItem> lista;
        private final OnMoradorClickListener listener;

        MoradorAdapter(List<MoradorItem> lista, OnMoradorClickListener listener) {
            this.lista = lista;
            this.listener = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            tv.setPadding(30, 25, 30, 25);
            tv.setTextSize(16);
            return new ViewHolder(tv);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            MoradorItem m = lista.get(position);
            holder.tv.setText(m.nome + " • " + m.unidade);
            holder.tv.setOnClickListener(v -> listener.onMoradorClick(m));
        }

        @Override
        public int getItemCount() {
            return lista.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv;
            ViewHolder(View itemView) {
                super(itemView);
                tv = (TextView) itemView;
            }
        }
    }
}