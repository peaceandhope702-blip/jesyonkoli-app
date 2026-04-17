package com.ricardo.jesyonkoli.data.model;

public class UnidadeConviteModel {

    private String unitId;
    private String unidade;
    private String status; // ATIVA / INATIVA

    private boolean temMoradorAtivo;
    private boolean temCodigoAtivo;

    public UnidadeConviteModel() {}

    public UnidadeConviteModel(String unitId, String unidade, String status) {
        this.unitId = unitId;
        this.unidade = unidade;
        this.status = status;
    }

    public String getUnitId() {
        return unitId;
    }

    public String getUnidade() {
        return unidade;
    }

    public String getStatus() {
        return status;
    }

    public boolean isTemMoradorAtivo() {
        return temMoradorAtivo;
    }

    public void setTemMoradorAtivo(boolean temMoradorAtivo) {
        this.temMoradorAtivo = temMoradorAtivo;
    }

    public boolean isTemCodigoAtivo() {
        return temCodigoAtivo;
    }

    public void setTemCodigoAtivo(boolean temCodigoAtivo) {
        this.temCodigoAtivo = temCodigoAtivo;
    }
}