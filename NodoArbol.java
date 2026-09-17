/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package caja;

public class NodoArbol {
    prestamos dato; // Tu clase de negocio original
    NodoArbol izquierdo;
    NodoArbol derecho;

    public NodoArbol(prestamos prestamo) {
        this.dato = prestamo;
        this.izquierdo = null;
        this.derecho = null;
    }
}