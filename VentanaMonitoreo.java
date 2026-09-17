package caja;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.sql.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.plot.CategoryPlot;

public class VentanaMonitoreo extends JFrame {
    private DefaultTableModel modelo;
    private JLabel lblTotalPrestado, lblTotalCobrado, lblSaldoPendiente;
    private JComboBox<String> comboFiltroCumplimiento;
    
    private JTree treeVisual;
    private DefaultTreeModel treeModel;

    private JPanel pnlContenedorGrafico;

    // ESTRUCTURA DE ATENCION: COLA DE AUDITORIA DE ALERTAS (FIFO)
    private ColaAlertas colaInfracciones = new ColaAlertas();
    private JButton btnProcesarAlerta;
    // Pila LIFO nativa de Java para guardar los respaldos inmutables de la base de datos
    private java.util.Stack<java.util.Map<Integer, Double>> pilaRespaldosSaldos = new java.util.Stack<>();
    private JButton btnRollback;    

    // ===== CLASE NODO INTERNA PARA LA COLA =====
    static class NodoAlerta {
        String mensajeAlerta;
        long timestamp;
        NodoAlerta siguiente;

        public NodoAlerta(String mensajeAlerta) {
            this.mensajeAlerta = mensajeAlerta;
            this.timestamp = System.currentTimeMillis();
            this.siguiente = null;
        }
    }

    // ===== CLASE COLA INTERNA (FIFO) =====
    static class ColaAlertas {
        private NodoAlerta frente;
        private NodoAlerta fin;

        public ColaAlertas() {
            frente = null;
            fin = null;
        }

        public void encolar(String mensaje) {
            NodoAlerta nuevo = new NodoAlerta(mensaje);
            if (isEmpty()) {
                frente = nuevo;
            } else {
                fin.siguiente = nuevo;
            }
            fin = nuevo;
        }

        public String desencolar() {
            if (isEmpty()) return null;
            String msg = frente.mensajeAlerta;
            frente = frente.siguiente;
            if (frente == null) {
                fin = null;
            }
            return msg;
        }

        public boolean isEmpty() {
            return frente == null;
        }
        
        public String verProximaAlerta() {
            if (isEmpty()) return "No hay alertas pendientes de procesamiento.";
            return frente.mensajeAlerta;
        }
    }

    // ===== CONSTRUCTOR DE LA VENTANA =====
    public VentanaMonitoreo() {
        setTitle("BCHP - Modulo de Reportes, Cumplimiento y Restricciones");
        setSize(1350, 750); 
        setLayout(new BorderLayout(15, 15));
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(17, 28, 36));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel pnlKPI = new JPanel(new GridLayout(1, 3, 20, 0));
        pnlKPI.setBackground(new Color(17, 28, 36));
        pnlKPI.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        lblTotalPrestado = crearTarjetaKPI(pnlKPI, "CAPITAL ADJUDICADO TOTAL", "S/. 0.00", new Color(41, 128, 185));
        lblTotalCobrado = crearTarjetaKPI(pnlKPI, "TOTAL RECUPERADO", "S/. 0.00", new Color(46, 204, 113));
        lblSaldoPendiente = crearTarjetaKPI(pnlKPI, "SALDO ACTIVO EXPUESTO", "S/. 0.00", new Color(231, 76, 60));
        add(pnlKPI, BorderLayout.NORTH);

        JPanel pnlContenidoCentral = new JPanel(new GridLayout(1, 2, 15, 0));
        pnlContenidoCentral.setBackground(new Color(17, 28, 36));

        JPanel pnlEstructurasInternas = new JPanel(new GridLayout(1, 2, 15, 0));
        pnlEstructurasInternas.setBackground(new Color(17, 28, 36));

