package caja;

public class Estudiante {
    int id;
    String nombre;
    String apellido;
    String dni;
    String telefono;
    String correo;
    int edad;
    
    // --- NUEVAS COLUMNAS CONECTADAS A LA BD ---
    double promedioNotas;
    double porcentajeAsistencia;
    String estadoUniversidad; // "ACTIVO", "RETIRADO", "ALERTA"

    public Estudiante(int id, String nombre, String apellido, int edad, String dni, String telefono, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.dni = dni;
        this.telefono = telefono;
        this.correo = correo;
        
        // Atributos iniciales por defecto
        this.promedioNotas = 11.0;
        this.porcentajeAsistencia = 100.0;
        this.estadoUniversidad = "ACTIVO";
    }
}


