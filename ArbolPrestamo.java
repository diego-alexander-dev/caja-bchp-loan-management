/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package caja;

public class ArbolPrestamo{
    private NodoArbol raiz;

    public ArbolPrestamo() {
        this.raiz = null;
    }

    // Método principal para insertar un préstamo
    public void insertar(prestamos nuevoPrestamo) {
        raiz = insertarRec(raiz, nuevoPrestamo);
    }

    private NodoArbol insertarRec(NodoArbol actual, prestamos nuevoPrestamo) {
        if (actual == null) {
            return new NodoArbol(nuevoPrestamo);
        }
        // Criterio de ordenamiento: saldo del crédito
        if (nuevoPrestamo.saldo < actual.dato.saldo) {
            actual.izquierdo = insertarRec(actual.izquierdo, nuevoPrestamo);
        } else {
            actual.derecho = insertarRec(actual.derecho, nuevoPrestamo);
        }
        return actual;
    }

    // Recorrido In-Order para listar de menor a mayor deuda
    public void mostrarInOrder() {
        System.out.println("\n=== PRÉSTAMOS EN EL ÁRBOL (ORDENADOS POR SALDO DE MENOR A MAYOR) ===");
        inOrderRec(raiz);
        System.out.println("==================================================================\n");
    }

    private void inOrderRec(NodoArbol actual) {
        if (actual != null) {
            inOrderRec(actual.izquierdo);
            System.out.println(actual.dato.toString());
            inOrderRec(actual.derecho);
        }
    }

    // Busca el nodo que está más a la derecha (Deuda Máxima)
    public prestamos obtenerDeudaMaxima() {
        if (raiz == null) return null;
        NodoArbol actual = raiz;
        while (actual.derecho != null) {
            actual = actual.derecho;
        }
        return actual.dato;
    }

    // Busca el nodo que está más a la izquierda (Deuda Mínima)
    public prestamos obtenerDeudaMinima() {
        if (raiz == null) return null;
        NodoArbol actual = raiz;
        while (actual.izquierdo != null) {
            actual = actual.izquierdo;
        }
        return actual.dato;
    }
}