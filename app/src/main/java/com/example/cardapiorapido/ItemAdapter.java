package com.example.cardapiorapido;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> itens;
    private OnItemClickListener alterarCallback, deletarCallback;
    private Context context;

    // Interface pública para cliques
    public interface OnItemClickListener {
        void onClick(Item item);
    }

    // Modificando o construtor para receber os callbacks e o contexto
    public ItemAdapter(List<Item> itens, OnItemClickListener alterarCallback, OnItemClickListener deletarCallback, Context context) {
        this.itens = itens;
        this.alterarCallback = alterarCallback;
        this.deletarCallback = deletarCallback;
        this.context = context;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itens.get(position);
        holder.textNome.setText(item.getNome());
        holder.textDescricao.setText(item.getDescricao());
        holder.textValor.setText(String.valueOf(item.getValor()));
        holder.buttonAlterar.setBackground(ContextCompat.getDrawable(context, R.drawable.button_item));
        holder.buttonDeletar.setBackground(ContextCompat.getDrawable(context, R.drawable.button_item));

        // Aplicando o estilo para os TextViews
         // Nome - cor específica
        holder.textNome.setTextSize(16);  // Tamanho da fonte para o Nome
         // Fonte para o Nome

          // Descrição - cor específica
        holder.textDescricao.setTextSize(14);  // Tamanho da fonte para Descrição
          // Fonte para a Descrição

          // Valor - cor específica
        holder.textValor.setTextSize(14);  // Tamanho da fonte para Valor
          // Fonte para o Valor

        // Botão "Alterar" chama o callback de alterar
        holder.buttonAlterar.setOnClickListener(v -> {
            if (item != null && item.getId() != null) {  // Confirme que o ID não é nulo
                Intent intent = new Intent(context, AlterarItem.class);
                intent.putExtra("itemId", item.getId());  // Passando corretamente o ID do item
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Erro: ID do item é nulo.", Toast.LENGTH_SHORT).show();
            }
        });


        // Botão "Deletar" chama o callback de deletar
        holder.buttonDeletar.setOnClickListener(v -> {
            if (item != null) {
                deletarCallback.onClick(item); // Chama a callback de "Deletar"
            }
        });

        // Estilo dos botões
        holder.buttonAlterar.setBackground(context.getResources().getDrawable(R.drawable.button_item));  // Botão verde para alterar
        holder.buttonAlterar.setTextColor(context.getResources().getColor(android.R.color.white));

        holder.buttonDeletar.setBackground(context.getResources().getDrawable(R.drawable.button_item));  // Botão vermelho para deletar
        holder.buttonDeletar.setTextColor(context.getResources().getColor(android.R.color.white));
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView textNome, textDescricao, textValor;
        Button buttonAlterar, buttonDeletar;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textNome = itemView.findViewById(R.id.textNome);
            textDescricao = itemView.findViewById(R.id.textDescricao);
            textValor = itemView.findViewById(R.id.textValor);
            buttonAlterar = itemView.findViewById(R.id.buttonAlterar);
            buttonDeletar = itemView.findViewById(R.id.buttonDeletar);
        }
    }
}
