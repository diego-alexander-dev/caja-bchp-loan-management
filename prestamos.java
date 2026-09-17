package caja;
import java.time.LocalDate;

class prestamos {
    int id;
    String estudiante;
    double monto;
    int cuotas;
    double tasaInteres;
    double saldo;
    LocalDate fechaInicio;
    String estado;

    public prestamos(int id, String estudiante, double monto, int cuotas, double tasaInteres, LocalDate fechaInicio) {
        this.id = id;
        this.estudiante = estudiante;
        this.monto = monto;
        this.cuotas = cuotas;
        this.tasaInteres = tasaInteres;
        this.fechaInicio = fechaInicio;
        this.estado = "Activo";
        this.saldo = getMontoTotal(); // 🔥 clave
    }

    public double getMontoTotal() {
        return monto + (monto * tasaInteres / 100);
    }

    public double getCuotaMensual() {
        return getMontoTotal() / cuotas;
    }

    @Override
    public String toString() {
        return "P-" + String.format("%03d", id) + " - " + estudiante +
                " - Saldo: S/. " + String.format("%.2f", saldo);
    }
}