        JPanel pnlCentro = new JPanel(new BorderLayout(10, 10));
        pnlCentro.setBackground(new Color(23, 33, 43));
        pnlCentro.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(36, 47, 61)), "Seguimiento de Alumnos y Restricciones Financieras",
            0, 0, new Font("Segoe UI", Font.BOLD, 12), Color.WHITE));
        
        modelo = new DefaultTableModel();
        modelo.setColumnIdentifiers(new Object[]{
            "Estudiante", "Promedio", "Asistencia", "Saldo Deuda", "Situacion", "Dictamen del Sistema"
        });
        
        JTable tabla = new JTable(modelo);
        tabla.setBackground(new Color(23, 33, 43));
        tabla.setForeground(Color.WHITE);
        tabla.setGridColor(new Color(36, 47, 61));
        tabla.setRowHeight(28);
        
        JTableHeader header = tabla.getTableHeader();
        header.setBackground(new Color(36, 47, 61));
        header.setForeground(Color.WHITE);
        pnlCentro.add(new JScrollPane(tabla), BorderLayout.CENTER);
        pnlEstructurasInternas.add(pnlCentro);

        JPanel pnlArbolGrafico = new JPanel(new BorderLayout(10, 10));
        pnlArbolGrafico.setBackground(new Color(23, 33, 43));
        pnlArbolGrafico.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(36, 47, 61)), "Estructura Jerarquica del Arbol de Prestamos (En Memoria)",
            0, 0, new Font("Segoe UI", Font.BOLD, 12), Color.WHITE));

        DefaultMutableTreeNode raizGraficaInicial = new DefaultMutableTreeNode("Arbol de Prestamos");
        treeModel = new DefaultTreeModel(raizGraficaInicial);
        treeVisual = new JTree(treeModel);
        
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setBackgroundNonSelectionColor(new Color(23, 33, 43));
        renderer.setBackgroundSelectionColor(new Color(41, 128, 185));
        renderer.setTextNonSelectionColor(Color.WHITE);
        renderer.setTextSelectionColor(Color.WHITE);
        renderer.setBorderSelectionColor(new Color(0, 168, 204));
        renderer.setLeafIcon(new IconoPersonita());
        renderer.setOpenIcon(new IconoCarpetaBCHP(true));
        renderer.setClosedIcon(new IconoCarpetaBCHP(false));
        
        treeVisual.setCellRenderer(renderer);
        treeVisual.setBackground(new Color(23, 33, 43));
        
        JScrollPane scrollTree = new JScrollPane(treeVisual);
        scrollTree.getViewport().setBackground(new Color(23, 33, 43));
        scrollTree.setBorder(BorderFactory.createEmptyBorder());
        pnlArbolGrafico.add(scrollTree, BorderLayout.CENTER);
        pnlEstructurasInternas.add(pnlArbolGrafico);

        pnlContenidoCentral.add(pnlEstructurasInternas);

        JPanel pnlReporteBI = new JPanel(new BorderLayout(10, 10));
        pnlReporteBI.setBackground(new Color(23, 33, 43));
        pnlReporteBI.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(36, 47, 61)), "Dashboard Analitico: Saldo Expuesto por Situacion Universitaria",
            0, 0, new Font("Segoe UI", Font.BOLD, 12), Color.WHITE));

        pnlContenedorGrafico = new JPanel(new BorderLayout()); 
        pnlContenedorGrafico.setBackground(new Color(23, 33, 43));
        pnlReporteBI.add(pnlContenedorGrafico, BorderLayout.CENTER);
        pnlContenidoCentral.add(pnlReporteBI);

        add(pnlContenidoCentral, BorderLayout.CENTER);

        JPanel pnlLateral = new JPanel();
        pnlLateral.setPreferredSize(new Dimension(220, 0));
        pnlLateral.setBackground(new Color(23, 33, 43));
        pnlLateral.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 20));

        JLabel lblF = new JLabel("FILTRAR POR REGLA");
        lblF.setForeground(Color.WHITE);
        lblF.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlLateral.add(lblF);

        comboFiltroCumplimiento = new JComboBox<>(new String[]{"Ver Todo el Alumnado", "Rendimiento Deficiente (<12)", "Faltas Criticas (<70%)", "Alumnos Retirados de U"});
        comboFiltroCumplimiento.setPreferredSize(new Dimension(200, 35));
        comboFiltroCumplimiento.addActionListener(e -> procesarMatrizCumplimiento());
        pnlLateral.add(comboFiltroCumplimiento);

        // COLA ACCION: Boton para despachar y atender las alertas de intrusion acumuladas
        btnProcesarAlerta = new JButton("Atender Alerta de Intrusion");
        btnProcesarAlerta.setBackground(new Color(241, 196, 15)); // Amarillo corporativo de atencion
        btnProcesarAlerta.setForeground(Color.BLACK);
        btnProcesarAlerta.setPreferredSize(new Dimension(200, 40));
        btnProcesarAlerta.setBorderPainted(false);
        btnProcesarAlerta.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnProcesarAlerta.addActionListener(e -> procesarProximaAlerta());
        pnlLateral.add(btnProcesarAlerta);
        // BOTÓN DE PÁNICO (Rollback) - Apagado por defecto
        btnRollback = new JButton("Pánico: Rollback DB");
        btnRollback.setBackground(new Color(192, 57, 43)); // Rojo Alerta
        btnRollback.setForeground(Color.WHITE);
        btnRollback.setPreferredSize(new Dimension(200, 40));
        btnRollback.setBorderPainted(false);
        btnRollback.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnRollback.setEnabled(false); // Solo se enciende si hay un hackeo
        btnRollback.addActionListener(e -> ejecutarRollbackCriptografico());
        pnlLateral.add(btnRollback);

        JButton btnRestringir = new JButton("Bloquear Desercion");
        btnRestringir.setBackground(new Color(231, 76, 60));
        btnRestringir.setForeground(Color.WHITE);
        btnRestringir.setPreferredSize(new Dimension(200, 40));
        btnRestringir.setBorderPainted(false);
        pnlLateral.add(btnRestringir);

        JButton btnVolver = new JButton("Volver");
        btnVolver.setPreferredSize(new Dimension(200, 35));
        btnVolver.addActionListener(e -> this.dispose());
        pnlLateral.add(btnVolver);

        add(pnlLateral, BorderLayout.EAST);

        procesarMatrizCumplimiento();
        setVisible(true);
    }

    private void procesarMatrizCumplimiento() {
        modelo.setRowCount(0);
        int criterioFiltro = comboFiltroCumplimiento.getSelectedIndex();

        String sqlKPI = "SELECT SUM(monto + (monto * tasa_interes / 100)) as emi, SUM(saldo) as sal FROM prestamos";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement psKPI = con.prepareStatement(sqlKPI);
             ResultSet rsKPI = psKPI.executeQuery()) {
            if (rsKPI.next()) {
                double emitido = rsKPI.getDouble("emi");
                double saldoExpuesto = rsKPI.getDouble("sal");
                lblTotalPrestado.setText("S/. " + String.format("%.2f", emitido));
                lblSaldoPendiente.setText("S/. " + String.format("%.2f", saldoExpuesto));
                lblTotalCobrado.setText("S/. " + String.format("%.2f", (emitido - saldoExpuesto)));
            }
        } catch (SQLException e) {
            System.out.println("Error KPIs: " + e.getMessage());
        }

        ArbolPrestamo arbolAuditoria = new ArbolPrestamo();
        StringBuilder cadenaInmutable = new StringBuilder();

        String sqlData = "SELECT p.id_prestamo, e.nombre, e.apellido, p.monto, p.cuotas, p.saldo, p.tasa_interes, p.estado, e.promedio_notas, e.porcentaje_asistencia, e.estado_universidad " +
                         "FROM prestamos p INNER JOIN estudiantes e ON p.id_estudiante = e.id_estudiante";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement psData = con.prepareStatement(sqlData);
             ResultSet rs = psData.executeQuery()) {
            // Mapa temporal para guardar los saldos antes de que sean alterados
            java.util.Map<Integer, Double> snapshotActual = new java.util.HashMap<>();

            while (rs.next()) {
                prestamos pObj = new prestamos(
                    rs.getInt("id_prestamo"),
                    rs.getString("nombre") + " " + rs.getString("apellido"),
                    rs.getDouble("monto"),
                    rs.getInt("cuotas"),
                    rs.getDouble("tasa_interes"),
                    java.time.LocalDate.now()
                );
                pObj.saldo = rs.getDouble("saldo");
                // Guardamos el ID del préstamo y su saldo original en la instantánea
                snapshotActual.put(rs.getInt("id_prestamo"), pObj.saldo);

                arbolAuditoria.insertar(pObj);

                double nota = rs.getDouble("promedio_notas");
                double asistencia = rs.getDouble("porcentaje_asistencia");
                String estadoMatricula = rs.getString("estado_universidad");
                double deuda = rs.getDouble("saldo");
                String nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido");

                cadenaInmutable.append(nombreCompleto).append(nota).append(estadoMatricula).append(deuda);

                boolean evaluarFila = false;
                if (criterioFiltro == 0) evaluarFila = true;
                else if (criterioFiltro == 1 && nota < 12.0) evaluarFila = true;
                else if (criterioFiltro == 2 && asistencia < 70.0) evaluarFila = true;
                else if (criterioFiltro == 3 && estadoMatricula.toUpperCase().equals("RETIRADO")) evaluarFila = true;

                if (evaluarFila) {
                    String dictamen = "SITUACION ESTABLE - APTO";
                    if (nota < 12.0 || asistencia < 70.0) dictamen = "RIESGO DE DESERCION DETECTADO";
                    if (estadoMatricula.toUpperCase().equals("RETIRADO")) dictamen = "RESTRICCION: SUSPENDER CREDITO";

                    modelo.addRow(new Object[]{
                        nombreCompleto,
                        nota,
                        asistencia + "%",
                        "S/. " + String.format("%.2f", deuda),
                        estadoMatricula,
                        dictamen
                    });
                }
            }

            arbolAuditoria.mostrarInOrder();

            DefaultMutableTreeNode nodoRaizVisual = new DefaultMutableTreeNode("Cartera de Creditos (Indexado por Saldo)");
            prestamos maxDeuda = arbolAuditoria.obtenerDeudaMaxima();
            prestamos minDeuda = arbolAuditoria.obtenerDeudaMinima();

            if (maxDeuda != null && minDeuda != null) {
                DefaultMutableTreeNode ramaMenor = new DefaultMutableTreeNode("Menor Riesgo (Minimo Saldo)");
                ramaMenor.add(new DefaultMutableTreeNode(minDeuda.estudiante + " -> S/. " + minDeuda.saldo));
                
                DefaultMutableTreeNode ramaMayor = new DefaultMutableTreeNode("Mayor Riesgo (Maximo Saldo)");
                ramaMayor.add(new DefaultMutableTreeNode(maxDeuda.estudiante + " -> S/. " + maxDeuda.saldo));
                
                nodoRaizVisual.add(ramaMenor);
                nodoRaizVisual.add(ramaMayor);
            }

            treeModel.setRoot(nodoRaizVisual);
            for (int i = 0; i < treeVisual.getRowCount(); i++) {
                treeVisual.expandRow(i);
            }

            generarGraficoBIRiesgo();

           String hashDelMomento = SeguridadBCHP.calcularSHA256(cadenaInmutable.toString());

            if (SeguridadBCHP.HASH_MAESTRO_ESTUDIANTES.isEmpty()) {
                SeguridadBCHP.HASH_MAESTRO_ESTUDIANTES = hashDelMomento;
                // Guardamos la instantánea limpia en el tope de la Pila (PUSH)
                pilaRespaldosSaldos.push(snapshotActual); 
                System.out.println("🛡️ Criptografía Inicializada. Snapshot guardado en la Pila LIFO.");
            } else {
                if (hashDelMomento.equals(SeguridadBCHP.HASH_MAESTRO_ESTUDIANTES)) {
                    System.out.println("🛡️ Auditoría OK: Base de datos inmutable y segura.");
                    btnRollback.setEnabled(false); // Todo está bien
                } else {
                    colaInfracciones.encolar("VIOLACIÓN DE INTEGRIDAD - Saldos alterados desde pgAdmin.");
                    
                    JOptionPane.showMessageDialog(this, 
                        "🚨 ALERTA DE SEGURIDAD CRÍTICA DETECTADA 🚨\n\n" +
                        "Se ha identificado una manipulación externa de los saldos.\n" +
                        "La firma SHA-256 no coincide.\n\n" +
                        "Active el Protocolo de Rollback para restaurar la Base de Datos.", 
                        "BCHP Criptográfico - Alerta de Intrusión", JOptionPane.ERROR_MESSAGE);
                    
                    comboFiltroCumplimiento.setEnabled(false);
                    btnRollback.setEnabled(true); // ¡SE ENCIENDE EL BOTÓN DE PÁNICO!
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error analitico: " + e.getMessage());
        }
    }

    // 🔥 METODO OPERATIVO DE LA COLA: Desencola y procesa los incidentes en orden de llegada
    private void procesarProximaAlerta() {
        if (colaInfracciones.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El flujo de auditoria se encuentra limpio. Cero anomalias.", "Seguridad BCHP", JOptionPane.INFORMATION_MESSAGE);
        } else {
            String incidente = colaInfracciones.desencolar();
            JOptionPane.showMessageDialog(this, 
                "Desencolando Proximo Incidente de Seguridad (FIFO):\n\n" + incidente + "\n\n" +
                "Estado: MITIGADO - Restableciendo candado maestro en memoria RAM.", 
                "Centro de Mitigacion de Riesgos BCHP", JOptionPane.WARNING_MESSAGE);
            
            // Permite restaurar temporalmente los controles al limpiar la cola de incidentes
            if (colaInfracciones.isEmpty()) {
                comboFiltroCumplimiento.setEnabled(true);
            }
        }
    }

    private void generarGraficoBIRiesgo() {
        pnlContenedorGrafico.removeAll();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String sqlChart = "SELECT e.estado_universidad, SUM(p.saldo) AS saldo_total " +
                          "FROM prestamos p " +
                          "INNER JOIN estudiantes e ON p.id_estudiante = e.id_estudiante " +
                          "WHERE p.estado = 'Activo' " + 
                          "GROUP BY e.estado_universidad";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sqlChart);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String estadoUni = rs.getString("estado_universidad");
                double saldoTotal = rs.getDouble("saldo_total");
                dataset.addValue(saldoTotal, "Saldo Activo Fijo", estadoUni);
            }

        } catch (SQLException e) {
            System.out.println("Error generando analitica de barras: " + e.getMessage());
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Distribucion de Deuda Activa por Estado de Alumno", 
            "Condicion Universitaria", 
            "Volumen de Saldo Neto (S/.)", 
            dataset,
            PlotOrientation.VERTICAL,
            true,  
            true,  
            false  
        );

        chart.setBackgroundPaint(new Color(23, 33, 43)); 
        chart.getTitle().setPaint(Color.WHITE);
        chart.getLegend().setBackgroundPaint(new Color(23, 33, 43));
        chart.getLegend().setItemPaint(Color.WHITE);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(17, 28, 36)); 
        plot.setDomainGridlinePaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.WHITE);

        plot.getDomainAxis().setLabelPaint(Color.WHITE);
        plot.getDomainAxis().setTickLabelPaint(Color.WHITE);
        plot.getRangeAxis().setLabelPaint(Color.WHITE);
        plot.getRangeAxis().setTickLabelPaint(Color.WHITE);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(231, 76, 60)); 
        renderer.setDrawBarOutline(false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(new Color(23, 33, 43));
        pnlContenedorGrafico.add(chartPanel, BorderLayout.CENTER);
        
        pnlContenedorGrafico.revalidate();
        pnlContenedorGrafico.repaint();
    }

    private JLabel crearTarjetaKPI(JPanel pnl, String titulo, String valor, Color color) {
        JPanel muerte = new JPanel(new GridLayout(2, 1)); muerte.setBackground(color);
        muerte.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JLabel t = new JLabel(titulo); t.setForeground(Color.WHITE); t.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        JLabel v = new JLabel(valor); v.setForeground(Color.WHITE); v.setFont(new Font("Segoe UI", Font.BOLD, 18));
        muerte.add(t); muerte.add(v); pnl.add(muerte);
        return v;
    }

    private class IconoPersonita implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 168, 204));
            g2.fillOval(x + 4, y + 1, 8, 8);
            g2.fillArc(x + 1, y + 9, 14, 10, 0, 180);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 16; }
        @Override public int getIconHeight() { return 16; }
    }

    private class IconoCarpetaBCHP implements Icon {
        private boolean abierta;
        public IconoCarpetaBCHP(boolean abierta) { this.abierta = abierta; }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(52, 152, 219));
            if (abierta) {
                g2.fillRect(x + 1, y + 4, 14, 10);
                g2.setColor(new Color(41, 128, 185));
                g2.fillRect(x + 3, y + 2, 6, 3);
            } else {
                g2.fillRect(x + 1, y + 3, 14, 11);
                g2.setColor(new Color(41, 128, 185));
                g2.fillRect(x + 2, y + 1, 5, 2);
            }
            g2.dispose();
        }
        @Override public int getIconWidth() { return 16; }
        @Override public int getIconHeight() { return 16; }
        
    }

