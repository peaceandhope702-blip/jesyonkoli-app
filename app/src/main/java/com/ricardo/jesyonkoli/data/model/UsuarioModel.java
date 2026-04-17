package com.ricardo.jesyonkoli.data.model;

public class UsuarioModel {

    private String id;
    private String nome;
    private String email;
    private String unidade;
    private String unitId;
    private String perfil;
    private String status;

    // NOUVO
    private String condominioId;

    public UsuarioModel() {
    }

    public UsuarioModel(String id, String nome, String email, String unidade, String unitId, String perfil, String status, String condominioId) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.unidade = unidade;
        this.unitId = unitId;
        this.perfil = perfil;
        this.status = status;
        this.condominioId = condominioId;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getUnidade() {
        return unidade;
    }

    public String getUnitId() {
        return unitId;
    }

    public String getPerfil() {
        return perfil;
    }

    public String getStatus() {
        return status;
    }

    public String getCondominioId() {
        return condominioId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCondominioId(String condominioId) {
        this.condominioId = condominioId;
    }


}