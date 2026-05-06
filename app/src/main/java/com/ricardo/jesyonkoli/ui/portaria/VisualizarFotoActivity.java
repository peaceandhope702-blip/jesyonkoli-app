package com.ricardo.jesyonkoli.ui.portaria;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.ricardo.jesyonkoli.R;

public class VisualizarFotoActivity extends AppCompatActivity {

    private PhotoView photoView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_foto);

        photoView = findViewById(R.id.photoView);

        // ☁️ URL FIREBASE
        String fotoUrl = getIntent().getStringExtra("fotoUrl");

        if (fotoUrl != null && !fotoUrl.isEmpty()) {

            Glide.with(this)
                    .load(fotoUrl)
                    .error(R.drawable.test_image)
                    .into(photoView);

        }

        // 👆 klik pou fèmen
        photoView.setOnClickListener(v -> finish());
    }
}