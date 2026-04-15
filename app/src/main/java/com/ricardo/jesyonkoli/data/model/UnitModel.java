package com.ricardo.jesyonkoli.data.model;

public class UnitModel {

    private String unitId;
    private String unidade;
    private String status;
    private String condominioId;

    // 🔹 Obligatwa pou Firestore
    public UnitModel() {
    }

    // 🔹 Constructor principal (ORDRE FIXE)
    public UnitModel(String unitId, String unidade, String status, String condominioId) {
        this.unitId = unitId;
        this.unidade = unidade;
        this.status = status;
        this.condominioId = condominioId;
    }

    // =========================
    // GETTERS
    // =========================

    public String getUnitId() {
        return unitId;
    }

    public String getUnidade() {
        return unidade;
    }

    public String getStatus() {
        return status;
    }

    public String getCondominioId() {
        return condominioId;
    }

    // =========================
    // SETTERS
    // =========================

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCondominioId(String condominioId) {
        this.condominioId = condominioId;
    }

    // =========================
    // BONUS (DEBUG PRO)
    // =========================

    @Override
    public String toString() {
        return "UnitModel{" +
                "unitId='" + unitId + '\'' +
                ", unidade='" + unidade + '\'' +
                ", status='" + status + '\'' +
                ", condominioId='" + condominioId + '\'' +
                '}';
    }
}