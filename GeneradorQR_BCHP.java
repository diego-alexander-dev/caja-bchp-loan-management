/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package caja;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;

public class GeneradorQR_BCHP {
    public static byte[] generarBytesQR(String textoBoleta) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            // Genera la matriz de bits internacional de 256x256 píxeles
            BitMatrix bitMatrix = qrCodeWriter.encode(textoBoleta, BarcodeFormat.QR_CODE, 256, 256);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            return baos.toByteArray(); // Retorna los bytes reales de la imagen escaneable
        } catch (Exception e) {
            System.out.println("Error matemático al codificar QR: " + e.getMessage());
            return null;
        }
    }
}