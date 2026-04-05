package com.ricardo.jesyonkoli.core.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.pm.PackageInfoCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UpdateChecker {

    public interface UpdateCallback {
        void onContinue();
    }

    public static void checkForUpdates(Context context, UpdateCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("app_config")
                .document("version")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        handleVersionCheck(context, documentSnapshot, callback);
                    } else {
                        callback.onContinue();
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    callback.onContinue();
                });
    }

    private static void handleVersionCheck(Context context, DocumentSnapshot doc, UpdateCallback callback) {

        Long latestVersionCodeLong = doc.getLong("latestVersionCode");
        String latestVersionName = doc.getString("latestVersionName");
        String apkUrl = doc.getString("apkUrl");
        Boolean forceUpdate = doc.getBoolean("forceUpdate");

        if (latestVersionCodeLong == null || apkUrl == null || forceUpdate == null) {
            callback.onContinue();
            return;
        }

        int latestVersionCode = latestVersionCodeLong.intValue();
        int currentVersionCode;

        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);

            currentVersionCode = (int) PackageInfoCompat.getLongVersionCode(packageInfo);

        } catch (Exception e) {
            e.printStackTrace();
            callback.onContinue();
            return;
        }

        Log.d("UPDATE_CHECK", "SERVER = " + latestVersionCode);
        Log.d("UPDATE_CHECK", "APP = " + currentVersionCode);

        // 🔥 KLE FINAL: sèlman si version server PI GWO
        if (latestVersionCode > currentVersionCode) {

            if (forceUpdate) {
                showForceUpdateDialog(context, latestVersionName, apkUrl);
            } else {
                showOptionalUpdateDialog(context, latestVersionName, apkUrl, callback);
            }

        } else {
            // ✅ Pa gen update → pa montre popup
            callback.onContinue();
        }
    }

    private static void showOptionalUpdateDialog(Context context, String latestVersionName, String apkUrl, UpdateCallback callback) {
        new AlertDialog.Builder(context)
                .setTitle("Atualização disponível")
                .setMessage("Nova versão disponível: " + latestVersionName)
                .setCancelable(false)
                .setPositiveButton("Atualizar", (dialog, which) -> openApkLink(context, apkUrl))
                .setNegativeButton("Depois", (dialog, which) -> callback.onContinue())
                .show();
    }

    private static void showForceUpdateDialog(Context context, String latestVersionName, String apkUrl) {
        new AlertDialog.Builder(context)
                .setTitle("Atualização obrigatória")
                .setMessage("Você precisa atualizar o app. Versão: " + latestVersionName)
                .setCancelable(false)
                .setPositiveButton("Atualizar", (dialog, which) -> openApkLink(context, apkUrl))
                .show();
    }

    private static void openApkLink(Context context, String apkUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl));
        context.startActivity(intent);
    }
}