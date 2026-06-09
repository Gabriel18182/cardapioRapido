package com.example.cardapiorapido;

import com.google.firebase.Timestamp;

public class Pedido {
    private String id;
    private String usuarioId;
    private String mesaId;
    private String mesaNome;
    private String itemId;
    private String itemNome;
    private String itemDescricao;
    private String itemValor;
    private String observacao;
    private String status;
    private Boolean baixado;
    private int quantidade;
    private Timestamp criadoEm;
    private Timestamp baixadoEm;

    public Pedido() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getMesaId() {
        return mesaId;
    }

    public void setMesaId(String mesaId) {
        this.mesaId = mesaId;
    }

    public String getMesaNome() {
        return mesaNome;
    }

    public void setMesaNome(String mesaNome) {
        this.mesaNome = mesaNome;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemNome() {
        return itemNome;
    }

    public void setItemNome(String itemNome) {
        this.itemNome = itemNome;
    }

    public String getItemDescricao() {
        return itemDescricao;
    }

    public void setItemDescricao(String itemDescricao) {
        this.itemDescricao = itemDescricao;
    }

    public String getItemValor() {
        return itemValor;
    }

    public void setItemValor(String itemValor) {
        this.itemValor = itemValor;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getBaixado() {
        return baixado;
    }

    public void setBaixado(Boolean baixado) {
        this.baixado = baixado;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public Timestamp getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Timestamp criadoEm) {
        this.criadoEm = criadoEm;
    }

    public Timestamp getBaixadoEm() {
        return baixadoEm;
    }

    public void setBaixadoEm(Timestamp baixadoEm) {
        this.baixadoEm = baixadoEm;
    }
}
