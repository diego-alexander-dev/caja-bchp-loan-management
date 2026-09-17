/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package caja;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class SeguridadBCHP {
    
    // Almacén global en memoria que guardará la firma digital legítima de la base de datos
    public static String HASH_MAESTRO_ESTUDIANTES = "";

    // Algoritmo matemático para procesar cualquier texto y devolver su firma SHA-256 única
    public static String calcularSHA256(String textoOriginal) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashGrabado = digest.digest(textoOriginal.getBytes(StandardCharsets.UTF_8));
            
            // Convertimos la matriz de bytes en una cadena hexadecimal de 64 caracteres
            StringBuilder hexString = new StringBuilder(2 * hashGrabado.length);
            for (byte b : hashGrabado) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (Exception e) {
            System.out.println("Error Criptográfico: " + e.getMessage());
            return "";
        }
    }
}
