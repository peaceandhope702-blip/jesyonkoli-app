package com.ricardo.jesyonkoli.data.model;

import com.google.firebase.Timestamp;

public class Encomenda {

    private String id;
    private String unidade;
    private String destinatario;
    private String descricao;
    private String status;
    private Timestamp createdAt;

    public Encomenda() {
    }

    public String getId() {
        return id;
    }

    public String getUnidade() {
        return unidade;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getStatus() {
        return status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}