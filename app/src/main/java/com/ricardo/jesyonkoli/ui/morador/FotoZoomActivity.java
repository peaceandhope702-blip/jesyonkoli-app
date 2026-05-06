package com.ricardo.jesyonkoli.ui.morador;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.github.chrisbanes.photoview.PhotoView;
import com.bumptech.glide.Glide;
import com.ricardo.jesyonkoli.R;


public class FotoZoomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PhotoView photoView = new PhotoView(this);
        setContentView(photoView);

        String fotoUrl = getIntent().getStringExtra("fotoUrl");

        Glide.with(this)
                .load(fotoUrl)
                .into(photoView);
    }
}