/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package caja;

public class ArbolSolicitudes {
    private NodoArbolSolicitudes raiz;

    public void insertar(int id, String est, double promedio) {
        raiz = insertarRec(raiz, id, est, promedio);
    }

    private NodoArbolSolicitudes insertarRec(NodoArbolSolicitudes actual, int id, String est, double promedio) {
        if (actual == null) {
            return new NodoArbolSolicitudes(id, est, promedio);
        }
        // Criterio de ordenamiento: de menor a mayor promedio de notas
        if (promedio < actual.promedioNotas) {
            actual.izquierdo = insertarRec(actual.izquierdo, id, est, promedio);
        } else {
            actual.derecho = insertarRec(actual.derecho, id, est, promedio);
        }
        return actual;
    }

    public void mostrarPostOrder() {
        System.out.println("\n=== ÁRBOL DE SOLICITUDES RECURSIVAS (POST-ORDER) ===");
        postOrderRec(raiz);
        System.out.println("====================================================\n");
    }

    private void postOrderRec(NodoArbolSolicitudes actual) {
        if (actual != null) {
            postOrderRec(actual.izquierdo);
            postOrderRec(actual.derecho);
            System.out.println("[SOL-" + actual.idSolicitud + "] Estudiante: " + actual.estudiante + " | Promedio: " + actual.promedioNotas);
        }
    }
}
