package com.example.cardapiorapido;

import java.util.UUID;

public class Mesa {
    private String id;
    private String nome;
    private String usuarioId;

    public Mesa() {
    }

    public Mesa(String nome, String usuarioId) {
        this.id = UUID.randomUUID().toString();
        this.nome = nome;
        this.usuarioId = usuarioId;
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

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }
}
