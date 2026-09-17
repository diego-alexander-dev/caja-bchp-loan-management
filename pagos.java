package caja;
import java.time.LocalDate;

public class pagos {

    int id;
    String estudiante;
    int prestamoId;
    double monto;
    int cuotaNumero;
    String metodoPago;
    LocalDate fechaPago;
    String estado;

    public pagos(int id, String estudiante, int prestamoId,
                 double monto, int cuotaNumero, String metodoPago) {

        this.id = id;
        this.estudiante = estudiante;
        this.prestamoId = prestamoId;
        this.monto = monto;
        this.cuotaNumero = cuotaNumero;
        this.metodoPago = metodoPago;
        this.fechaPago = LocalDate.now();
        this.estado = "Completado";
    }
}



