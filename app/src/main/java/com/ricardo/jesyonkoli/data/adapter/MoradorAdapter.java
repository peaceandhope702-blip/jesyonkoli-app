package com.ricardo.jesyonkoli.data.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ricardo.jesyonkoli.R;
import com.ricardo.jesyonkoli.data.model.UsuarioModel;

import java.util.ArrayList;
import java.util.List;

public class MoradorAdapter extends RecyclerView.Adapter<MoradorAdapter.ViewHolder> {

    private final List<UsuarioModel> lista;
    private final List<UsuarioModel> listaFull;

    public MoradorAdapter() {
        this.lista = new ArrayList<>();
        this.listaFull = new ArrayList<>();
    }

    public void setData(List<UsuarioModel> novosDados) {
        lista.clear();
        lista.addAll(novosDados);

        listaFull.clear();
        listaFull.addAll(novosDados);

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_morador, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UsuarioModel user = lista.get(position);

        String nome = user.getNome() != null ? user.getNome() : "";
        String unidade = user.getUnidade() != null ? user.getUnidade() : "";
        String status = user.getStatus() != null ? user.getStatus() : "INATIVO";

        holder.tvNome.setText(nome);
        holder.tvUnidade.setText("Unidade: " + unidade);
        holder.tvStatus.setText("Status: " + status);
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
            String pesquisa = text.trim().toLowerCase();

            for (UsuarioModel user : listaFull) {
                String nome = user.getNome() != null ? user.getNome().toLowerCase() : "";
                String unidade = user.getUnidade() != null ? user.getUnidade().toLowerCase() : "";

                if (nome.contains(pesquisa) || unidade.contains(pesquisa)) {
                    lista.add(user);
                }
            }
        }

        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvNome, tvUnidade, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNome);
            tvUnidade = itemView.findViewById(R.id.tvUnidade);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}