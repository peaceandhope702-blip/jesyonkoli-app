package com.ricardo.jesyonkoli.data.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;
import com.ricardo.jesyonkoli.R;
import com.ricardo.jesyonkoli.data.model.UsuarioModel;

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UserViewHolder> {

    private final Context context;
    private final List<UsuarioModel> listaUsuarios;
    private final OnUserClickListener clickListener;
    private final OnStatusChangeListener statusChangeListener;

    public UsuarioAdapter(Context context,
                          List<UsuarioModel> listaUsuarios,
                          OnUserClickListener clickListener,
                          OnStatusChangeListener statusChangeListener) {
        this.context = context;
        this.listaUsuarios = listaUsuarios;
        this.clickListener = clickListener;
        this.statusChangeListener = statusChangeListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_usuario, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UsuarioModel usuario = listaUsuarios.get(position);

        holder.tvNome.setText(safe(usuario.getNome(), "Sem nome"));
        holder.tvEmail.setText(safe(usuario.getEmail(), "Sem e-mail"));
        holder.tvPerfil.setText("Perfil: " + safe(usuario.getPerfil(), "Sem perfil"));
        holder.tvUnidade.setText("Unidade: " + safe(usuario.getUnidade(), "Sem unidade"));

        applyStatusStyle(holder.chipStatus, usuario.getStatus());

        holder.imgPerfil.setImageResource(R.drawable.ic_user_placeholder);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onUserClick(usuario);
            }
        });

        holder.chipStatus.setOnClickListener(v -> {
            if (usuario.getId() == null || usuario.getId().trim().isEmpty()) {
                Toast.makeText(context, "ID do usuário não encontrado.", Toast.LENGTH_SHORT).show();
                return;
            }

            String statusAtual = usuario.getStatus() != null
                    ? usuario.getStatus().trim().toUpperCase()
                    : "INATIVO";

            String novoStatus = statusAtual.equals("ATIVO") ? "INATIVO" : "ATIVO";
            String acao = novoStatus.equals("ATIVO") ? "ativar" : "inativar";

            new AlertDialog.Builder(context)
                    .setTitle("Confirmação")
                    .setMessage("Tem certeza que deseja " + acao + " este usuário?")
                    .setPositiveButton("Confirmar", (dialog, which) -> {
                        if (statusChangeListener != null) {
                            statusChangeListener.onStatusChange(usuario, novoStatus);
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    private void applyStatusStyle(Chip chip, String statusValue) {
        String status = statusValue != null ? statusValue.trim().toUpperCase() : "INATIVO";
        chip.setText(status);

        if ("ATIVO".equals(status)) {
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#22C55E")));
            chip.setTextColor(Color.WHITE);
        } else {
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#EF4444")));
            chip.setTextColor(Color.WHITE);
        }
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    @Override
    public int getItemCount() {
        return listaUsuarios != null ? listaUsuarios.size() : 0;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        ShapeableImageView imgPerfil;
        android.widget.TextView tvNome, tvEmail, tvUnidade, tvPerfil;
        Chip chipStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            imgPerfil = itemView.findViewById(R.id.imgPerfil);
            tvNome = itemView.findViewById(R.id.tvNome);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvUnidade = itemView.findViewById(R.id.tvUnidade);
            tvPerfil = itemView.findViewById(R.id.tvPerfil);
            chipStatus = itemView.findViewById(R.id.chipStatus);
        }
    }

    public interface OnUserClickListener {
        void onUserClick(UsuarioModel usuario);
    }

    public interface OnStatusChangeListener {
        void onStatusChange(UsuarioModel usuario, String novoStatus);
    }
}