package com.ricardo.jesyonkoli.ui.portaria;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.ricardo.jesyonkoli.R;

import java.io.File;

public class VisualizarFotoActivity extends AppCompatActivity {

    ImageView imgFull;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_foto);

        imgFull = findViewById(R.id.imgFull);

        String path = getIntent().getStringExtra("fotoPath");

        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                imgFull.setImageBitmap(bitmap);
            }
        }
    }
}