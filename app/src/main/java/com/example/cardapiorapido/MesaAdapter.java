package com.example.cardapiorapido;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MesaAdapter extends RecyclerView.Adapter<MesaAdapter.MesaViewHolder> {

    public interface OnMesaClickListener {
        void onClick(Mesa mesa);
    }

    private final List<Mesa> mesas;
    private final OnMesaClickListener alterarCallback;
    private final OnMesaClickListener deletarCallback;
    private final OnMesaClickListener compartilharCallback;
    private final Context context;

    public MesaAdapter(List<Mesa> mesas, OnMesaClickListener alterarCallback, OnMesaClickListener deletarCallback, OnMesaClickListener compartilharCallback, Context context) {
        this.mesas = mesas;
        this.alterarCallback = alterarCallback;
        this.deletarCallback = deletarCallback;
        this.compartilharCallback = compartilharCallback;
        this.context = context;
    }

    @NonNull
    @Override
    public MesaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mesa_layout, parent, false);
        return new MesaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MesaViewHolder holder, int position) {
        Mesa mesa = mesas.get(position);
        holder.textNomeMesa.setText(mesa.getNome());
        holder.buttonAlterar.setBackground(context.getResources().getDrawable(R.drawable.button_item));
        holder.buttonDeletar.setBackground(context.getResources().getDrawable(R.drawable.button_item));
        holder.buttonAlterar.setOnClickListener(v -> alterarCallback.onClick(mesa));
        holder.buttonDeletar.setOnClickListener(v -> deletarCallback.onClick(mesa));
        holder.buttonCompartilhar.setOnClickListener(v -> compartilharCallback.onClick(mesa));
    }

    @Override
    public int getItemCount() {
        return mesas.size();
    }

    public static class MesaViewHolder extends RecyclerView.ViewHolder {
        TextView textNomeMesa;
        Button buttonAlterar, buttonDeletar, buttonCompartilhar;

        public MesaViewHolder(@NonNull View itemView) {
            super(itemView);
            textNomeMesa = itemView.findViewById(R.id.textNomeMesa);
            buttonAlterar = itemView.findViewById(R.id.buttonAlterarMesa);
            buttonDeletar = itemView.findViewById(R.id.buttonDeletarMesa);
            buttonCompartilhar = itemView.findViewById(R.id.buttonCompartilharMesa);
        }
    }
}
