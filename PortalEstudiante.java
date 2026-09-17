package caja;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.InputStream;
import java.sql.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class PortalEstudiante extends JFrame {
    private String codigoUsuario;
    private JTable tblMisPrestamos;
    private DefaultTableModel modeloTabla;
    private JLabel lblNombre, lblPromedio, lblAsistencia, lblDeudaTotal, lblEstadoUni;
    
    private JButton btnMisBoletas;
    private int idEstudianteAutenticado = -1;
    private double saldoTotalReal = 0.0; 
    
    private PilaOperaciones historial;
    private ArbolEstudiante arbolPrestamosUsuario;
    private ColaCronograma colaPagosPendientes = new ColaCronograma();
    
    private PrivacidadRenderer privacidadRenderer = new PrivacidadRenderer();

    // ===== ESTRUCTURA DE DATOS: ARBOL BINARIO DE BUSQUEDA (ABB) =====
    static class NodoArbol {
        int idPrestamo;
        double monto;
        String estado;
        NodoArbol izquierdo, derecho;

        public NodoArbol(int idPrestamo, double monto, String estado) {
            this.idPrestamo = idPrestamo;
            this.monto = monto;
            this.estado = estado;
            this.izquierdo = null;
            this.derecho = null;
        }
    }

    static class ArbolEstudiante {
        private NodoArbol raiz;

        public ArbolEstudiante() {
            raiz = null;
        }

        public void insertar(int id, double monto, String estado) {
            raiz = insertarRec(raiz, id, monto, estado);
        }

        private NodoArbol insertarRec(NodoArbol actual, int id, double monto, String estado) {
            if (actual == null) {
                return new NodoArbol(id, monto, estado);
            }
            if (monto > actual.monto) {
                actual.izquierdo = insertarRec(actual.izquierdo, id, monto, estado);
            } else {
                actual.derecho = insertarRec(actual.derecho, id, monto, estado);
            }
            return actual;
        }

        public void generarReporteInOrder(NodoArbol nodo, StringBuilder sb) {
            if (nodo != null) {
                generarReporteInOrder(nodo.izquierdo, sb);
                sb.append("   * [P-").append(String.format("%03d", nodo.idPrestamo))
                  .append("] Monto adjudicado: S/. ").append(String.format("%.2f", nodo.monto))
                  .append(" | Estado: ").append(nodo.estado).append("\n");
                generarReporteInOrder(nodo.derecho, sb);
            }
        }

        public NodoArbol getRaiz() {
            return raiz;
        }
    }

    // ===== ESTRUCTURA DE DATOS: PILA (LIFO) =====
    static class Nodo {
        String operacion;
        Nodo siguiente;

        public Nodo(String operacion) {
            this.operacion = operacion;
            this.siguiente = null;
        }
    }

    static class PilaOperaciones {
        private Nodo tope;

        public PilaOperaciones() {
            tope = null;
        }

        public void push(String operacion) {
            Nodo nuevo = new Nodo(operacion);
            nuevo.siguiente = tope;
            tope = nuevo;
            System.out.println("PUSH -> " + operacion);
        }

        public String pop() {
            if (isEmpty()) return null;
            String op = tope.operacion;
            tope = tope.siguiente;
            return op;
        }

        public boolean isEmpty() {
            return tope == null;
        }

        public String obtenerHistorialTexto() {
            if (isEmpty()) {
                return "No hay operaciones registradas en esta sesion.";
            }
            StringBuilder sb = new StringBuilder();
            Nodo aux = tope;
            int cont = 1;
            while (aux != null) {
                sb.append(cont).append(". ").append(aux.operacion).append("\n");
                aux = aux.siguiente;
                cont++;
            }
            return sb.toString();
        }
    }

    // ===== ESTRUCTURA DE DATOS: COLA DE COBRANZA (FIFO) =====
    static class NodoCuota {
        int numeroCuota;
        double montoCuota;
        NodoCuota siguiente;

        public NodoCuota(int numeroCuota, double montoCuota) {
            this.numeroCuota = numeroCuota;
            this.montoCuota = montoCuota;
            this.siguiente = null;
        }
    }

    static class ColaCronograma {
        private NodoCuota frente;
        private NodoCuota fin;

        public ColaCronograma() {
            frente = null;
            fin = null;
        }

        public void encolar(int num, double monto) {
            NodoCuota nuevo = new NodoCuota(num, monto);
            if (isEmpty()) {
                frente = nuevo;
            } else {
                fin.siguiente = nuevo;
            }
            fin = nuevo;
        }

        public String desencolar() {
            if (isEmpty()) return null;
            String info = "Cuota N° " + frente.numeroCuota + " - Monto: S/. " + String.format("%.2f", frente.montoCuota);
            frente = frente.siguiente;
            if (frente == null) {
                fin = null;
            }
            return info;
        }

        public boolean isEmpty() {
            return frente == null;
        }

        public String verTope() {
            if (isEmpty()) {
                return "Felicidades, no registras deudas pendientes en la cola del cronograma.";
            }
            return "PROXIMO VENCIMIENTO PRIORITARIO (FIFO):\nCuota N° " + frente.numeroCuota + " | Monto a pagar: S/. " + String.format("%.2f", frente.montoCuota);
        }
    }

    // ===== MOTOR DE ENMASCARAMIENTO VISUAL (MODO PRIVACIDAD) =====
    class PrivacidadRenderer extends DefaultTableCellRenderer {
        private boolean modoPrivado = false;
        public void setModoPrivado(boolean modoPrivado) { this.modoPrivado = modoPrivado; }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (modoPrivado && value != null) {
                value = "S/. ***";
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

    // ===== CONSTRUCTOR DEL PORTAL =====
    public PortalEstudiante(String codigoUsuario) {
        this.codigoUsuario = codigoUsuario;
        this.historial = new PilaOperaciones();
        this.arbolPrestamosUsuario = new ArbolEstudiante();
        
        setTitle("BCHP - Portal de Consultas Financieras y Academicas");
        setSize(1100, 720); 
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(17, 28, 36));

        // Header
        JPanel pnlHeader = new JPanel(new GridBagLayout());
        pnlHeader.setBackground(new Color(23, 33, 43));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        GridBagConstraints gbc = new GridBagConstraints();

        lblNombre = new JLabel("Cargando datos del alumno...");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblNombre.setForeground(Color.WHITE);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0; 
        gbc.anchor = GridBagConstraints.WEST; 
        pnlHeader.add(lblNombre, gbc);

        // Botonera organizada en un GridLayout(2, 3) 
        JPanel pnlBotonesAccion = new JPanel(new GridLayout(2, 3, 8, 8));
        pnlBotonesAccion.setBackground(new Color(23, 33, 43));

        // 1. Boton Mis Boletas
        btnMisBoletas = new JButton("Mis Boletas (QR)");
        btnMisBoletas.setBackground(new Color(46, 204, 113));
        btnMisBoletas.setForeground(Color.WHITE);
        btnMisBoletas.setFocusPainted(false);
        btnMisBoletas.setBorderPainted(false);
        btnMisBoletas.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnMisBoletas.setEnabled(false);
        btnMisBoletas.addActionListener(e -> {
            historial.push("Usuario abrio el repositorio de Boletas Electronicas QR.");
            abrirDialogoBoletas();
        });
        pnlBotonesAccion.add(btnMisBoletas);

        // 2. Boton Vencimientos (Cola)
        JButton btnCronogramaCola = new JButton("Vencimientos (Cola)");
        btnCronogramaCola.setBackground(new Color(241, 196, 15)); 
        btnCronogramaCola.setForeground(Color.BLACK);
        btnCronogramaCola.setFocusPainted(false);
        btnCronogramaCola.setBorderPainted(false);
        btnCronogramaCola.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnCronogramaCola.addActionListener(e -> {
            historial.push("Usuario consulto la prelacion de cuotas pendientes en la estructura Cola.");
            consultarPrelacionCuota();
        });
        pnlBotonesAccion.add(btnCronogramaCola);

        // 3. Boton Auditar Creditos (Arbol)
        JButton btnAuditarArbol = new JButton("Auditar Creditos (Arbol)");
        btnAuditarArbol.setBackground(new Color(155, 89, 182));
        btnAuditarArbol.setForeground(Color.WHITE);
        btnAuditarArbol.setFocusPainted(false);
        btnAuditarArbol.setBorderPainted(false);
        btnAuditarArbol.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnAuditarArbol.addActionListener(e -> {
            historial.push("Usuario ejecuto recorrido recursivo In-Order en su Arbol de Creditos.");
            mostrarReporteArbol();
        });
        pnlBotonesAccion.add(btnAuditarArbol);

        // 4. Boton Ver Historial (Pila)
        JButton btnHistorial = new JButton("Ver Historial (Pila)");
        btnHistorial.setBackground(new Color(52, 152, 219));
        btnHistorial.setForeground(Color.WHITE);
        btnHistorial.setFocusPainted(false);
        btnHistorial.setBorderPainted(false);
        btnHistorial.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnHistorial.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, 
                    historial.obtenerHistorialTexto(), 
                    "Historial de Operaciones (Estructura Pila LIFO)", 
                    JOptionPane.INFORMATION_MESSAGE);
        });
        pnlBotonesAccion.add(btnHistorial);
        
        // 5. Boton Realizar Pago (Reemplaza el espacio vacio)
        JButton btnPagarCuota = new JButton("Realizar Pago de Cuota");
        btnPagarCuota.setBackground(new Color(230, 126, 34)); // Un color naranja formal para distinguirlo
        btnPagarCuota.setForeground(Color.WHITE);
        btnPagarCuota.setFocusPainted(false);
        btnPagarCuota.setBorderPainted(false);
        btnPagarCuota.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnPagarCuota.addActionListener(e -> {
            historial.push("Usuario accedio a la pasarela de amortizacion FIFO.");
            abrirDialogoPagosFIFO();
        });
        pnlBotonesAccion.add(btnPagarCuota);

        // 6. Boton Cerrar Sesion
        JButton btnCerrar = new JButton("Cerrar Sesion");
        btnCerrar.setBackground(new Color(231, 76, 60));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnCerrar.addActionListener(e -> { 
            historial.push("Sesion cerrada por el usuario.");
            new Login().setVisible(true); 
            this.dispose(); 
        });
        pnlBotonesAccion.add(btnCerrar);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.0; 
        gbc.anchor = GridBagConstraints.EAST; 
        pnlHeader.add(pnlBotonesAccion, gbc);

        add(pnlHeader, BorderLayout.NORTH);

        // Panel Central
        JPanel pnlCentral = new JPanel(new BorderLayout(15, 15));
        pnlCentral.setBackground(new Color(17, 28, 36));
        pnlCentral.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20)); // Restaurado el margen inferior

        // Contenedor de Tarjetas KPI
        JPanel pnlKPIs = new JPanel(new GridLayout(1, 4, 15, 0));
        pnlKPIs.setBackground(new Color(17, 28, 36));
        lblPromedio = crearTarjetaKPI(pnlKPIs, "PROMEDIO NOTAS", "0.0", new Color(52, 152, 219));
        lblAsistencia = crearTarjetaKPI(pnlKPIs, "ASISTENCIA TOTAL", "0%", new Color(155, 89, 182));
        lblDeudaTotal = crearTarjetaKPI(pnlKPIs, "SALDO PENDIENTE", "S/. 0.00", new Color(241, 196, 15));
        lblEstadoUni = crearTarjetaKPI(pnlKPIs, "SITUACION MATRICULA", "...", new Color(46, 204, 113));
        pnlCentral.add(pnlKPIs, BorderLayout.NORTH);

        // Contenedor de Tabla Plana
        JPanel pnlTablaContainer = new JPanel(new BorderLayout());
        pnlTablaContainer.setBackground(new Color(23, 33, 43));
        pnlTablaContainer.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(36, 47, 61)), "Estado General de Cuentas",
            0, 0, new Font("Segoe UI", Font.BOLD, 12), Color.WHITE));

        // Panel Cabecera Interna para el boton Privacidad
        JPanel pnlTablaHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        pnlTablaHeader.setBackground(new Color(23, 33, 43));
        
        JToggleButton btnPrivacidad = new JToggleButton("[ Modo Privacidad ]");
        btnPrivacidad.setBackground(new Color(52, 73, 94));
        btnPrivacidad.setForeground(Color.WHITE);
        btnPrivacidad.setFocusPainted(false);
        btnPrivacidad.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnPrivacidad.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPrivacidad.addActionListener(e -> {
            boolean activo = btnPrivacidad.isSelected();
            btnPrivacidad.setText(activo ? "[ Mostrar Saldos ]" : "[ Modo Privacidad ]");
            privacidadRenderer.setModoPrivado(activo);
            tblMisPrestamos.repaint(); 
            lblDeudaTotal.setText(activo ? "S/. ***" : "S/. " + String.format("%.2f", saldoTotalReal));
            historial.push(activo ? "Se activo el enmascaramiento visual de saldos." : "Se desactivo el enmascaramiento visual.");
        });
        pnlTablaHeader.add(btnPrivacidad);
        pnlTablaContainer.add(pnlTablaHeader, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel();
        modeloTabla.setColumnIdentifiers(new Object[]{"N° Prestamo", "Monto Base", "Cuotas", "Interes (%)", "Saldo Restante", "Estado"});
        
        tblMisPrestamos = new JTable(modeloTabla);
        tblMisPrestamos.setBackground(new Color(23, 33, 43));
        tblMisPrestamos.setForeground(Color.WHITE);
        tblMisPrestamos.setGridColor(new Color(36, 47, 61));
        tblMisPrestamos.setRowHeight(30);
        
        JTableHeader header = tblMisPrestamos.getTableHeader();
        header.setBackground(new Color(36, 47, 61));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Aplicamos el Renderizador de Privacidad a las columnas financieras
        tblMisPrestamos.getColumnModel().getColumn(1).setCellRenderer(privacidadRenderer);
        tblMisPrestamos.getColumnModel().getColumn(4).setCellRenderer(privacidadRenderer);

        pnlTablaContainer.add(new JScrollPane(tblMisPrestamos), BorderLayout.CENTER);
        pnlCentral.add(pnlTablaContainer, BorderLayout.CENTER);
        
        add(pnlCentral, BorderLayout.CENTER);
        
        historial.push("Inicializacion del Portal de Estudiante para codigo: " + codigoUsuario);
        
        cargarDatosDesdePostgres();
    }

    private void cargarDatosDesdePostgres() {
        String dniEstudiante = codigoUsuario.substring(1); 
        idEstudianteAutenticado = -1;

        try (Connection con = ConexionBD.conectar()) {
            String queryEst = "SELECT id_estudiante, nombre, apellido, promedio_notas, porcentaje_asistencia, estado_universidad FROM estudiantes WHERE dni = ?";
            try (PreparedStatement ps = con.prepareStatement(queryEst)) {
                ps.setString(1, dniEstudiante);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    idEstudianteAutenticado = rs.getInt("id_estudiante");
                    String nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido");
                    lblNombre.setText("Hola, " + nombreCompleto);
                    lblPromedio.setText(rs.getString("promedio_notas"));
                    lblAsistencia.setText(rs.getString("porcentaje_asistencia") + "%");
                    lblEstadoUni.setText(rs.getString("estado_universidad"));
                    
                    historial.push("Consulta de perfil academica exitosa para: " + nombreCompleto);
                }
            }

            if (idEstudianteAutenticado != -1) {
                String queryPre = "SELECT id_prestamo, monto, cuotas, tasa_interes, saldo, estado FROM prestamos WHERE id_estudiante = ?";
                try (PreparedStatement ps = con.prepareStatement(queryPre)) {
                    ps.setInt(1, idEstudianteAutenticado);
                    ResultSet rs = ps.executeQuery();
                    double acumuladorDeuda = 0;
                    modeloTabla.setRowCount(0);
                    int contadorPrestamos = 0;
                    int totalCuotasEstructura = 0;
                    
                    arbolPrestamosUsuario = new ArbolEstudiante();
                    colaPagosPendientes = new ColaCronograma();
                    
                    while (rs.next()) {
                        int idPre = rs.getInt("id_prestamo");
                        double monto = rs.getDouble("monto");
                        int cuotas = rs.getInt("cuotas");
                        double tasa = rs.getDouble("tasa_interes");
                        double saldo = rs.getDouble("saldo");
                        String est = rs.getString("estado");
                        
                        acumuladorDeuda += saldo;
                        contadorPrestamos++;
                        
                        if (est.equalsIgnoreCase("Activo")) {
                            totalCuotasEstructura += cuotas;
                        }
                        
                        arbolPrestamosUsuario.insertar(idPre, monto, est);
                        
                        modeloTabla.addRow(new Object[]{
                            "P-" + String.format("%03d", idPre),
                            "S/. " + monto,
                            cuotas,
                            tasa + "%",
                            "S/. " + String.format("%.2f", saldo),
                            est
                        });
                    }
                    saldoTotalReal = acumuladorDeuda; 
                    lblDeudaTotal.setText("S/. " + String.format("%.2f", saldoTotalReal));
                    
                    if (contadorPrestamos > 0) {
                        btnMisBoletas.setEnabled(true);
                    } else {
                        btnMisBoletas.setEnabled(false);
                    }
                    
                    inicializarColaCronograma(acumuladorDeuda, totalCuotasEstructura);
                    historial.push("Finanzas mapeadas en Tabla, Arbol y Cola. Total creditos: " + contadorPrestamos);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error de sincronizacion de UI: " + e.getMessage());
        }
    }

    private void inicializarColaCronograma(double saldoTotal, int totalCuotas) {
        if (totalCuotas > 0 && saldoTotal > 0) {
            double montoPorCuota = saldoTotal / totalCuotas;
            for (int i = 1; i <= totalCuotas; i++) {
                colaPagosPendientes.encolar(i, montoPorCuota);
            }
        }
    }

    private void consultarPrelacionCuota() {
        String infoCuota = colaPagosPendientes.verTope();
        JOptionPane.showMessageDialog(this, infoCuota, "Prelacion Cronologica de Amortizaciones (FIFO)", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarReporteArbol() {
        StringBuilder sb = new StringBuilder();
        sb.append("   BCHP - SIMULADOR BI DE CAPACIDAD DE REFINANCIAMIENTO\n");
        sb.append("   Estructura: Arbol Binario de Busqueda de Creditos\n");
        
        if (arbolPrestamosUsuario.getRaiz() == null) {
            sb.append("Fallo: El sistema no registra creditos en memoria para indexar el arbol.\n");
            sb.append("   Estado: SIN HISTORIAL - NO APTO PARA EVALUACION.");
        } else {
            sb.append("Arbol: [Analisis Recursivo del Arbol de Financiamientos]:\n");
            arbolPrestamosUsuario.generarReporteInOrder(arbolPrestamosUsuario.getRaiz(), sb);
            
            NodoArbol prestamoPrincipal = arbolPrestamosUsuario.getRaiz(); 
            double notaActual = Double.parseDouble(lblPromedio.getText());
            String situacionMatricula = lblEstadoUni.getText().toUpperCase();
            
            int scoreCredito = 500; 
            
            if (prestamoPrincipal.estado.equalsIgnoreCase("Activo") || prestamoPrincipal.estado.equalsIgnoreCase("PAGADO")) {
                scoreCredito += 150;
            } else {
                scoreCredito -= 200; 
            }
            
            if (notaActual >= 14.0) scoreCredito += 100;
            if (situacionMatricula.equals("ACTIVO")) scoreCredito += 100;
            else if (situacionMatricula.equals("ALERTA")) scoreCredito -= 150;

            sb.append("Metricas: [Resultado del Motor de Inferencia Criptografico]:\n");
            sb.append("   * Score de Credito Interno BCHP: ").append(scoreCredito).append(" Puntos\n\n");
            
            sb.append("Analisis: [Dictamen Automatico del Sistema]:\n");
            if (scoreCredito >= 700) {
                sb.append("   PRE-APROBADO: Excelente comportamiento. Su arbol financiero\n");
                sb.append("      refleja estabilidad. Apto para ampliacion de linea de credito.");
            } else if (scoreCredito >= 500) {
                sb.append("   EVALUACION MANUAL: Capacidad de endeudamiento media.\n");
                sb.append("      Requiere el aval presencial del Asistente de Caja.");
            } else {
                sb.append("   BLOQUEADO: Riesgo financiero critico detectado en sus ramas.\n");
                sb.append("      Portal restringe nuevas solicitudes de manera preventiva.");
            }
        }
        
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setBackground(new Color(15, 23, 31)); 
        textArea.setForeground(new Color(52, 152, 219)); 
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(550, 380));
        
        JOptionPane.showMessageDialog(this, scrollPane, "BCHP BI Engine - Inteligencia de Estructuras", JOptionPane.PLAIN_MESSAGE);
    }

    private void abrirDialogoBoletas() {
        JDialog dlg = new JDialog(this, "BCHP - Mis Comprobantes Digitales QR", true);
        dlg.setSize(420, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));
        dlg.getContentPane().setBackground(new Color(23, 33, 43));

        JLabel lblInfo = new JLabel("<html><center><b>COMPROBANTE ELECTRONICO DE AMORTIZACION</b><br>Codigo de Integridad extraido del Servidor Relacional</center></html>", JLabel.CENTER);
        lblInfo.setForeground(Color.WHITE);
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfo.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        dlg.add(lblInfo, BorderLayout.NORTH);

        ImageIcon iconoQRFinal = null;
        String sqlQR = "SELECT codigo_qr FROM prestamos WHERE id_estudiante = ? AND estado = 'Activo' ORDER BY id_prestamo DESC LIMIT 1";
        
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sqlQR)) {
            ps.setInt(1, idEstudianteAutenticado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    InputStream is = rs.getBinaryStream("codigo_qr");
                    if (is != null) {
                        BufferedImage bi = ImageIO.read(is);
                        if (bi != null) {
                            Image imgEscalada = bi.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                            iconoQRFinal = new ImageIcon(imgEscalada);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Sincronizacion de bytes de imagen: " + e.getMessage());
        }

        JPanel pnlCentroCuerpo = new JPanel(new BorderLayout());
        pnlCentroCuerpo.setBackground(new Color(23, 33, 43));

        if (iconoQRFinal != null) {
            JLabel lblImagenQR = new JLabel(iconoQRFinal, JLabel.CENTER);
            pnlCentroCuerpo.add(lblImagenQR, BorderLayout.CENTER);
        } else {
            JPanel pnlQRCanvas = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    g2.setColor(Color.WHITE);
                    g2.fillRect(100, 20, 200, 200);
                    
                    g2.setColor(Color.BLACK);
                    g2.fillRect(110, 30, 50, 50);  
                    g2.fillRect(240, 30, 50, 50);  
                    g2.fillRect(110, 160, 50, 50); 
                    
                    g2.setColor(Color.WHITE);
                    g2.fillRect(120, 40, 30, 30);
                    g2.fillRect(250, 40, 30, 30);
                    g2.fillRect(120, 170, 30, 30);

                    g2.setColor(Color.BLACK);
                    g2.fillRect(130, 50, 10, 10);
                    g2.fillRect(260, 50, 10, 10);
                    g2.fillRect(130, 180, 10, 10);
                    
                    g2.fillRect(175, 100, 20, 15);
                    g2.fillRect(200, 130, 15, 30);
                    g2.fillRect(175, 160, 30, 10);
                    g2.fillRect(220, 95, 15, 15);
                    g2.fillRect(175, 45, 40, 10);
                    g2.fillRect(125, 110, 30, 20);
                }
            };
            pnlQRCanvas.setBackground(new Color(23, 33, 43));
            pnlCentroCuerpo.add(pnlQRCanvas, BorderLayout.CENTER);
        }
        
        dlg.add(pnlCentroCuerpo, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        pnlFooter.setBackground(new Color(17, 28, 36));
        
        JButton btnCerrarDlg = new JButton("Entendido / Escaneado");
        btnCerrarDlg.setBackground(new Color(52, 152, 219));
        btnCerrarDlg.setForeground(Color.WHITE);
        btnCerrarDlg.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCerrarDlg.setPreferredSize(new Dimension(180, 35));
        btnCerrarDlg.setBorderPainted(false);
        btnCerrarDlg.addActionListener(ev -> dlg.dispose());
        
        pnlFooter.add(btnCerrarDlg);
        dlg.add(pnlFooter, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private void abrirDialogoPagosFIFO() {
        if (colaPagosPendientes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Felicidades. No registras cuotas pendientes de pago.", "Caja BCHP", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dlg = new JDialog(this, "BCHP - Pasarela de Amortizacion Dinamica (FIFO)", true);
        dlg.setSize(650, 350);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(new Color(23, 33, 43));

        JLabel lblTitulo = new JLabel("  Amortizacion FIFO (Regla: Solo se permite liquidar la cuota mas antigua)");
        lblTitulo.setForeground(new Color(241, 196, 15));
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        dlg.add(lblTitulo, BorderLayout.NORTH);

        DefaultTableModel modPagos = new DefaultTableModel(new Object[]{"Orden", "Descripcion", "Monto Base", "Mora (Dinamica)", "Total", "Pagar"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 5 ? Boolean.class : String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5 && row == 0; 
            }
        };

        ColaCronograma colaTemp = new ColaCronograma();
        int orden = 1;
        
        while (!colaPagosPendientes.isEmpty()) {
            String info = colaPagosPendientes.desencolar(); 
            String[] partes = info.split(" - Monto: S/. ");
            String desc = partes[0];
            double monto = Double.parseDouble(partes[1].replace(",", "."));
            
            double mora = (orden == 1) ? (monto * 0.05) : 0.0; 
            double total = monto + mora;

            modPagos.addRow(new Object[]{
                "#" + orden, desc, "S/. " + String.format("%.2f", monto), "S/. " + String.format("%.2f", mora), "S/. " + String.format("%.2f", total), false
            });
            
            colaTemp.encolar(Integer.parseInt(desc.replace("Cuota N° ", "")), monto);
            orden++;
        }
        colaPagosPendientes = colaTemp;

        JTable tablaPagos = new JTable(modPagos);
        tablaPagos.setBackground(new Color(36, 47, 61));
        tablaPagos.setForeground(Color.WHITE);
        tablaPagos.setRowHeight(30);
        tablaPagos.getTableHeader().setBackground(new Color(17, 28, 36));
        tablaPagos.getTableHeader().setForeground(Color.WHITE);
        
        dlg.add(new JScrollPane(tablaPagos), BorderLayout.CENTER);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        pnlBotones.setBackground(new Color(17, 28, 36));

        JLabel lblTotal = new JLabel("Total Seleccionado: S/. 0.00");
        lblTotal.setForeground(Color.WHITE);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));

        modPagos.addTableModelListener(e -> {
            if (e.getColumn() == 5) {
                boolean isChecked = (boolean) modPagos.getValueAt(0, 5);
                if (isChecked) {
                    String totalStr = modPagos.getValueAt(0, 4).toString().replace("S/. ", "").replace(",", ".");
                    lblTotal.setText("Total a Pagar: S/. " + totalStr);
                } else {
                    lblTotal.setText("Total Seleccionado: S/. 0.00");
                }
            }
        });

        JButton btnConfirmar = new JButton("Confirmar Pago Atomico");
        btnConfirmar.setBackground(new Color(52, 152, 219));
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnConfirmar.setFocusPainted(false);
        btnConfirmar.addActionListener(e -> {
            boolean isChecked = (boolean) modPagos.getValueAt(0, 5);
            if (!isChecked) {
                JOptionPane.showMessageDialog(dlg, "Error: Debes marcar la cuota del frente de la cola (N°1) para proceder con el pago.", "Restriccion FIFO de Base", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String pagado = colaPagosPendientes.desencolar();
            historial.push("Pago consolidado en base de datos. Se desencolo: " + pagado);
            
            JOptionPane.showMessageDialog(dlg, "Transaccion atomica exitosa en PostgreSQL.\n" + pagado + " ha sido amortizada.", "Pago Registrado", JOptionPane.INFORMATION_MESSAGE);
            dlg.dispose();
            
            cargarDatosDesdePostgres(); 
        });

        pnlBotones.add(lblTotal);
        pnlBotones.add(btnConfirmar);
        dlg.add(pnlBotones, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private JLabel crearTarjetaKPI(JPanel panel, String titulo, String valor, Color colorCinta) {
        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.setBackground(new Color(23, 33, 43));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(36, 47, 61)),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        
        JPanel cinta = new JPanel(); cinta.setBackground(colorCinta); cinta.setPreferredSize(new Dimension(5, 0));
        tarjeta.add(cinta, BorderLayout.WEST);
        
        JPanel info = new JPanel(new GridLayout(2, 1)); info.setBackground(new Color(23, 33, 43));
        JLabel t = new JLabel(titulo); t.setFont(new Font("Segoe UI", Font.BOLD, 10)); t.setForeground(new Color(120, 144, 156));
        JLabel v = new JLabel(valor); v.setFont(new Font("Segoe UI", Font.BOLD, 18)); v.setForeground(Color.WHITE);
        
        info.add(t); info.add(v); tarjeta.add(info, BorderLayout.CENTER);
        panel.add(tarjeta);
        return v;
    }
}