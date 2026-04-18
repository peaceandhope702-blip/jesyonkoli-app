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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AssinaturaActivity extends AppCompatActivity {

    private SignatureView signatureView;
    private Button btnLimpar, btnConfirmarRetirada;

    private FirebaseFirestore db;

    private String encomendaId;
    private ArrayList<String> listaIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assinatura);

        signatureView = findViewById(R.id.signatureView);
        btnLimpar = findViewById(R.id.btnLimpar);
        btnConfirmarRetirada = findViewById(R.id.btnConfirmarRetirada);

        db = FirebaseFirestore.getInstance();

        encomendaId = getIntent().getStringExtra("encomendaId");
        listaIds = getIntent().getStringArrayListExtra("listaEncomendasIds");

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

        btnConfirmarRetirada.setEnabled(false);

        // 🔥 GROUP MODE
        if (listaIds != null && !listaIds.isEmpty()) {

            for (String id : listaIds) {
                atualizarEncomenda(id, assinaturaBase64);
            }

            Toast.makeText(this, "Retirada múltipla confirmada", Toast.LENGTH_LONG).show();
            finish();

        } else if (encomendaId != null) {

            atualizarEncomenda(encomendaId, assinaturaBase64);

            Toast.makeText(this, "Retirada confirmada", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void atualizarEncomenda(String id, String assinaturaBase64) {

        Map<String, Object> update = new HashMap<>();
        update.put("status", "RETIRADA");
        update.put("retiradaAt", FieldValue.serverTimestamp());
        update.put("assinaturaBase64", assinaturaBase64);

        db.collection("encomendas")
                .document(id)
                .update(update);
    }

    private String bitmapToBase64(Bitmap bitmap) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
}