package caja;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;

public class RegistroPagos extends JFrame {
    JTextField txtMontoPago, txtCuotaNum, txtBuscar;
    JComboBox<String> comboPrestamo;
    JComboBox<String> comboMetodo;
    DefaultTableModel modelo;

    Color colorPrimario = new Color(41, 128, 185); 
    Color colorSecundario = new Color(23, 33, 43);
    Color colorExito = new Color(46, 204, 113);
    Color colorFondo = new Color(17, 28, 36);

    public RegistroPagos() {
        setTitle("BCHP - Procesar Amortizaciones y Pagos");
        setSize(1000, 700);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(colorFondo);

        // Cabecera Estándar Nativa Dark
        JPanel pnlHead = new JPanel();
        pnlHead.setBackground(colorSecundario);
        pnlHead.setPreferredSize(new Dimension(1000, 60));
        JLabel tit = new JLabel("Registro y Control de Amortizaciones (Pagos)");
        tit.setForeground(Color.WHITE); 
        tit.setFont(new Font("Segoe UI", Font.BOLD, 20));
        pnlHead.add(tit);
        add(pnlHead, BorderLayout.NORTH);

        // Panel Central Dark
        JPanel pnlCentral = new JPanel(new BorderLayout(10, 10));
        pnlCentral.setBackground(colorFondo);
        pnlCentral.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Formulario de Parámetros
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(colorSecundario);
        pnlForm.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(36, 47, 61)), "Detalle de la Transacción",
            0, 0, new Font("Segoe UI", Font.BOLD, 12), Color.WHITE));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10); gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; pnlForm.add(crearEtiqueta("Seleccione Crédito:"), gbc);
        gbc.gridx = 1; comboPrestamo = new JComboBox<>(); pnlForm.add(comboPrestamo, gbc);

        gbc.gridx = 2; pnlForm.add(crearEtiqueta("Método de Pago:"), gbc);
        gbc.gridx = 3; comboMetodo = new JComboBox<>(new String[]{"YAPE", "PLIN", "TRANSFERENCIA BCP", "PAGOEFECTIVO"}); 
        pnlForm.add(comboMetodo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; pnlForm.add(crearEtiqueta("Monto a Amortizar (S/.):"), gbc);
        gbc.gridx = 1; txtMontoPago = crearCampoTexto(); pnlForm.add(txtMontoPago, gbc);

        gbc.gridx = 2; pnlForm.add(crearEtiqueta("Número de Cuota:"), gbc);
        gbc.gridx = 3; txtCuotaNum = crearCampoTexto(); pnlForm.add(txtCuotaNum, gbc);

        pnlCentral.add(pnlForm, BorderLayout.NORTH);

        // Tabla de Historial de Pagos con Buscador
        JPanel pnlContenedorTabla = new JPanel(new BorderLayout(5, 5));
        pnlContenedorTabla.setBackground(colorFondo);

        JPanel pnlBusq = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlBusq.setBackground(colorFondo);
        pnlBusq.add(crearEtiqueta("🔍 Buscar por Código de Pago:"));
        txtBuscar = crearCampoTexto(); txtBuscar.setPreferredSize(new Dimension(200, 30)); pnlBusq.add(txtBuscar);
        JButton btnBus = crearBoton("Filtrar", colorPrimario);
        pnlBusq.add(btnBus);

        modelo = new DefaultTableModel();
        modelo.setColumnIdentifiers(new Object[]{"ID Pago", "Ref. Crédito", "DNI Alumno", "Monto Abonado", "Cuota N°", "Método", "Fecha"});
        JTable tabla = new JTable(modelo);
        tabla.setBackground(colorSecundario);
        tabla.setForeground(Color.WHITE);
        tabla.setGridColor(new Color(36, 47, 61));
        tabla.setRowHeight(25);

        JTableHeader header = tabla.getTableHeader();
        header.setBackground(new Color(36, 47, 61));
        header.setForeground(Color.WHITE);

        pnlContenedorTabla.add(pnlBusq, BorderLayout.NORTH);
        pnlContenedorTabla.add(new JScrollPane(tabla), BorderLayout.CENTER);
        pnlCentral.add(pnlContenedorTabla, BorderLayout.CENTER);
        add(pnlCentral, BorderLayout.CENTER);

        // Botones de Comando Inferiores
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        pnlBotones.setBackground(colorFondo);
        JButton btnAdd = crearBoton("Procesar y Confirmar Pago", colorExito);
        btnAdd.addActionListener(e -> procesarInsercionPago());
        JButton btnVol = crearBoton("Volver al Menú", new Color(127, 140, 141));
        btnVol.addActionListener(e -> this.dispose());

        pnlBotones.add(btnAdd); pnlBotones.add(btnVol);
        add(pnlBotones, BorderLayout.SOUTH);

        cargarPrestamosEnCombo();
        cargarHistorialPagosGlobal();
        
        setVisible(true);
    }

    private void cargarPrestamosEnCombo() {
        comboPrestamo.removeAllItems();
        String sql = "SELECT p.id_prestamo, e.apellido, p.saldo FROM prestamos p " +
                     "INNER JOIN estudiantes e ON p.id_estudiante = e.id_estudiante WHERE p.saldo > 0 ORDER BY p.id_prestamo ASC";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                comboPrestamo.addItem("CR-" + String.format("%03d", rs.getInt("id_prestamo")) + " (" + rs.getString("apellido") + ") - Saldo: S/." + rs.getDouble("saldo"));
            }
        } catch (SQLException e) {
            System.out.println("Error combo pagos: " + e.getMessage());
        }
    }

    private void cargarHistorialPagosGlobal() {
        modelo.setRowCount(0);
        String sql = "SELECT pa.id_pago, pa.id_prestamo, e.dni, pa.monto, pa.cuota_numero, pa.metodo_pago, pa.fecha_pago " +
                     "FROM pagos pa INNER JOIN prestamos pr ON pa.id_prestamo = pr.id_prestamo " +
                     "INNER JOIN estudiantes e ON pr.id_estudiante = e.id_estudiante ORDER BY pa.id_pago DESC";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modelo.addRow(new Object[]{
                    "PAG-" + String.format("%04d", rs.getInt("id_pago")),
                    "CR-" + String.format("%03d", rs.getInt("id_prestamo")),
                    rs.getString("dni"),
                    "S/. " + rs.getDouble("monto"),
                    rs.getInt("cuota_numero"),
                    rs.getString("metodo_pago"),
                    rs.getDate("fecha_pago")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error listando pagos: " + e.getMessage());
        }
    }

    private void procesarInsercionPago() {
        String seleccion = (String) comboPrestamo.getSelectedItem();
        if (seleccion == null) { JOptionPane.showMessageDialog(this, "No hay créditos activos para cobrar."); return; }

        // Extrae el ID numérico del formato "CR-001" -> 1
        int idPrestamoReal = Integer.parseInt(seleccion.split(" ")[0].replace("CR-", ""));

        try {
            double montoAbono = Double.parseDouble(txtMontoPago.getText().trim());
            int cuota = Integer.parseInt(txtCuotaNum.getText().trim());
            String metodo = (String) comboMetodo.getSelectedItem();

            if (montoAbono <= 0) { JOptionPane.showMessageDialog(this, "El monto debe ser mayor a cero."); return; }

            try (Connection con = ConexionBD.conectar()) {
                con.setAutoCommit(false); // Transacción segura

                // 1. Insertar el registro de pago
                String sqlInsert = "INSERT INTO pagos (id_prestamo, monto, cuota_numero, metodo_pago) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                    ps.setInt(1, idPrestamoReal);
                    ps.setDouble(2, montoAbono);
                    ps.setInt(3, cuota);
                    ps.setString(4, metodo);
                    ps.executeUpdate();
                }

                // 2. Descontar del saldo del préstamo en PostgreSQL
                String sqlUpdate = "UPDATE prestamos SET saldo = GREATEST(saldo - ?, 0), estado = CASE WHEN (saldo - ?) <= 0 THEN 'Pagado' ELSE 'Activo' END WHERE id_prestamo = ?";
                try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
                    ps.setDouble(1, montoAbono);
                    ps.setDouble(2, montoAbono);
                    ps.setInt(3, idPrestamoReal);
                    ps.executeUpdate();
                }

                con.commit(); // Consolidar cambios
                JOptionPane.showMessageDialog(this, "Amortización procesada y saldo actualizado.");
                
                txtMontoPago.setText(""); txtCuotaNum.setText("");
                cargarPrestamosEnCombo();
                cargarHistorialPagosGlobal();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error de validación en la transacción.");
        }
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel jl = new JLabel(texto);
        jl.setForeground(Color.WHITE);
        jl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return jl;
    }

    private JTextField crearCampoTexto() {
        JTextField jt = new JTextField();
        jt.setBackground(colorFondo);
        jt.setForeground(Color.WHITE);
        jt.setCaretColor(Color.WHITE);
        jt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(36, 47, 61)),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        return jt;
    }

    private JButton crearBoton(String t, Color c) {
        JButton b = new JButton(t);
        b.setBackground(c); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setOpaque(true); b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(190, 35));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
}

