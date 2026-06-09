package com.example.cardapiorapido;

import java.util.ArrayList;
import java.util.List;

public class MesaPedidoResumo {
    private final Mesa mesa;
    private final List<Pedido> pedidos = new ArrayList<>();

    public MesaPedidoResumo(Mesa mesa) {
        this.mesa = mesa;
    }

    public Mesa getMesa() {
        return mesa;
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public void addPedido(Pedido pedido) {
        pedidos.add(pedido);
    }

    public boolean temPedidosAbertos() {
        return !pedidos.isEmpty();
    }

    public boolean temPedidosPendentesBaixa() {
        for (Pedido pedido : pedidos) {
            if (pedido.getBaixado() == null || !pedido.getBaixado()) {
                return true;
            }
        }
        return false;
    }

    public double getTotal() {
        double total = 0.0;
        for (Pedido pedido : pedidos) {
            total += parseValor(pedido.getItemValor()) * Math.max(1, pedido.getQuantidade());
        }
        return total;
    }

    private double parseValor(String valor) {
        if (valor == null) {
            return 0.0;
        }

        try {
            String normalizado = valor.replace("R$", "").replace(".", "").replace(",", ".").trim();
            return Double.parseDouble(normalizado);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
