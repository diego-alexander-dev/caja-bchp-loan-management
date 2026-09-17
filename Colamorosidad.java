package caja;

public class Colamorosidad {

    private static class Nodo {
        String mensaje;
        int idPrestamo;
        String nombreEstudiante;
        Nodo siguiente;

        Nodo(int idPrestamo, String nombreEstudiante, String mensaje) {
            this.idPrestamo = idPrestamo;
            this.nombreEstudiante = nombreEstudiante;
            this.mensaje = mensaje;
            this.siguiente = null;
        }
    }

    private Nodo frente;
    private Nodo fin;
    private int size;

    public Colamorosidad() {
        frente = null;
        fin = null;
        size = 0;
    }

    public void encolar(int idPrestamo, String nombreEstudiante, String mensaje) {
        Nodo nuevo = new Nodo(idPrestamo, nombreEstudiante, mensaje);
        if (fin == null) {
            frente = nuevo;
            fin = nuevo;
        } else {
            fin.siguiente = nuevo;
            fin = nuevo;
        }
        size++;
        System.out.println("[COLA] ENCOLADO: " + nombreEstudiante + " | Tamano: " + size);
    }

    public String desencolar() {
        if (isEmpty()) {
            System.out.println("[COLA] Cola vacia, no se puede desencolar");
            return null;
        }
        String msg = frente.nombreEstudiante + ": " + frente.mensaje;
        System.out.println("[COLA] DESENCOLADO: " + frente.nombreEstudiante + " | Tamano: " + (size - 1));
        frente = frente.siguiente;
        if (frente == null) fin = null;
        size--;
        return msg;
    }

    public boolean isEmpty() {
        return frente == null;
    }

    public int size() {
        return size;
    }

    public void mostrarCola() {
        System.out.println("\n=== COLA DE NOTIFICACIONES (FIFO) ===");
        Nodo actual = frente;
        int pos = 1;
        while (actual != null) {
            String marca = (actual == frente) ? " <FRENTE>" : "";
            System.out.println(pos + ". [P-" + actual.idPrestamo + "] " + actual.nombreEstudiante + marca);
            actual = actual.siguiente;
            pos++;
        }
        System.out.println("=====================================\n");
    }
}