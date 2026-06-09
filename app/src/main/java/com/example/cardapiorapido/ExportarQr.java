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
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.FileOutputStream;
import java.util.EnumMap;
import java.util.Map;

public class ExportarQr extends AppCompatActivity {

    private ImageView imageViewQrCode;
    private Button buttonExportar, buttonVoltar;
    private Bitmap qrBitmap;
    private String linkCardapio;
    private String mesaId;
    private String mesaNome;
    private ImageView iconeUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exportar_qr);

        imageViewQrCode = findViewById(R.id.imageViewQrCode);
        buttonExportar = findViewById(R.id.buttonCompartilhar);
        buttonVoltar = findViewById(R.id.buttonVoltar);
        iconeUsuario = findViewById(R.id.iconeUsuario);
        mesaId = getIntent().getStringExtra("mesaId");
        mesaNome = getIntent().getStringExtra("mesaNome");

        gerarQrCode();

        buttonExportar.setOnClickListener(v -> compartilharQrCode());

        iconeUsuario.setOnClickListener(v -> {
            Intent intent = new Intent(ExportarQr.this, Perfil.class);
            startActivity(intent);
        });

        buttonVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(ExportarQr.this, TelaPrincipal.class);
            startActivity(intent);
            finish();
        });
    }

    private void gerarQrCode() {
        FirebaseUser usuarioAtual = FirebaseAuth.getInstance().getCurrentUser();

        if (usuarioAtual == null) {
            Toast.makeText(this, "Faca login novamente para gerar o QR Code.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            linkCardapio = montarLinkCardapio(usuarioAtual.getUid());
            qrBitmap = gerarBitmapQrCode(linkCardapio, 800);
            imageViewQrCode.setImageBitmap(qrBitmap);
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao gerar QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String montarLinkCardapio(String usuarioId) {
        Uri.Builder builder = Uri.parse(getString(R.string.cardapio_public_base_url))
                .buildUpon()
                .appendQueryParameter("usuarioId", usuarioId);

        if (mesaId != null && !mesaId.trim().isEmpty()) {
            builder.appendQueryParameter("mesaId", mesaId);
        }

        return builder.build().toString();
    }

    private Bitmap gerarBitmapQrCode(String qrContent, int tamanho) throws Exception {
        MultiFormatWriter writer = new MultiFormatWriter();
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 2);

        BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, tamanho, tamanho, hints);

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
            Toast.makeText(this, "QR Code ainda nao foi gerado.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File picturesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (picturesDir == null) {
                throw new IllegalStateException("Nao foi possivel acessar a pasta de imagens.");
            }

            File file = new File(picturesDir, "qr_code.png");
            FileOutputStream outputStream = new FileOutputStream(file);
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            Uri uri = FileProvider.getUriForFile(this, "com.example.cardapiorapido.provider", file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            String textoCompartilhar = mesaNome == null || mesaNome.trim().isEmpty()
                    ? "Acesse o cardapio: " + linkCardapio
                    : "Acesse o cardapio da mesa " + mesaNome + ": " + linkCardapio;
            shareIntent.putExtra(Intent.EXTRA_TEXT, textoCompartilhar);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Compartilhar QR Code"));

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao compartilhar QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
