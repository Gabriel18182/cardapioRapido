package com.example.cardapiorapido;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Perfil extends AppCompatActivity {

    private EditText emailUser, contatoUser;
    private Button buttonVoltar, buttonSair;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        // Inicializar Firebase Firestore e Auth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Associar os componentes da interface
        emailUser = findViewById(R.id.emailUser);
        contatoUser = findViewById(R.id.contatoUser);
        buttonVoltar = findViewById(R.id.buttonVoltar);
        buttonSair = findViewById(R.id.buttonSair);

        // Tornar os campos de texto não editáveis
        emailUser.setFocusable(false);
        contatoUser.setFocusable(false);

        // Carregar dados do usuário
        carregarDadosUsuario();

        // Configurar ações dos botões
        buttonVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(Perfil.this, TelaPrincipal.class);
            startActivity(intent);
            finish();
        });

        buttonSair.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(Perfil.this, FormLogin.class);
            startActivity(intent);
            finish();
        });
    }

    private void carregarDadosUsuario() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usuarios").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Recuperar os campos do Firestore
                        String email = documentSnapshot.getString("email");
                        String telefone = documentSnapshot.getString("telefone"); // Certifique-se do nome correto no Firestore

                        // Atualizar os EditText com os valores recuperados
                        emailUser.setText(email != null ? email : "N/A");
                        contatoUser.setText(telefone != null ? telefone : "N/A");
                    } else {
                        // Documento do usuário não encontrado
                        Toast.makeText(Perfil.this, "Usuário não encontrado no Firestore.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Exibir erro caso a operação falhe
                    Toast.makeText(Perfil.this, "Erro ao carregar os dados do Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

