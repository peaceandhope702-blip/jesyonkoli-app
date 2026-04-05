package com.ricardo.jesyonkoli;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.ricardo.jesyonkoli.core.util.UpdateChecker;
import com.ricardo.jesyonkoli.ui.auth.LoginActivity;
import com.ricardo.jesyonkoli.ui.auth.RoleGate;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        UpdateChecker.checkForUpdates(this, this::continueAppFlow);
    }

    private void continueAppFlow() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            RoleGate.routeUser(SplashActivity.this);
        } else {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }
    }
}