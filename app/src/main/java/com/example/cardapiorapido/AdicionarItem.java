package com.example.cardapiorapido;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdicionarItem extends AppCompatActivity {

    private EditText editNomeItem, editDescricaoItem, editPrecoItem;
    private Button buttonSalvarItem, buttonCancelar;
    private ImageView iconeUsuario; // ImageView para redirecionar ao perfil
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_item);

        // Inicializar o Firestore
        db = FirebaseFirestore.getInstance();

        // Inicializar os componentes da interface
        editNomeItem = findViewById(R.id.nomeItem);
        editDescricaoItem = findViewById(R.id.descricaoItem);
        editPrecoItem = findViewById(R.id.valorItem);
        buttonSalvarItem = findViewById(R.id.buttonAdicionar);
        buttonCancelar = findViewById(R.id.buttonCancelar);
        iconeUsuario = findViewById(R.id.iconeUsuario); // Inicializar o ImageView

        // Configurar o clique do botão "Salvar Item"
        buttonSalvarItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarItem();
            }
        });

        // Configurar o clique do botão "Cancelar"
        buttonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Voltar para a Tela Principal (TelaPrincipal)
                Intent intent = new Intent(AdicionarItem.this, TelaPrincipal.class);
                startActivity(intent);
                finish();
            }
        });

        // Configurar o clique no ícone de usuário para redirecionar ao perfil
        iconeUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdicionarItem.this, Perfil.class);
                startActivity(intent);
            }
        });
    }

    private void salvarItem() {
        // Obter os dados inseridos pelo usuário
        String nome = editNomeItem.getText().toString();
        String descricao = editDescricaoItem.getText().toString();
        String preco = editPrecoItem.getText().toString();  // Já é uma String

        if (nome.isEmpty() || descricao.isEmpty() || preco.isEmpty()) {
            // Se algum campo estiver vazio, mostrar um aviso
            Toast.makeText(AdicionarItem.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
        } else {
            // Obter o usuário atual (usando Firebase Authentication)
            String usuarioId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Certifique-se de que o valor de preco seja uma String
            String precoStr = preco.trim();  // Garantir que não haja espaços antes ou depois

            // Criar um objeto de item com um ID aleatório e o usuarioId
            Item novoItem = new Item(nome, descricao, precoStr, usuarioId);  // Enviar preco como String

            // Salvar no Firebase Firestore
            db.collection("itens")
                    .add(novoItem) // O Firestore cria o documento com o ID aleatório
                    .addOnSuccessListener(documentReference -> {
                        // Exibir uma mensagem de sucesso
                        Toast.makeText(AdicionarItem.this, "Item adicionado com sucesso", Toast.LENGTH_SHORT).show();

                        // Agora o item tem o ID aleatório, você pode continuar a lógica
                        Intent intent = new Intent(AdicionarItem.this, TelaPrincipal.class);
                        startActivity(intent);
                        finish(); // Fecha a atividade atual
                    })
                    .addOnFailureListener(e -> {
                        // Exibir o erro detalhado para depuração
                        String errorMessage = e.getMessage();
                        Toast.makeText(AdicionarItem.this, "Erro ao adicionar item: " + errorMessage, Toast.LENGTH_LONG).show();
                    });
        }
    }
}
