package com.ricardo.jesyonkoli.data.adapter;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ricardo.jesyonkoli.R;
import com.ricardo.jesyonkoli.data.model.UnitModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class UnitAdapter extends RecyclerView.Adapter<UnitAdapter.ViewHolder> {

    private final List<UnitModel> lista;

    public UnitAdapter(List<UnitModel> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_unit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UnitModel unit = lista.get(position);

        holder.tvUnitId.setText("ID: " + safe(unit.getUnitId(), "--"));
        holder.tvUnidade.setText("Unidade: " + safe(unit.getUnidade(), "--"));

        String status = unit.getStatus();
        if (status == null || status.trim().isEmpty()) {
            status = "INDEFINIDO";
        }

        holder.tvStatus.setText("Status: " + status.toUpperCase());

        if ("ATIVA".equalsIgnoreCase(status)) {
            holder.btnToggleUnitStatus.setText("Inativar unidade");
        } else {
            holder.btnToggleUnitStatus.setText("Ativar unidade");
        }

        String finalStatus = status;

        holder.btnToggleUnitStatus.setOnClickListener(v -> {
            if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) return;

            String novoStatus = finalStatus.equalsIgnoreCase("ATIVA")
                    ? "INATIVA"
                    : "ATIVA";

            String acao = novoStatus.equalsIgnoreCase("ATIVA") ? "ativar" : "inativar";

            new AlertDialog.Builder(v.getContext())
                    .setTitle("Confirmação")
                    .setMessage("Tem certeza que deseja " + acao + " esta unidade?")
                    .setPositiveButton("Confirmar", (dialog, which) -> {
                        FirebaseFirestore.getInstance()
                                .collection("units")
                                .document(unit.getUnitId())
                                .update("status", novoStatus)
                                .addOnSuccessListener(unused -> {
                                    UnitModel atualizado = new UnitModel(
                                            unit.getUnitId(),
                                            unit.getUnidade(),
                                            novoStatus
                                    );

                                    int pos = holder.getAdapterPosition();
                                    if (pos != RecyclerView.NO_POSITION) {
                                        lista.set(pos, atualizado);
                                        notifyItemChanged(pos);
                                    }

                                    Toast.makeText(v.getContext(),
                                            "Unidade atualizada com sucesso",
                                            Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(
                                        v.getContext(),
                                        "Erro ao atualizar unidade: " + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show());
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        holder.btnGerarConvite.setOnClickListener(v -> {
            if (!"ATIVA".equalsIgnoreCase(unit.getStatus())) {
                Toast.makeText(v.getContext(),
                        "A unidade precisa estar ATIVA para gerar convite",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(v.getContext())
                    .setTitle("Gerar convite")
                    .setMessage("Deseja gerar um novo código para a unidade " + unit.getUnidade() + "?")
                    .setPositiveButton("Gerar", (dialog, which) -> gerarConvite(unit, holder))
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    private void gerarConvite(UnitModel unit, ViewHolder holder) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String codigo = gerarCodigoAleatorio();
        String adminUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        Map<String, Object> convite = new HashMap<>();
        convite.put("code", codigo);
        convite.put("unitId", unit.getUnitId());
        convite.put("active", true);
        convite.put("uses", 0);
        convite.put("maxUses", 1);
        convite.put("createdAt", FieldValue.serverTimestamp());
        convite.put("createdBy", adminUid);

        db.collection("invitationCodes")
                .add(convite)
                .addOnSuccessListener(documentReference -> {
                    new AlertDialog.Builder(holder.itemView.getContext())
                            .setTitle("Convite gerado")
                            .setMessage("Unidade: " + unit.getUnidade() + "\n\nCódigo: " + codigo)
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .show();
                })
                .addOnFailureListener(e -> Toast.makeText(
                        holder.itemView.getContext(),
                        "Erro ao gerar convite: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
    }

    private String gerarCodigoAleatorio() {
        String caracteres = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(caracteres.length());
            sb.append(caracteres.charAt(index));
        }

        return sb.toString();
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUnitId, tvUnidade, tvStatus;
        Button btnToggleUnitStatus, btnGerarConvite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUnitId = itemView.findViewById(R.id.tvUnitId);
            tvUnidade = itemView.findViewById(R.id.tvUnidade);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnToggleUnitStatus = itemView.findViewById(R.id.btnToggleUnitStatus);
            btnGerarConvite = itemView.findViewById(R.id.btnGerarConvite);
        }
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}