package com.example.cardapiorapido;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MesasActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMesas;
    private MesaAdapter adapter;
    private final List<Mesa> mesas = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration mesasListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesas);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerViewMesas = findViewById(R.id.recyclerViewMesas);
        Button buttonAdicionarMesa = findViewById(R.id.buttonAdicionarMesa);
        Button buttonPedidos = findViewById(R.id.buttonPedidosMesas);
        Button buttonVoltar = findViewById(R.id.buttonVoltarMesas);

        recyclerViewMesas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MesaAdapter(mesas, this::abrirDialogEditarMesa, this::confirmarDeletarMesa, this::compartilharMesa, this);
        recyclerViewMesas.setAdapter(adapter);

        buttonAdicionarMesa.setOnClickListener(v -> abrirDialogNovaMesa());
        buttonPedidos.setOnClickListener(v -> startActivity(new Intent(this, PedidosActivity.class)));
        buttonVoltar.setOnClickListener(v -> {
            startActivity(new Intent(this, TelaPrincipal.class));
            finish();
        });

        carregarMesas();
    }

    private void carregarMesas() {
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
                    adapter.notifyDataSetChanged();
                });
    }

    private void abrirDialogNovaMesa() {
        abrirDialogMesa("Adicionar mesa", "", nome -> salvarNovaMesa(nome));
    }

    private void abrirDialogEditarMesa(Mesa mesa) {
        abrirDialogMesa("Alterar mesa", mesa.getNome(), nome -> salvarNomeMesa(mesa, nome));
    }

    private void abrirDialogMesa(String titulo, String valorInicial, MesaNomeCallback callback) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("Ex: 1, 2, A, Balcao");
        input.setText(valorInicial);
        input.setSelection(input.getText().length());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setView(input)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nome = input.getText().toString().trim();
            if (nome.isEmpty()) {
                Toast.makeText(this, "Informe o nome da mesa.", Toast.LENGTH_SHORT).show();
                return;
            }
            callback.onNomeInformado(nome);
            dialog.dismiss();
        }));

        dialog.show();
    }

    private void salvarNovaMesa(String nome) {
        FirebaseUser usuarioAtual = auth.getCurrentUser();
        if (usuarioAtual == null) {
            return;
        }

        Map<String, Object> dados = new HashMap<>();
        dados.put("nome", nome);
        dados.put("usuarioId", usuarioAtual.getUid());
        dados.put("criadaEm", FieldValue.serverTimestamp());

        db.collection(AppConstants.COLECAO_MESAS)
                .add(dados)
                .addOnSuccessListener(documentReference -> Toast.makeText(this, "Mesa adicionada.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao adicionar mesa: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void salvarNomeMesa(Mesa mesa, String novoNome) {
        db.collection(AppConstants.COLECAO_MESAS).document(mesa.getId())
                .update("nome", novoNome)
                .addOnSuccessListener(aVoid -> atualizarNomeMesaNosPedidosAbertos(mesa, novoNome))
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao alterar mesa: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void atualizarNomeMesaNosPedidosAbertos(Mesa mesa, String novoNome) {
        FirebaseUser usuarioAtual = auth.getCurrentUser();
        if (usuarioAtual == null) {
            return;
        }

        db.collection(AppConstants.COLECAO_PEDIDOS)
                .whereEqualTo("usuarioId", usuarioAtual.getUid())
                .whereEqualTo("mesaId", mesa.getId())
                .whereEqualTo("status", "aberto")
                .get()
                .addOnSuccessListener(query -> {
                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot document : query) {
                        batch.update(document.getReference(), "mesaNome", novoNome);
                    }
                    batch.commit();
                    Toast.makeText(this, "Mesa alterada.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Mesa alterada, mas nao foi possivel atualizar pedidos abertos.", Toast.LENGTH_LONG).show());
    }

    private void confirmarDeletarMesa(Mesa mesa) {
        new AlertDialog.Builder(this)
                .setTitle("Deletar mesa")
                .setMessage("Deseja deletar a mesa " + mesa.getNome() + "?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Deletar", (dialog, which) -> deletarMesaSeLivre(mesa))
                .show();
    }

    private void compartilharMesa(Mesa mesa) {
        Intent intent = new Intent(this, ExportarQr.class);
        intent.putExtra("mesaId", mesa.getId());
        intent.putExtra("mesaNome", mesa.getNome());
        startActivity(intent);
    }

    private void deletarMesaSeLivre(Mesa mesa) {
        FirebaseUser usuarioAtual = auth.getCurrentUser();
        if (usuarioAtual == null) {
            return;
        }

        db.collection(AppConstants.COLECAO_PEDIDOS)
                .whereEqualTo("usuarioId", usuarioAtual.getUid())
                .whereEqualTo("mesaId", mesa.getId())
                .whereEqualTo("status", "aberto")
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        Toast.makeText(this, "Feche o pedido da mesa antes de deletar.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    db.collection(AppConstants.COLECAO_MESAS).document(mesa.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Mesa deletada.", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Erro ao deletar mesa: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao verificar pedidos da mesa.", Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mesasListener != null) {
            mesasListener.remove();
        }
    }

    private interface MesaNomeCallback {
        void onNomeInformado(String nome);
    }
}
