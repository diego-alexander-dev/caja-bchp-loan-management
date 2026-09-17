package caja;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.io.ByteArrayOutputStream;

// 🔥 LIBRERÍAS DE CODIFICACIÓN CRIPTOGRÁFICA QR (ZXing)
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class RegistroPrestamos extends JFrame {
    JTextField txtMonto, txtCuotas, txtPromedio, txtBuscar;
    JComboBox<String> comboEstudiante;
    JLabel lblMontoMax, lblPrioridad, lblId;
    DefaultTableModel modelo;

    Color colorPrimario = new Color(41, 128, 185); 
    Color colorSecundario = new Color(23, 33, 43);
    Color colorExito = new Color(46, 204, 113);
    Color colorFondo = new Color(17, 28, 36);

    // =========================================================================
    // ESTRUCTURAS DE DATOS AVANZADAS EN MEMORIA RAM
    // =========================================================================
    private ArbolPrioridad arbolEvaluacion = new ArbolPrioridad();
    private PilaTransacciones pilaCaja = new PilaTransacciones();

    // ----- ESTRUCTURA 1: ÁRBOL BINARIO DE PRIORIZACIÓN ACADÉMICA (ABB) -----
    static class NodoArbol {
        int idPrestamo;
        double promedio;
        String estudiante;
        NodoArbol izquierdo, derecho;

        public NodoArbol(int idPrestamo, double promedio, String estudiante) {
            this.idPrestamo = idPrestamo;
            this.promedio = promedio;
            this.estudiante = estudiante;
            this.izquierdo = null;
            this.derecho = null;
        }
    }

    static class ArbolPrioridad {
        private NodoArbol raiz = null;

        public void insertar(int id, double promedio, String estudiante) {
            raiz = insertarRec(raiz, id, promedio, estudiante);
        }

        private NodoArbol insertarRec(NodoArbol actual, int id, double promedio, String estudiante) {
            if (actual == null) return new NodoArbol(id, promedio, estudiante);
            if (promedio > actual.promedio) {
                actual.izquierdo = insertarRec(actual.izquierdo, id, promedio, estudiante);
            } else {
                actual.derecho = insertarRec(actual.derecho, id, promedio, estudiante);
            }
            return actual;
        }

        public void generarReporteInOrder(NodoArbol nodo, StringBuilder sb) {
            if (nodo != null) {
                generarReporteInOrder(nodo.izquierdo, sb);
                String prioridad = nodo.promedio > 15 ? "ALTA" : (nodo.promedio >= 11 ? "MEDIA" : "RECHAZADO");
                sb.append("   • [Nota: ").append(nodo.promedio).append("] Alumno: ").append(nodo.estudiante)
                  .append(" | Prioridad: ").append(prioridad).append("\n");
                generarReporteInOrder(nodo.derecho, sb);
            }
        }

        public NodoArbol getRaiz() { return raiz; }
    }

    // ----- ESTRUCTURA 2: PILA DE TRAZA TRANSACCIONAL DE CAJA (LIFO) -----
    static class NodoTransaccion {
        String logOperacion;
        NodoTransaccion abajo;

        public NodoTransaccion(String logOperacion) {
            this.logOperacion = logOperacion;
            this.abajo = null;
        }
    }

    static class PilaTransacciones {
        private NodoTransaccion tope = null;

        public void push(String operacion) {
            NodoTransaccion nuevo = new NodoTransaccion(operacion);
            nuevo.abajo = tope;
            tope = nuevo;
            System.out.println("LOG CAJA -> " + operacion);
        }

        public String pop() {
            if (isEmpty()) return null;
            String op = tope.logOperacion;
            tope = tope.abajo;
            return op;
        }

        public boolean isEmpty() { return tope == null; }
    }

    // ----- HERRAMIENTA AUXILIAR: GENERADOR MATEMÁTICO DE MATRIZ QR -----
    private byte[] generarBytesQR(String textoBoleta) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(textoBoleta, BarcodeFormat.QR_CODE, 256, 256);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            System.out.println("Error interno codificando QR: " + e.getMessage());
            return null;
        }
    }

    // =========================================================================
    // CONSTRUCTOR PRINCIPAL DE LA INTERFAZ
    // =========================================================================
    public RegistroPrestamos() {
        setTitle("BCHP - Gestión de Créditos Universitarios");
        setSize(1100, 720); // Ajuste leve de soltura para albergar nuevos comandos
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(colorFondo);

        JPanel pnlHead = new JPanel();
        pnlHead.setBackground(colorSecundario);
        pnlHead.setPreferredSize(new Dimension(1000, 60));
        JLabel tit = new JLabel("Evaluación y Concesión de Créditos Universitarios");
        tit.setForeground(Color.WHITE); 
        tit.setFont(new Font("Segoe UI", Font.BOLD, 20));
        pnlHead.add(tit);
        add(pnlHead, BorderLayout.NORTH);

        JPanel pnlCentral = new JPanel(new BorderLayout(10, 10));
        pnlCentral.setBackground(colorFondo);
        pnlCentral.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(colorSecundario);
        pnlForm.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(36, 47, 61)), "Evaluación de Crédito",
            0, 0, new Font("Segoe UI", Font.BOLD, 12), Color.WHITE));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10); gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; pnlForm.add(crearEtiqueta("ID Préstamo:"), gbc);
        gbc.gridx = 1; lblId = new JLabel("Cargando..."); 
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 14)); lblId.setForeground(Color.WHITE); pnlForm.add(lblId, gbc);

        gbc.gridx = 2; pnlForm.add(crearEtiqueta("Seleccione Estudiante:"), gbc);
        gbc.gridx = 3; comboEstudiante = new JComboBox<>(); pnlForm.add(comboEstudiante, gbc);

        gbc.gridx = 0; gbc.gridy = 1; pnlForm.add(crearEtiqueta("Promedio Notas:"), gbc);
        gbc.gridx = 1; txtPromedio = crearCampoTexto(); 
        txtPromedio.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) { evaluarNivelYPrioridad(); }
        });
        pnlForm.add(txtPromedio, gbc);

        gbc.gridx = 2; pnlForm.add(crearEtiqueta("Prioridad de Filtro:"), gbc);
        gbc.gridx = 3; lblPrioridad = new JLabel("Ingrese Promedio");
        lblPrioridad.setFont(new Font("Segoe UI", Font.BOLD, 13)); lblPrioridad.setForeground(Color.YELLOW); pnlForm.add(lblPrioridad, gbc);

        gbc.gridx = 0; gbc.gridy = 2; pnlForm.add(crearEtiqueta("Monto Máx Autorizado:"), gbc);
        gbc.gridx = 1; lblMontoMax = new JLabel("S/. 0.00");
        lblMontoMax.setFont(new Font("Segoe UI", Font.BOLD, 14)); lblMontoMax.setForeground(new Color(0, 168, 204)); pnlForm.add(lblMontoMax, gbc);

        gbc.gridx = 2; pnlForm.add(crearEtiqueta("Monto a Solicitar (S/.):"), gbc);
        gbc.gridx = 3; txtMonto = crearCampoTexto(); pnlForm.add(txtMonto, gbc);

        gbc.gridx = 2; gbc.gridy = 3; pnlForm.add(crearEtiqueta("N° Cuotas (1-24):"), gbc);
        gbc.gridx = 3; txtCuotas = crearCampoTexto(); pnlForm.add(txtCuotas, gbc);

        pnlCentral.add(pnlForm, BorderLayout.NORTH);

        JPanel pnlContenedorTabla = new JPanel(new BorderLayout(5, 5));
        pnlContenedorTabla.setBackground(colorFondo);

        JPanel pnlBusq = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlBusq.setBackground(colorFondo);
        pnlBusq.add(crearEtiqueta("🔍 Buscar Alumno:"));
        txtBuscar = crearCampoTexto(); txtBuscar.setPreferredSize(new Dimension(200, 30)); pnlBusq.add(txtBuscar);
        JButton btnBus = crearBoton("Filtrar Servidor", colorPrimario);
        btnBus.addActionListener(e -> buscarPrestamosPorFiltro());
        pnlBusq.add(btnBus);

        modelo = new DefaultTableModel();
        modelo.setColumnIdentifiers(new Object[]{"ID Crédito", "DNI Alumno", "Estudiante", "Monto Base", "Cuotas", "Saldo Restante", "Estado"});
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

        // BOTONERA ACTUALIZADA CON COMANDOS PARA LAS ESTRUCTURAS DE MEMORIA
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        pnlBotones.setBackground(colorFondo);
        
        JButton btnAdd = crearBoton("Aprobar y Desembolsar Crédito", colorExito);
        btnAdd.addActionListener(e -> procesarInsercionPrestamo());
        
        JButton btnRevertir = crearBoton("Revertir Último Cambio (Pila)", new Color(231, 76, 60));
        btnRevertir.addActionListener(e -> deshacerUltimaOperacionCaja());
        
        JButton btnAuditarArbol = new JButton("Ver Árbol Académico");
        btnAuditarArbol.setBackground(new Color(155, 89, 182));
        btnAuditarArbol.setForeground(Color.WHITE);
        btnAuditarArbol.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAuditarArbol.setPreferredSize(new Dimension(190, 35));
        btnAuditarArbol.setBorderPainted(false);
        btnAuditarArbol.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAuditarArbol.addActionListener(e -> mostrarAuditoriaArbolAsistente());

        JButton btnVol = crearBoton("Volver al Menú", new Color(127, 140, 141));
        btnVol.addActionListener(e -> this.dispose());

        pnlBotones.add(btnAdd); 
        pnlBotones.add(btnRevertir);
        pnlBotones.add(btnAuditarArbol);
        pnlBotones.add(btnVol);
        add(pnlBotones, BorderLayout.SOUTH);

        cargarAlumnosEnCombo();
        cargarHistorialPrestamosGlobal();
        
        setVisible(true); 
    }

    private void cargarAlumnosEnCombo() {
        comboEstudiante.removeAllItems();
        String sql = "SELECT dni, nombre, apellido FROM estudiantes ORDER BY apellido ASC";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                comboEstudiante.addItem(rs.getString("dni") + " - " + rs.getString("apellido") + ", " + rs.getString("nombre"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error cargando lista de alumnos: " + e.getMessage());
        }
    }

    private void cargarHistorialPrestamosGlobal() {
        modelo.setRowCount(0);
        arbolEvaluacion = new ArbolPrioridad(); // Reseteo de indexación local
        
        String sqlId = "SELECT COALESCE(MAX(id_prestamo), 0) + 1 AS siguiente FROM prestamos";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement psId = con.prepareStatement(sqlId);
             ResultSet rsId = psId.executeQuery()) {
            if (rsId.next()) {
                lblId.setText(String.valueOf(rsId.getInt("siguiente")));
            }
        } catch (SQLException e) {
            System.out.println("Error al calcular ID: " + e.getMessage());
        }

        String sqlData = "SELECT p.id_prestamo, e.dni, e.nombre, e.apellido, p.monto, p.cuotas, p.saldo, p.estado, e.promedio_notas " +
                         "FROM prestamos p INNER JOIN estudiantes e ON p.id_estudiante = e.id_estudiante ORDER BY p.id_prestamo DESC";
        
        try (Connection con = ConexionBD.conectar();
             PreparedStatement psData = con.prepareStatement(sqlData);
             ResultSet rs = psData.executeQuery()) {
            
            while (rs.next()) {
                int idPre = rs.getInt("id_prestamo");
                String nomCompleto = rs.getString("apellido") + " " + rs.getString("nombre");
                double promedioNotas = rs.getDouble("promedio_notas");

                // INYECCIÓN EN EL ÁRBOL BINARIO DE BUSQUEDA EN MEMORIA
                arbolEvaluacion.insertar(idPre, promedioNotas, nomCompleto);

                modelo.addRow(new Object[]{
                    "P-" + String.format("%03d", idPre),
                    rs.getString("dni"),
                    nomCompleto,
                    "S/. " + rs.getDouble("monto"),
                    rs.getInt("cuotas"),
                    "S/. " + String.format("%.2f", rs.getDouble("saldo")),
                    rs.getString("estado")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error listando créditos: " + e.getMessage());
        }
    }

    private void evaluarNivelYPrioridad() {
        try {
            if (txtPromedio.getText().trim().isEmpty()) {
                lblPrioridad.setText("Ingrese Promedio"); lblMontoMax.setText("S/. 0.00"); return;
            }
            double p = Double.parseDouble(txtPromedio.getText());
            if (p < 0 || p > 20) { 
                lblPrioridad.setText("Nota Inválida"); lblMontoMax.setText("S/. 0.00"); return; 
            }
            if (p < 11) { 
                lblPrioridad.setText("RECHAZADO"); lblPrioridad.setForeground(Color.RED); lblMontoMax.setText("S/. 0.00"); 
            } else if (p <= 15) { 
                lblPrioridad.setText("PRIORIDAD MEDIA"); lblPrioridad.setForeground(new Color(230, 126, 34)); lblMontoMax.setText("S/. 2000.00"); 
            } else { 
                lblPrioridad.setText("PRIORIDAD ALTA"); lblPrioridad.setForeground(colorExito); lblMontoMax.setText("S/. 5000.00"); 
            }
        } catch (Exception e) { 
            lblPrioridad.setText("Ingrese Promedio"); lblMontoMax.setText("S/. 0.00"); 
        }
    }

    // 🔥 LOGICA CRÍTICA: PROCESAMIENTO, ESCRITURA EN POSTGRESQL Y CREACIÓN REAL DEL QR ES_CANEABLE
    private void procesarInsercionPrestamo() {
        String seleccion = (String) comboEstudiante.getSelectedItem();
        if (seleccion == null) return;

        String dniEstudiante = seleccion.split(" - ")[0]; 
        int idEstudianteReal = -1;

        try {
            double prom = Double.parseDouble(txtPromedio.getText());
            double monto = Double.parseDouble(txtMonto.getText());
            int cuotas = Integer.parseInt(txtCuotas.getText());

            if (prom < 11) { JOptionPane.showMessageDialog(this, "Estudiante no apto por promedio."); return; }
            double max = (prom <= 15) ? 2000 : 5000;

            if (monto > max) { JOptionPane.showMessageDialog(this, "El monto supera el límite (S/. " + max + ")"); return; }
            if (cuotas < 1 || cuotas > 24) { JOptionPane.showMessageDialog(this, "Cuotas de 1 a 24."); return; }

            double saldoCalculado = monto + (monto * 10.00 / 100);

            try (Connection con = ConexionBD.conectar()) {
                String queryId = "SELECT id_estudiante FROM estudiantes WHERE dni = ?";
                try (PreparedStatement ps = con.prepareStatement(queryId)) {
                    ps.setString(1, dniEstudiante);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) idEstudianteReal = rs.getInt("id_estudiante");
                }

                if (idEstudianteReal != -1) {
                    // 1. Armamos la boleta estructurada con saltos de línea (\n)
                    StringBuilder boleta = new StringBuilder();
                    boleta.append("=========================================\n");
                    boleta.append("      CAJA BCHP - BANCA UNIVERSITARIA     \n");
                    boleta.append("=========================================\n");
                    boleta.append("COMPROBANTE ELECTRONICO DE DESEMBOLSO\n");
                    boleta.append("-----------------------------------------\n");
                    boleta.append("DNI ALUMNO : ").append(dniEstudiante).append("\n");
                    boleta.append("ESTUDIANTE : ").append(seleccion.split(" - ")[1]).append("\n");
                    boleta.append("-----------------------------------------\n");
                    boleta.append("MONTO BASE : S/. ").append(monto).append("\n");
                    boleta.append("INTERES(10%): S/. ").append(monto * 0.10).append("\n");
                    boleta.append("SALDO NETO : S/. ").append(saldoCalculado).append("\n");
                    boleta.append("N° CUOTAS  : ").append(cuotas).append(" Meses\n");
                    boleta.append("ESTADO     : CREDITO VIGENTE / ACTIVO\n");
                    boleta.append("=========================================\n");
                    boleta.append("¡Verificado por el Sistema Criptográfico UTP!");

                    // 2. Transmutación matemática a arreglo de bytes PNG real
                    byte[] bytesQR = generarBytesQR(boleta.toString());

                    // 3. Sentencia preparada con soporte para la columna binaria BYTEA
                    String sqlInsert = "INSERT INTO prestamos (id_estudiante, monto, cuotas, tasa_interes, saldo, estado, fecha_inicio, codigo_qr) VALUES (?, ?, ?, 10.00, ?, 'Activo', ?, ?)";
                    try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                        ps.setInt(1, idEstudianteReal);
                        ps.setDouble(2, monto);
                        ps.setInt(3, cuotas);
                        ps.setDouble(4, saldoCalculado);
                        ps.setDate(5, java.sql.Date.valueOf(LocalDate.now()));
                        
                        if (bytesQR != null) {
                            ps.setBytes(6, bytesQR);
                        } else {
                            ps.setNull(6, java.sql.Types.BINARY);
                        }
                        
                        ps.executeUpdate();

                        // 4. INYECCIÓN EN LA PILA TRANSACCIONAL (LIFO)
                        pilaCaja.push("Desembolso aprobado -> Alumno: " + dniEstudiante + " | Saldo Neto: S/. " + saldoCalculado);

                        JOptionPane.showMessageDialog(this, "Préstamo registrado con éxito y Código QR Real adjuntado.");
                        
                        txtMonto.setText(""); txtCuotas.setText(""); txtPromedio.setText("");
                        lblMontoMax.setText("S/. 0.00"); lblPrioridad.setText("Ingrese Promedio");
                        lblPrioridad.setForeground(Color.YELLOW);
                        
                        cargarHistorialPrestamosGlobal(); 
                    }
                }
            }
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(this, "Verifique los valores numéricos ingresados o dependencias .jar."); 
        }
    }

    private void deshacerUltimaOperacionCaja() {
        if (pilaCaja.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No registras transacciones de crédito reversibles en la sesión activa.", "Auditoría de Caja", JOptionPane.INFORMATION_MESSAGE);
        } else {
            String logRevertido = pilaCaja.pop();
            JOptionPane.showMessageDialog(this, "Transacción de Ventanilla Anulada de Memoria Caché:\n\n" + logRevertido, "Reversión LIFO exitosa", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void mostrarAuditoriaArbolAsistente() {
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("   BCHP ASISTENTE - REPORTE DE PRIORIZACIÓN ACADÉMICA\n");
        sb.append("   Estructura: Árbol Binario de Búsqueda (Memoria RAM)\n");
        sb.append("====================================================\n\n");

        if (arbolEvaluacion.getRaiz() == null) {
            sb.append("No se registran solicitudes indexadas en las ramas del árbol de forma activa.");
        } else {
            arbolEvaluacion.generarReporteInOrder(arbolEvaluacion.getRaiz(), sb);
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        textArea.setBackground(new Color(15, 23, 31));
        textArea.setForeground(new Color(155, 89, 182));
        textArea.setEditable(false);
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(550, 350));

        JOptionPane.showMessageDialog(this, scroll, "Estructura No Lineal (ABB) - Módulo Asistente", JOptionPane.PLAIN_MESSAGE);
    }

    private void buscarPrestamosPorFiltro() {
        String query = txtBuscar.getText().trim().toUpperCase();
        modelo.setRowCount(0);
        
        String sql = "SELECT p.id_prestamo, e.dni, e.nombre, e.apellido, p.monto, p.cuotas, p.saldo, p.estado " +
                     "FROM prestamos p INNER JOIN estudiantes e ON p.id_estudiante = e.id_estudiante " +
                     "WHERE e.nombre LIKE ? OR e.apellido LIKE ? ORDER BY p.id_prestamo DESC";
        
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                modelo.addRow(new Object[]{
                    "P-" + String.format("%03d", rs.getInt("id_prestamo")),
                    rs.getString("dni"),
                    rs.getString("apellido") + " " + rs.getString("nombre"),
                    "S/. " + rs.getDouble("monto"),
                    rs.getInt("cuotas"),
                    "S/. " + String.format("%.2f", rs.getDouble("saldo")),
                    rs.getString("estado")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error filtro: " + e.getMessage());
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
        b.setPreferredSize(new Dimension(210, 35));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
}
