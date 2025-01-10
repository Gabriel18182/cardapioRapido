package com.example.cardapiorapido;

import java.util.UUID;

public class Item {
    private String id;
    private String nome;
    private String descricao;
    private String valor;
    private String usuarioId; // Campo para armazenar o ID do usuário

    // Construtores, getters e setters

    public Item() {
    }

    public Item(String nome, String descricao, String valor, String usuarioId) {
        this.id = UUID.randomUUID().toString();
        this.nome = nome;
        this.descricao = descricao;
        this.valor = valor;
        this.usuarioId = usuarioId; // Definindo o ID do usuário
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }
}
