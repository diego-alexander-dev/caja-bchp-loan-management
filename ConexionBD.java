package caja;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static final String URL = "jdbc:postgresql://localhost:5432/ALGORITMOS1";
    private static final String USUARIO = "postgres";
    private static final String CLAVE = "123456"; 

    public static Connection conectar() {
        Connection conexion = null;
        try {
            Class.forName("org.postgresql.Driver");
            conexion = DriverManager.getConnection(URL, USUARIO, CLAVE);
            System.out.println(" Conexión establecida con éxito con PostgreSQL");
        } catch (ClassNotFoundException e) {
            System.out.println(" Error: No se encontró el Driver JDBC en las Librerías.");
        } catch (SQLException e) {
            System.out.println(" Error de conexión: " + e.getMessage());
        }
        return conexion;
    }
}

