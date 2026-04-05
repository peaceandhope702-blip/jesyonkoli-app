package com.ricardo.jesyonkoli.data.model;

public class UnitModel {

    private String unitId;
    private String unidade;
    private String status;

    public UnitModel(String unitId, String unidade, String status) {
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
}