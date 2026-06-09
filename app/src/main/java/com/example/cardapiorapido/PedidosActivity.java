package com.example.cardapiorapido;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
    private final List<String> opcoesFiltro = new ArrayList<>();
    private Spinner spinnerFiltroMesa;
    private String mesaFiltroId = "";
    private boolean atualizandoFiltro = false;
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
        spinnerFiltroMesa = findViewById(R.id.spinnerFiltroMesa);
        Button buttonMesas = findViewById(R.id.buttonMesasPedidos);
        Button buttonVoltar = findViewById(R.id.buttonVoltarPedidos);

        recyclerViewPedidos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PedidoResumoAdapter(resumos, this::confirmarFecharMesa, this::confirmarBaixarPedidos, this);
        recyclerViewPedidos.setAdapter(adapter);
        configurarFiltroMesas();

        buttonMesas.setOnClickListener(v -> startActivity(new Intent(this, MesasActivity.class)));
        buttonVoltar.setOnClickListener(v -> {
            startActivity(new Intent(this, TelaPrincipal.class));
            finish();
        });

        carregarDados();
    }

    private void configurarFiltroMesas() {
        ArrayAdapter<String> filtroAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opcoesFiltro);
        filtroAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltroMesa.setAdapter(filtroAdapter);
        spinnerFiltroMesa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (atualizandoFiltro) {
                    return;
                }

                mesaFiltroId = position <= 0 || position - 1 >= mesas.size() ? "" : mesas.get(position - 1).getId();
                montarResumos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mesaFiltroId = "";
                montarResumos();
            }
        });
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
                    atualizarOpcoesFiltro();
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
                    Collections.sort(pedidosAbertos, (a, b) -> {
                        if (a.getCriadoEm() == null && b.getCriadoEm() == null) {
                            return 0;
                        }
                        if (a.getCriadoEm() == null) {
                            return 1;
                        }
                        if (b.getCriadoEm() == null) {
                            return -1;
                        }
                        return a.getCriadoEm().compareTo(b.getCriadoEm());
                    });
                    montarResumos();
                });
    }

    private void atualizarOpcoesFiltro() {
        int selecaoAnterior = spinnerFiltroMesa.getSelectedItemPosition();
        String filtroAnterior = mesaFiltroId;

        atualizandoFiltro = true;
        opcoesFiltro.clear();
        opcoesFiltro.add("Todas as mesas");
        for (Mesa mesa : mesas) {
            opcoesFiltro.add("Mesa " + mesa.getNome());
        }

        ArrayAdapter<String> adapterFiltro = (ArrayAdapter<String>) spinnerFiltroMesa.getAdapter();
        adapterFiltro.notifyDataSetChanged();

        int novaSelecao = 0;
        for (int i = 0; i < mesas.size(); i++) {
            if (mesas.get(i).getId().equals(filtroAnterior)) {
                novaSelecao = i + 1;
                break;
            }
        }

        if (selecaoAnterior >= 0 && novaSelecao < opcoesFiltro.size()) {
            spinnerFiltroMesa.setSelection(novaSelecao);
        }
        mesaFiltroId = novaSelecao == 0 ? "" : mesas.get(novaSelecao - 1).getId();
        atualizandoFiltro = false;
    }

    private void montarResumos() {
        Map<String, MesaPedidoResumo> porMesa = new LinkedHashMap<>();

        for (Mesa mesa : mesas) {
            if (!mesaFiltroId.isEmpty() && !mesaFiltroId.equals(mesa.getId())) {
                continue;
            }
            porMesa.put(mesa.getId(), new MesaPedidoResumo(mesa));
        }

        for (Pedido pedido : pedidosAbertos) {
            String mesaId = pedido.getMesaId();
            if (!mesaFiltroId.isEmpty() && !mesaFiltroId.equals(mesaId)) {
                continue;
            }
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

    private void confirmarBaixarPedidos(MesaPedidoResumo resumo) {
        if (!resumo.temPedidosPendentesBaixa()) {
            Toast.makeText(this, "Nao ha pedidos novos para dar baixa.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Dar baixa")
                .setMessage("Marcar os pedidos novos da mesa " + resumo.getMesa().getNome() + " como registrados/em preparacao?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Dar baixa", (dialog, which) -> baixarPedidosNovos(resumo))
                .show();
    }

    private void baixarPedidosNovos(MesaPedidoResumo resumo) {
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
                    boolean temBaixa = false;
                    for (QueryDocumentSnapshot document : query) {
                        Boolean baixado = document.getBoolean("baixado");
                        if (baixado == null || !baixado) {
                            batch.update(document.getReference(), "baixado", true);
                            batch.update(document.getReference(), "baixadoEm", FieldValue.serverTimestamp());
                            temBaixa = true;
                        }
                    }

                    if (!temBaixa) {
                        Toast.makeText(this, "Todos os pedidos ja estavam baixados.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    batch.commit()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Baixa registrada.", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Erro ao dar baixa: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao buscar pedidos novos.", Toast.LENGTH_LONG).show());
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
