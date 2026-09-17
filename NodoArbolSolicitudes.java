/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package caja;

public class NodoArbolSolicitudes {
    int idSolicitud;
    String estudiante;
    double promedioNotas;
    NodoArbolSolicitudes izquierdo, derecho;

    public NodoArbolSolicitudes(int idSolicitud, String estudiante, double promedioNotas) {
        this.idSolicitud = idSolicitud;
        this.estudiante = estudiante;
        this.promedioNotas = promedioNotas;
        this.izquierdo = null;
        this.derecho = null;
    }
}