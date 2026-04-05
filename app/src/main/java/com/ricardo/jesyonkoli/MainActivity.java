package com.ricardo.jesyonkoli;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;



public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> test = new HashMap<>();
        test.put("status", "ok");
        test.put("time", System.currentTimeMillis());

        db.collection("debug")
                .document("android_test")
                .set(test)
                .addOnSuccessListener(aVoid -> Log.d("FIREBASE_TEST", "Write OK ✅"))
                .addOnFailureListener(e -> Log.e("FIREBASE_TEST", "Write FAIL ❌ " + e.getMessage()));


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}