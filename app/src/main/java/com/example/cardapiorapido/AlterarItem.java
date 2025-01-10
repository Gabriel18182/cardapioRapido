package com.example.cardapiorapido;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AlterarItem extends AppCompatActivity {

    private EditText editTextNome, editTextDescricao, editTextValor;
    private Button buttonSalvar, buttonVoltar;
    private ImageView iconeUsuario;
    private String itemId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alterar_item);

        // Inicializar o Firestore
        db = FirebaseFirestore.getInstance();

        // Inicializar os componentes da interface
        editTextNome = findViewById(R.id.nomeItem);
        editTextDescricao = findViewById(R.id.descricaoItem);
        editTextValor = findViewById(R.id.valorItem);
        buttonSalvar = findViewById(R.id.buttonAdicionar);
        buttonVoltar = findViewById(R.id.buttonVoltar);
        iconeUsuario = findViewById(R.id.iconeUsuario);

        // Recuperar o itemId que foi passado de outra atividade
        itemId = getIntent().getStringExtra("itemId");

        if (itemId == null || itemId.isEmpty()) {
            Toast.makeText(this, "Erro: ID do item não foi fornecido.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Carregar os dados do item com o ID obtido
        carregarItem(itemId);

        // Configurar o clique do botão "Salvar"
        buttonSalvar.setOnClickListener(v -> salvarAlteracoes());



        // Configurar o botão "Voltar"
        buttonVoltar.setOnClickListener(v -> {
            // Voltar para a Tela Principal (TelaPrincipal)
            Intent intent = new Intent(AlterarItem.this, TelaPrincipal.class);
            startActivity(intent);
            finish();
        });



        // Configurar o clique no ícone de usuário para redirecionar ao perfil
        iconeUsuario.setOnClickListener(v -> {
            Intent intent = new Intent(AlterarItem.this, Perfil.class);
            startActivity(intent);
        });
    }

    private void carregarItem(String itemId) {
        db.collection("itens").document(itemId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Item item = documentSnapshot.toObject(Item.class);
                        if (item != null) {
                            editTextNome.setText(item.getNome());
                            editTextDescricao.setText(item.getDescricao());
                            editTextValor.setText(item.getValor());
                        }
                    } else {
                        Toast.makeText(this, "Item não encontrado.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar o item.", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void salvarAlteracoes() {
        final String novoNome = editTextNome.getText().toString().trim();
        final String novaDescricao = editTextDescricao.getText().toString().trim();
        final String novoValor = editTextValor.getText().toString().trim();

        if (novoNome.isEmpty() || novaDescricao.isEmpty() || novoValor.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos corretamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> itemAtualizado = new HashMap<>();
        itemAtualizado.put("nome", novoNome);
        itemAtualizado.put("descricao", novaDescricao);
        itemAtualizado.put("valor", novoValor);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("itens").document(itemId)
                .update(itemAtualizado)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Item alterado com sucesso!", Toast.LENGTH_SHORT).show();


                    // Enviar resultado para TelaPrincipal indicando que a lista deve ser recarregada
                    Intent intent = new Intent(AlterarItem.this, TelaPrincipal.class);
                    startActivity(intent);
                    finish(); // Envia o resultado OK


                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao salvar as alterações: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
