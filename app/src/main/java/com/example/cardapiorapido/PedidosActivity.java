package com.example.cardapiorapido;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PedidosActivity extends AppCompatActivity {

    private PedidoResumoAdapter adapter;
    private final List<Mesa> mesas = new ArrayList<>();
    private final List<Pedido> pedidosAbertos = new ArrayList<>();
    private final List<MesaPedidoResumo> resumos = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration mesasListener;
    private ListenerRegistration pedidosListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        RecyclerView recyclerViewPedidos = findViewById(R.id.recyclerViewPedidos);
        Button buttonMesas = findViewById(R.id.buttonMesasPedidos);
        Button buttonVoltar = findViewById(R.id.buttonVoltarPedidos);

        recyclerViewPedidos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PedidoResumoAdapter(resumos, this::confirmarFecharMesa, this);
        recyclerViewPedidos.setAdapter(adapter);

        buttonMesas.setOnClickListener(v -> startActivity(new Intent(this, MesasActivity.class)));
        buttonVoltar.setOnClickListener(v -> {
            startActivity(new Intent(this, TelaPrincipal.class));
            finish();
        });

        carregarDados();
    }

    private void carregarDados() {
        FirebaseUser usuarioAtual = auth.getCurrentUser();
        if (usuarioAtual == null) {
            Toast.makeText(this, "Faca login novamente.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mesasListener = db.collection(AppConstants.COLECAO_MESAS)
                .whereEqualTo("usuarioId", usuarioAtual.getUid())
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Erro ao carregar mesas: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    mesas.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot document : value) {
                            Mesa mesa = document.toObject(Mesa.class);
                            mesa.setId(document.getId());
                            mesas.add(mesa);
                        }
                    }
                    Collections.sort(mesas, (a, b) -> String.valueOf(a.getNome()).compareToIgnoreCase(String.valueOf(b.getNome())));
                    montarResumos();
                });

        pedidosListener = db.collection(AppConstants.COLECAO_PEDIDOS)
                .whereEqualTo("usuarioId", usuarioAtual.getUid())
                .whereEqualTo("status", "aberto")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Erro ao carregar pedidos: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    pedidosAbertos.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot document : value) {
                            Pedido pedido = document.toObject(Pedido.class);
                            pedido.setId(document.getId());
                            pedidosAbertos.add(pedido);
                        }
                    }
                    montarResumos();
                });
    }

    private void montarResumos() {
        Map<String, MesaPedidoResumo> porMesa = new LinkedHashMap<>();

        for (Mesa mesa : mesas) {
            porMesa.put(mesa.getId(), new MesaPedidoResumo(mesa));
        }

        for (Pedido pedido : pedidosAbertos) {
            String mesaId = pedido.getMesaId();
            if (!porMesa.containsKey(mesaId)) {
                Mesa mesaAvulsa = new Mesa();
                mesaAvulsa.setId(mesaId);
                mesaAvulsa.setNome(pedido.getMesaNome() == null ? "Mesa removida" : pedido.getMesaNome());
                mesaAvulsa.setUsuarioId(pedido.getUsuarioId());
                porMesa.put(mesaId, new MesaPedidoResumo(mesaAvulsa));
            }
            porMesa.get(mesaId).addPedido(pedido);
        }

        resumos.clear();
        resumos.addAll(porMesa.values());
        adapter.notifyDataSetChanged();
    }

    private void confirmarFecharMesa(MesaPedidoResumo resumo) {
        if (!resumo.temPedidosAbertos()) {
            Toast.makeText(this, "Esta mesa nao possui pedidos abertos.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Fechar pedido")
                .setMessage("Fechar o pedido da mesa " + resumo.getMesa().getNome() + " e liberar para o proximo cliente?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Fechar", (dialog, which) -> fecharMesa(resumo))
                .show();
    }

    private void fecharMesa(MesaPedidoResumo resumo) {
        FirebaseUser usuarioAtual = auth.getCurrentUser();
        if (usuarioAtual == null) {
            return;
        }

        db.collection(AppConstants.COLECAO_PEDIDOS)
                .whereEqualTo("usuarioId", usuarioAtual.getUid())
                .whereEqualTo("mesaId", resumo.getMesa().getId())
                .whereEqualTo("status", "aberto")
                .get()
                .addOnSuccessListener(query -> {
                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot document : query) {
                        batch.update(document.getReference(), "status", "fechado");
                        batch.update(document.getReference(), "fechadoEm", FieldValue.serverTimestamp());
                    }
                    batch.commit()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Mesa fechada.", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Erro ao fechar mesa: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao buscar pedidos da mesa.", Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mesasListener != null) {
            mesasListener.remove();
        }
        if (pedidosListener != null) {
            pedidosListener.remove();
        }
    }
}
