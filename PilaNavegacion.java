package caja;

import javax.swing.JFrame;

public class PilaNavegacion {

    private static class Nodo {
        String nombre;
        JFrame ventana;
        Nodo siguiente;

        Nodo(String nombre, JFrame ventana) {
            this.nombre   = nombre;
            this.ventana  = ventana;
            this.siguiente = null;
        }
    }

    private Nodo tope;
    private int size;

    public PilaNavegacion() {
        tope = null;
        size = 0;
    }

    public void push(String nombre, JFrame ventana) {
        Nodo nuevo = new Nodo(nombre, ventana);
        nuevo.siguiente = tope;
        tope = nuevo;
        size++;
        System.out.println("[PILA] PUSH -> " + nombre + " | Tamano: " + size);
    }

    public JFrame pop() {
        if (isEmpty()) {
            System.out.println("[PILA] Pila vacia, no se puede POP");
            return null;
        }
        JFrame ventana = tope.ventana;
        System.out.println("[PILA] POP <- " + tope.nombre + " | Tamano: " + (size - 1));
        tope = tope.siguiente;
        size--;
        return ventana;
    }

    public String peekNombre() {
        if (isEmpty()) return null;
        return tope.nombre;
    }

    public JFrame peekVentana() {
        if (isEmpty()) return null;
        return tope.ventana;
    }

    public boolean isEmpty() {
        return tope == null;
    }

    public int size() {
        return size;
    }

    public void vaciar() {
        while (!isEmpty()) {
            JFrame v = pop();
            if (v != null) v.dispose();
        }
    }

    public void mostrarPila() {
        System.out.println("\n=== HISTORIAL DE NAVEGACION (PILA) ===");
        Nodo actual = tope;
        int pos = size;
        while (actual != null) {
            String marca = (actual == tope) ? " <TOPE>" : "";
            System.out.println(pos + ". " + actual.nombre + marca);
            actual = actual.siguiente;
            pos--;
        }
        System.out.println("=======================================\n");
    }
}

