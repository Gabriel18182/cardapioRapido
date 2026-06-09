package com.example.cardapiorapido;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PedidoResumoAdapter extends RecyclerView.Adapter<PedidoResumoAdapter.PedidoResumoViewHolder> {

    public interface OnFecharMesaClickListener {
        void onClick(MesaPedidoResumo resumo);
    }

    public interface OnBaixarPedidosClickListener {
        void onClick(MesaPedidoResumo resumo);
    }

    private final List<MesaPedidoResumo> resumos;
    private final OnFecharMesaClickListener fecharCallback;
    private final OnBaixarPedidosClickListener baixarCallback;
    private final Context context;
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final SimpleDateFormat horaFormatada = new SimpleDateFormat("HH:mm", new Locale("pt", "BR"));

    public PedidoResumoAdapter(List<MesaPedidoResumo> resumos, OnFecharMesaClickListener fecharCallback, OnBaixarPedidosClickListener baixarCallback, Context context) {
        this.resumos = resumos;
        this.fecharCallback = fecharCallback;
        this.baixarCallback = baixarCallback;
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
        holder.textAlerta.setText(resumo.temPedidosPendentesBaixa()
                ? "Ha pedidos novos aguardando baixa."
                : "Todos os pedidos abertos ja foram baixados.");
        holder.textAlerta.setVisibility(resumo.temPedidosAbertos() ? View.VISIBLE : View.GONE);
        holder.buttonBaixar.setBackground(context.getResources().getDrawable(R.drawable.button_principal));
        holder.buttonBaixar.setEnabled(resumo.temPedidosPendentesBaixa());
        holder.buttonBaixar.setAlpha(resumo.temPedidosPendentesBaixa() ? 1f : 0.5f);
        holder.buttonBaixar.setOnClickListener(v -> baixarCallback.onClick(resumo));
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
            builder.append(pedido.getBaixado() != null && pedido.getBaixado() ? "[EM PREPARACAO] " : "[PENDENTE DE BAIXA] ")
                    .append(formatarHora(pedido))
                    .append(" - ")
                    .append(quantidade)
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

    private String formatarHora(Pedido pedido) {
        if (pedido.getCriadoEm() == null) {
            return "--:--";
        }

        Date data = pedido.getCriadoEm().toDate();
        return horaFormatada.format(data);
    }

    @Override
    public int getItemCount() {
        return resumos.size();
    }

    public static class PedidoResumoViewHolder extends RecyclerView.ViewHolder {
        TextView textMesa, textItens, textTotal, textAlerta;
        Button buttonFechar, buttonBaixar;

        public PedidoResumoViewHolder(@NonNull View itemView) {
            super(itemView);
            textMesa = itemView.findViewById(R.id.textMesaPedido);
            textItens = itemView.findViewById(R.id.textItensPedido);
            textTotal = itemView.findViewById(R.id.textTotalPedido);
            textAlerta = itemView.findViewById(R.id.textAlertaPedido);
            buttonBaixar = itemView.findViewById(R.id.buttonBaixarPedidos);
            buttonFechar = itemView.findViewById(R.id.buttonFecharMesa);
        }
    }
}
