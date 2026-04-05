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

public class EncomendaAdapter extends RecyclerView.Adapter<EncomendaAdapter.EncomendaViewHolder> {

    public interface OnEncomendaClickListener {
        void onEncomendaClick(Encomenda encomenda);
    }

    private final List<Encomenda> encomendaList;
    private final OnEncomendaClickListener listener;

    public EncomendaAdapter(List<Encomenda> encomendaList, OnEncomendaClickListener listener) {
        this.encomendaList = encomendaList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EncomendaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_encomenda_portaria, parent, false);
        return new EncomendaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EncomendaViewHolder holder, int position) {
        Encomenda encomenda = encomendaList.get(position);

        holder.tvUnidade.setText(safe(encomenda.getUnidade()));
        holder.tvStatus.setText(safeStatus(encomenda.getStatus()));
        holder.tvDataHora.setText(formatDataHora(encomenda.getCreatedAt()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEncomendaClick(encomenda);
            }
        });
    }

    @Override
    public int getItemCount() {
        return encomendaList != null ? encomendaList.size() : 0;
    }

    public static class EncomendaViewHolder extends RecyclerView.ViewHolder {

        TextView tvUnidade, tvStatus, tvDataHora, btnDetalhes;

        public EncomendaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUnidade = itemView.findViewById(R.id.tvUnidade);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDataHora = itemView.findViewById(R.id.tvDataHora);

        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeStatus(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "PENDENTE";
        }
        return value.trim().toUpperCase();
    }

    private String formatDataHora(Timestamp timestamp) {
        if (timestamp == null) {
            return "--/--/---- --:--";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(timestamp.toDate());
    }
}