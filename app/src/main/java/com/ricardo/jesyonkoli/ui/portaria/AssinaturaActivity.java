package com.ricardo.jesyonkoli.ui.portaria;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ricardo.jesyonkoli.R;
import com.ricardo.jesyonkoli.ui.view.SignatureView;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class AssinaturaActivity extends AppCompatActivity {

    private SignatureView signatureView;
    private Button btnLimpar, btnConfirmarRetirada;

    private FirebaseFirestore db;

    private String encomendaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assinatura);

        signatureView = findViewById(R.id.signatureView);
        btnLimpar = findViewById(R.id.btnLimpar);
        btnConfirmarRetirada = findViewById(R.id.btnConfirmarRetirada);

        db = FirebaseFirestore.getInstance();

        encomendaId = getIntent().getStringExtra("encomendaId");

        if (encomendaId == null) {
            Toast.makeText(this, "Erro: encomenda não encontrada", Toast.LENGTH_LONG).show();
            finish();
        }

        btnLimpar.setOnClickListener(v -> signatureView.clearSignature());

        btnConfirmarRetirada.setOnClickListener(v -> confirmarRetirada());
    }

    private void confirmarRetirada() {

        if (!signatureView.hasSignature()) {
            Toast.makeText(this, "Peça ao morador para assinar", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap bitmap = signatureView.getSignatureBitmap();

        String assinaturaBase64 = bitmapToBase64(bitmap);

        Map<String, Object> update = new HashMap<>();
        update.put("status", "RETIRADA");
        update.put("retiradaAt", FieldValue.serverTimestamp());
        update.put("assinaturaBase64", assinaturaBase64);

        db.collection("encomendas")
                .document(encomendaId)
                .update(update)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Retirada confirmada", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private String bitmapToBase64(Bitmap bitmap) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

        byte[] bytes = baos.toByteArray();

        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
}