package com.ricardo.jesyonkoli.data.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ricardo.jesyonkoli.R;
import com.ricardo.jesyonkoli.data.model.UnidadeConviteModel;

import java.util.ArrayList;
import java.util.List;

public class UnidadeConviteAdapter extends RecyclerView.Adapter<UnidadeConviteAdapter.ViewHolder> {

    public interface OnGerarClick {
        void onClick(UnidadeConviteModel unidade);
    }

    private OnGerarClick listener;

    private final List<UnidadeConviteModel> lista = new ArrayList<>();
    private final List<UnidadeConviteModel> listaFull = new ArrayList<>();

    public UnidadeConviteAdapter(OnGerarClick listener) {
        this.listener = listener;
    }

    public void setData(List<UnidadeConviteModel> data) {
        lista.clear();
        lista.addAll(data);

        listaFull.clear();
        listaFull.addAll(data);

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_unidade_convite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        UnidadeConviteModel u = lista.get(position);

        holder.tvUnidade.setText("Unidade: " + u.getUnidade());
        holder.tvStatus.setText("Status: " + u.getStatus());
        holder.tvMorador.setText("Morador atual: " + (u.isTemMoradorAtivo() ? "SIM" : "NÃO"));
        holder.tvCodigo.setText("Código ativo: " + (u.isTemCodigoAtivo() ? "SIM" : "NÃO"));

        boolean podeGerar = "ATIVA".equals(u.getStatus());

        holder.btnGerar.setEnabled(podeGerar);

        if (u.isTemCodigoAtivo()) {
            holder.btnGerar.setText("Regerar código");
        } else {
            holder.btnGerar.setText("Gerar código");
        }

        holder.btnGerar.setOnClickListener(v -> {
            if (listener != null && podeGerar) {
                listener.onClick(u);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public void filter(String text) {
        lista.clear();

        if (text == null || text.trim().isEmpty()) {
            lista.addAll(listaFull);
        } else {
            String pesquisa = text.toLowerCase();

            for (UnidadeConviteModel u : listaFull) {

                String unidade = u.getUnidade() != null
                        ? u.getUnidade().toLowerCase()
                        : "";

                if (unidade.contains(pesquisa)) {
                    lista.add(u);
                }
            }
        }

        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvUnidade, tvStatus, tvMorador, tvCodigo;
        Button btnGerar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUnidade = itemView.findViewById(R.id.tvUnidade);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvMorador = itemView.findViewById(R.id.tvMorador);
            tvCodigo = itemView.findViewById(R.id.tvCodigo);
            btnGerar = itemView.findViewById(R.id.btnGerar);
        }
    }
}