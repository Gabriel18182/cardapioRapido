package com.example.cardapiorapido;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PedidoResumoAdapter extends RecyclerView.Adapter<PedidoResumoAdapter.PedidoResumoViewHolder> {

    public interface OnFecharMesaClickListener {
        void onClick(MesaPedidoResumo resumo);
    }

    private final List<MesaPedidoResumo> resumos;
    private final OnFecharMesaClickListener fecharCallback;
    private final Context context;
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public PedidoResumoAdapter(List<MesaPedidoResumo> resumos, OnFecharMesaClickListener fecharCallback, Context context) {
        this.resumos = resumos;
        this.fecharCallback = fecharCallback;
        this.context = context;
    }

    @NonNull
    @Override
    public PedidoResumoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pedido_mesa_layout, parent, false);
        return new PedidoResumoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoResumoViewHolder holder, int position) {
        MesaPedidoResumo resumo = resumos.get(position);
        holder.textMesa.setText("Mesa " + resumo.getMesa().getNome());
        holder.textItens.setText(montarTextoPedidos(resumo));
        holder.textTotal.setText("Total aberto: " + moeda.format(resumo.getTotal()));
        holder.buttonFechar.setBackground(context.getResources().getDrawable(R.drawable.button_principal));
        holder.buttonFechar.setEnabled(resumo.temPedidosAbertos());
        holder.buttonFechar.setAlpha(resumo.temPedidosAbertos() ? 1f : 0.5f);
        holder.buttonFechar.setOnClickListener(v -> fecharCallback.onClick(resumo));
    }

    private String montarTextoPedidos(MesaPedidoResumo resumo) {
        if (!resumo.temPedidosAbertos()) {
            return "Sem pedidos abertos.";
        }

        StringBuilder builder = new StringBuilder();
        for (Pedido pedido : resumo.getPedidos()) {
            int quantidade = Math.max(1, pedido.getQuantidade());
            builder.append(quantidade)
                    .append("x ")
                    .append(pedido.getItemNome() == null ? "Produto" : pedido.getItemNome())
                    .append(" - ")
                    .append(pedido.getItemValor() == null ? "" : pedido.getItemValor());

            if (pedido.getObservacao() != null && !pedido.getObservacao().trim().isEmpty()) {
                builder.append("\nObs: ").append(pedido.getObservacao().trim());
            }
            builder.append("\n\n");
        }
        return builder.toString().trim();
    }

    @Override
    public int getItemCount() {
        return resumos.size();
    }

    public static class PedidoResumoViewHolder extends RecyclerView.ViewHolder {
        TextView textMesa, textItens, textTotal;
        Button buttonFechar;

        public PedidoResumoViewHolder(@NonNull View itemView) {
            super(itemView);
            textMesa = itemView.findViewById(R.id.textMesaPedido);
            textItens = itemView.findViewById(R.id.textItensPedido);
            textTotal = itemView.findViewById(R.id.textTotalPedido);
            buttonFechar = itemView.findViewById(R.id.buttonFecharMesa);
        }
    }
}
