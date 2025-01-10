package com.example.cardapiorapido;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;

public class ExportarQr extends AppCompatActivity {

    private ImageView imageViewQrCode;
    private Button buttonExportar, buttonVoltar;
    private Bitmap qrBitmap;
    private ImageView iconeUsuario; // ImageView para redirecionar ao perfil do usuário

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exportar_qr);

        imageViewQrCode = findViewById(R.id.imageViewQrCode);
        buttonExportar = findViewById(R.id.buttonCompartilhar); // Corrigido para o novo ID
        buttonVoltar = findViewById(R.id.buttonVoltar); // Botão de voltar
        iconeUsuario = findViewById(R.id.iconeUsuario); // Inicializar o ImageView para o ícone do usuário

        // Verifique se a permissão foi concedida
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Se a permissão não foi concedida, solicite-a
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        } else {
            // Se a permissão já foi concedida, pode prosseguir com a operação
            gerarQrCode();
        }

        // Configurar o botão para compartilhar o QR Code
        buttonExportar.setOnClickListener(v -> compartilharQrCode());

        // Configurar o clique no ícone de usuário para redirecionar ao perfil
        iconeUsuario.setOnClickListener(v -> {
            Intent intent = new Intent(ExportarQr.this, Perfil.class);
            startActivity(intent);
        });

        // Configurar o botão de voltar para retornar à tela principal
        buttonVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(ExportarQr.this, TelaPrincipal.class);
            startActivity(intent);
            finish();  // Fecha a tela atual para não manter na pilha de atividades
        });
    }

    private void gerarQrCode() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String usuarioId = mAuth.getCurrentUser().getUid();  // Obtendo o ID do usuário logado

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Buscar apenas os itens do usuário logado
        db.collection("itens")
                .whereEqualTo("usuarioId", usuarioId)  // Filtrar pelos itens do usuário logado
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {
                        List<JSONObject> itensList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            JSONObject itemJson = new JSONObject();
                            itemJson.put("id", document.getId());
                            itemJson.put("nome", document.getString("nome"));
                            itemJson.put("descricao", document.getString("descricao"));

                            // Tratar o valor para garantir que seja uma string
                            Object valorObj = document.get("valor");
                            String valor = "";
                            if (valorObj != null) {
                                if (valorObj instanceof String) {
                                    valor = (String) valorObj;
                                } else if (valorObj instanceof Double) {
                                    valor = String.format("%.2f", valorObj);  // Converter para String com duas casas decimais
                                } else if (valorObj instanceof Long) {
                                    valor = String.valueOf(valorObj);  // Converter para String
                                }
                            }
                            itemJson.put("valor", valor);

                            itensList.add(itemJson);
                        }

                        // Criar um JSON Array com os itens
                        JSONArray itensArray = new JSONArray(itensList);
                        String qrContent = itensArray.toString();

                        // Gerar o QR Code
                        qrBitmap = gerarBitmapQrCode(qrContent, 800);  // Defina o tamanho do QR Code
                        imageViewQrCode.setImageBitmap(qrBitmap);

                        // Gerar o PDF com os itens
                        criarPdfComItens(queryDocumentSnapshots.getDocuments());  // Passando os documentos filtrados

                    } catch (Exception e) {
                        Toast.makeText(ExportarQr.this, "Erro ao processar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ExportarQr.this, "Erro ao carregar itens: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private Bitmap gerarBitmapQrCode(String qrContent, int tamanho) throws Exception {
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, tamanho, tamanho);

        Bitmap bitmap = Bitmap.createBitmap(tamanho, tamanho, Bitmap.Config.RGB_565);
        for (int x = 0; x < tamanho; x++) {
            for (int y = 0; y < tamanho; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? android.graphics.Color.BLACK : android.graphics.Color.WHITE);
            }
        }
        return bitmap;
    }

    private void compartilharQrCode() {
        if (qrBitmap == null) {
            Toast.makeText(this, "QR Code ainda não foi gerado.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Salvar o QR Code em um arquivo temporário
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "qr_code.png");
            FileOutputStream outputStream = new FileOutputStream(file);
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            // Usar FileProvider para obter uma URI segura
            Uri uri = FileProvider.getUriForFile(this, "com.example.cardapiorapido.provider", file);

            // Compartilhar o arquivo
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Garantir permissão de leitura para o arquivo
            startActivity(Intent.createChooser(shareIntent, "Compartilhar QR Code"));

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao compartilhar QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) { // A mesma constante usada ao solicitar a permissão
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão concedida, então prosseguir com a operação
                gerarQrCode();
            } else {
                // Permissão negada, exibir mensagem ao usuário
                Toast.makeText(this, "Permissão para salvar no armazenamento negada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void criarPdfComItens(List<DocumentSnapshot> documentos) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Criar o PDF
        try {
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(600, 800, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setTextSize(12);
            paint.setColor(Color.BLACK);

            // Escrever título no PDF
            canvas.drawText("Cardápio", 200, 30, paint);
            int yPos = 60;

            // Iterar pelos itens e escrever no PDF
            for (DocumentSnapshot documentSnapshot : documentos) {
                String nome = documentSnapshot.getString("nome");
                String descricao = documentSnapshot.getString("descricao");

                // Obter o valor do campo "valor" e garantir que seja uma String
                Object valorObj = documentSnapshot.get("valor");
                String valor = "";

                if (valorObj != null) {
                    if (valorObj instanceof String) {
                        valor = (String) valorObj;  // Se for String, mantenha como está
                    } else if (valorObj instanceof Double) {
                        valor = String.format("%.2f", valorObj);  // Converter para String com duas casas decimais
                    } else if (valorObj instanceof Long) {
                        valor = String.valueOf(valorObj);  // Converter para String
                    } else {
                        valor = valorObj.toString();  // Para outros tipos, convertê-los para String
                    }
                }

                // Escrever o conteúdo do item no PDF
                String itemText = nome + " - " + descricao + " - " + valor;
                canvas.drawText(itemText, 20, yPos, paint);

                yPos += 30;  // Aumenta a posição para o próximo item
            }

            document.finishPage(page);

            // Salvar o PDF no dispositivo
            File pdfFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "cardapio.pdf");
            FileOutputStream fos = new FileOutputStream(pdfFile);
            document.writeTo(fos);

            // Fechar o documento PDF
            document.close();

            // Exibir mensagem de sucesso
            Toast.makeText(ExportarQr.this, "PDF gerado com sucesso!", Toast.LENGTH_SHORT).show();

            // Compartilhar o PDF (opcional)
            compartilharPdf(pdfFile);

        } catch (Exception e) {
            Toast.makeText(ExportarQr.this, "Erro ao criar o PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void compartilharPdf(File pdfFile) {
        try {
            Uri pdfUri = FileProvider.getUriForFile(this, "com.example.cardapiorapido.provider", pdfFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Garantir permissão de leitura para o arquivo

            startActivity(Intent.createChooser(shareIntent, "Compartilhar PDF"));
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao compartilhar PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
