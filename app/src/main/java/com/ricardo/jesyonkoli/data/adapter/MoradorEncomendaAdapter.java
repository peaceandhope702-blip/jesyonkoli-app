package com.ricardo.jesyonkoli.data.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.ricardo.jesyonkoli.R;
import com.ricardo.jesyonkoli.data.model.Encomenda;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MoradorEncomendaAdapter extends RecyclerView.Adapter<MoradorEncomendaAdapter.ViewHolder> {

    public interface OnEncomendaClickListener {
        void onEncomendaClick(Encomenda encomenda);
    }

    private final List<Encomenda> lista;
    private final OnEncomendaClickListener listener;

    public MoradorEncomendaAdapter(List<Encomenda> lista, OnEncomendaClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_encomenda_morador, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Encomenda encomenda = lista.get(position);

        holder.tvUnidade.setText(safe(encomenda.getUnidade()));
        holder.tvDescricao.setText(safeDescricao(encomenda.getDescricao()));
        holder.tvStatus.setText(safeStatus(encomenda.getStatus()));
        holder.tvDataHora.setText(formatDataHoraLonga(encomenda.getCreatedAt()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEncomendaClick(encomenda);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista == null ? 0 : lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUnidade, tvDescricao, tvStatus, tvDataHora;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUnidade = itemView.findViewById(R.id.tvUnidade);
            tvDescricao = itemView.findViewById(R.id.tvDescricao);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDataHora = itemView.findViewById(R.id.tvDataHora);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeDescricao(String value) {
        return value == null || value.trim().isEmpty() ? "Sem descrição" : value.trim();
    }

    private String safeStatus(String value) {
        return value == null || value.trim().isEmpty() ? "PENDENTE" : value.trim().toUpperCase();
    }

    private String formatDataHoraLonga(Timestamp timestamp) {
        if (timestamp == null) {
            return "--/--/---- --:--";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(timestamp.toDate());
    }
}