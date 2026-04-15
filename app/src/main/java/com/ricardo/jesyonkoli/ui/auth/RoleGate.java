package com.ricardo.jesyonkoli.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ricardo.jesyonkoli.ui.admin.AdminPanelActivity;
import com.ricardo.jesyonkoli.ui.morador.MoradorHomeActivity;
import com.ricardo.jesyonkoli.ui.portaria.PortariaDashboardActivity;

public class RoleGate {

    public static void routeUser(Activity activity) {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(activity, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        Toast.makeText(activity, "Dados do usuário não encontrados", Toast.LENGTH_SHORT).show();
                        logout(activity);
                        return;
                    }

                    String role = document.getString("role");
                    String status = document.getString("status");
                    String condominioId = document.getString("condominioId");

                    // ✅ FIX 1: fallback si status pa egziste
                    if (status == null) {
                        status = "ATIVO";
                    }

                    // 🔒 Vérification status
                    if (!status.equalsIgnoreCase("ATIVO")) {
                        new AlertDialog.Builder(activity)
                                .setTitle("Conta inativa")
                                .setMessage("Sua conta está inativa. Entre em contato com a administração do condomínio.")
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialog, which) -> logout(activity))
                                .show();
                        return;
                    }

                    // 🔒 Vérification condominio
                    if (condominioId == null || condominioId.trim().isEmpty()) {
                        Toast.makeText(activity, "Condomínio não definido", Toast.LENGTH_SHORT).show();
                        logout(activity);
                        return;
                    }

                    // 🔁 ROUTING
                    if ("PORTARIA".equalsIgnoreCase(role)) {

                        Intent intent = new Intent(activity, PortariaDashboardActivity.class);
                        intent.putExtra("condominioId", condominioId);
                        activity.startActivity(intent);
                        activity.finish();

                    } else if ("MORADOR".equalsIgnoreCase(role)) {

                        Intent intent = new Intent(activity, MoradorHomeActivity.class);
                        intent.putExtra("condominioId", condominioId);
                        activity.startActivity(intent);
                        activity.finish();

                    } else if ("ADMIN".equalsIgnoreCase(role)) {

                        Intent intent = new Intent(activity, AdminPanelActivity.class);
                        intent.putExtra("condominioId", condominioId);
                        activity.startActivity(intent);
                        activity.finish();

                    } else {

                        Toast.makeText(activity, "Role inválido", Toast.LENGTH_SHORT).show();
                        logout(activity);
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(activity,
                            "Erro: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    logout(activity);
                });
    }

    private static void logout(Activity activity) {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}