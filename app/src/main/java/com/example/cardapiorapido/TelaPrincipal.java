package com.example.cardapiorapido;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TelaPrincipal extends AppCompatActivity {

    private static final int REQUEST_CODE_EDIT = 1; // Código de requisição para edição
    private static final int REQUEST_CODE_ADD = 2;  // Código de requisição para adicionar

    private RecyclerView recyclerViewItens;
    private ItemAdapter adapter;
    private List<Item> listaItens;
    private Button buttonAdicionarItem;
    private Button buttonGerarQrCode;
    private Button buttonMenu;
    private ImageView iconeUsuario;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_principal);

        recyclerViewItens = findViewById(R.id.recyclerViewItens);
        buttonAdicionarItem = findViewById(R.id.buttonAdicionar);
        buttonGerarQrCode = findViewById(R.id.buttonGerar);
        buttonMenu = findViewById(R.id.buttonMenu);
        iconeUsuario = findViewById(R.id.iconeUsuario);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // Configuração do RecyclerView
        recyclerViewItens.setLayoutManager(new LinearLayoutManager(this));
        listaItens = new ArrayList<>();
        adapter = new ItemAdapter(
                listaItens,
                item -> {
                    // Abrir AlterarItem para edição
                    Intent intent = new Intent(TelaPrincipal.this, AlterarItem.class);
                    intent.putExtra("itemId", item.getId());
                    startActivityForResult(intent, REQUEST_CODE_EDIT);  // Iniciar para edição
                },
                this::deletarItem,
                TelaPrincipal.this
        );
        recyclerViewItens.setAdapter(adapter);

        // Adicionar uma linha divisória no RecyclerView
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        Drawable dividerDrawable = getResources().getDrawable(R.drawable.divider);
        dividerItemDecoration.setDrawable(dividerDrawable);
        recyclerViewItens.addItemDecoration(dividerItemDecoration);

        // Carregar itens da Firestore
        carregarItens();

        buttonMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        configurarMenuLateral();

        // Ação do botão "Adicionar Item"
        buttonAdicionarItem.setOnClickListener(v -> {
            Intent intent = new Intent(TelaPrincipal.this, AdicionarItem.class);
            startActivityForResult(intent, REQUEST_CODE_ADD);  // Iniciar para adicionar
        });

        // Ação do botão "Gerar QR Code"
        buttonGerarQrCode.setOnClickListener(v -> {
            Intent intent = new Intent(TelaPrincipal.this, MesasActivity.class);
            startActivity(intent);
        });

        // Ação do ícone de usuário (perfil)
        iconeUsuario.setOnClickListener(v -> {
            Intent intent = new Intent(TelaPrincipal.this, Perfil.class);
            startActivity(intent);
        });
    }

    private void configurarMenuLateral() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            drawerLayout.closeDrawer(GravityCompat.START);

            if (itemId == R.id.menu_cardapio) {
                return true;
            } else if (itemId == R.id.menu_mesas) {
                startActivity(new Intent(TelaPrincipal.this, MesasActivity.class));
                return true;
            } else if (itemId == R.id.menu_pedidos) {
                startActivity(new Intent(TelaPrincipal.this, PedidosActivity.class));
                return true;
            } else if (itemId == R.id.menu_qr) {
                startActivity(new Intent(TelaPrincipal.this, MesasActivity.class));
                return true;
            } else if (itemId == R.id.menu_perfil) {
                startActivity(new Intent(TelaPrincipal.this, Perfil.class));
                return true;
            }

            return false;
        });
    }

    // Método para carregar itens da Firestore
    private void carregarItens() {
        String usuarioId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("itens")
                .whereEqualTo("usuarioId", usuarioId)
                .orderBy("nome")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        listaItens.clear();

                        for (DocumentSnapshot document : value.getDocuments()) {
                            Item item = document.toObject(Item.class);
                            if (item != null) {
                                item.setId(document.getId());
                                listaItens.add(item);
                            }
                        }

                        // Notificar o adaptador de que a lista foi alterada
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // Método para deletar um item
    private void deletarItem(Item item) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("itens").document(item.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    listaItens.remove(item);
                    adapter.notifyDataSetChanged();  // Atualizar a lista do RecyclerView
                    Toast.makeText(this, "Item deletado com sucesso!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao deletar o item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Método para lidar com o retorno das atividades
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Verifica se o resultado foi OK (alteração ou adição de item bem-sucedida)
        if (resultCode == RESULT_OK) {
            // Se foi o retorno da edição ou da adição, recarregar os itens
            carregarItens();
        }
    }
}