private void ejecutarRollbackCriptografico() {
        if (pilaRespaldosSaldos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay respaldos en la pila para restaurar.", "Error Crítico", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // POP: Extraemos el último estado limpio de la Pila LIFO
        java.util.Map<Integer, Double> respaldoLimpio = pilaRespaldosSaldos.pop();
        int registrosRestaurados = 0;

        String sqlRestore = "UPDATE prestamos SET saldo = ? WHERE id_prestamo = ?";
        
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sqlRestore)) {
            
            // Recorremos la instantánea y reescribimos la base de datos a la fuerza
            for (java.util.Map.Entry<Integer, Double> registro : respaldoLimpio.entrySet()) {
                ps.setDouble(1, registro.getValue());
                ps.setInt(2, registro.getKey());
                ps.addBatch(); // Ejecución en lote para mayor rendimiento
                registrosRestaurados++;
            }
            
            ps.executeBatch(); // Disparamos la restauración masiva
            
            // Reseteamos el Hash para que vuelva a confiar en el sistema
            SeguridadBCHP.HASH_MAESTRO_ESTUDIANTES = "";
            
            JOptionPane.showMessageDialog(this, 
                "✅ ROLLBACK COMPLETADO CON ÉXITO ✅\n\n" +
                "Se han restaurado " + registrosRestaurados + " registros desde la memoria RAM.\n" +
                "La base de datos ha regresado a su estado íntegro original.", 
                "Protocolo de Restauración LIFO", JOptionPane.INFORMATION_MESSAGE);
            
            // Recargamos la interfaz
            procesarMatrizCumplimiento();
            btnRollback.setEnabled(false);
            comboFiltroCumplimiento.setEnabled(true);
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Fallo durante el Rollback: " + ex.getMessage());
        }
}
}