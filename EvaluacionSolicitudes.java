package caja;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.sql.*;

public class EvaluacionSolicitudes extends JFrame {
    private DefaultTableModel modelo;
    private JComboBox<String> comboFiltroEstado;
    private JTable tabla; 
    private JTree treeVisual;
    private DefaultTreeModel treeModel;

    Color colorPrimario = new Color(41, 128, 185); 
    Color colorSecundario = new Color(23, 33, 43);
    Color colorExito = new Color(46, 204, 113);
    Color colorFondo = new Color(17, 28, 36);

    public EvaluacionSolicitudes() {
        setTitle("BCHP - Mesa de Aprobación y Evaluación de Solicitudes");
        setSize(1250, 650); 
        setLayout(new BorderLayout(15, 15));
        setLocationRelativeTo(null);
        getContentPane().setBackground(colorFondo);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel pnlHead = new JPanel();
        pnlHead.setBackground(colorSecundario);
        pnlHead.setPreferredSize(new Dimension(1250, 60));
        JLabel tit = new JLabel("Mesa de Control: Evaluación y Aprobación de Créditos");
        tit.setForeground(Color.WHITE); 
        tit.setFont(new Font("Segoe UI", Font.BOLD, 20));
        pnlHead.add(tit);
        add(pnlHead, BorderLayout.NORTH);

        JPanel pnlEstructuras = new JPanel(new GridLayout(1, 2, 15, 0));
        pnlEstructuras.setBackground(colorFondo);
        pnlEstructuras.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel pnlTablaCentro = new JPanel(new BorderLayout(10, 10));
        pnlTablaCentro.setBackground(colorSecundario);
        pnlTablaCentro.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(36, 47, 61)), "Solicitudes de Crédito Pendientes",
            0, 0, new Font("Segoe UI", Font.BOLD, 12), Color.WHITE));

        modelo = new DefaultTableModel();
        modelo.setColumnIdentifiers(new Object[]{"ID Solicitud", "Estudiante", "Promedio", "Monto Solicitado", "Cuotas", "Recomendación"});
        
        tabla = new JTable(modelo);
        tabla.setBackground(colorSecundario);
        tabla.setForeground(Color.WHITE);
        tabla.setGridColor(new Color(36, 47, 61));
        tabla.setRowHeight(26);

        JTableHeader header = tabla.getTableHeader();
        header.setBackground(new Color(36, 47, 61));
        header.setForeground(Color.WHITE);
        pnlTablaCentro.add(new JScrollPane(tabla), BorderLayout.CENTER);
        pnlEstructuras.add(pnlTablaCentro);

        JPanel pnlArbolGrafico = new JPanel(new BorderLayout(10, 10));
        pnlArbolGrafico.setBackground(colorSecundario);
        pnlArbolGrafico.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(36, 47, 61)), "Árbol Binario de Priorización",
            0, 0, new Font("Segoe UI", Font.BOLD, 12), Color.WHITE));

        DefaultMutableTreeNode raizGraficaInicial = new DefaultMutableTreeNode("Cola de Evaluación");
        treeModel = new DefaultTreeModel(raizGraficaInicial);
        treeVisual = new JTree(treeModel);

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setBackgroundNonSelectionColor(colorSecundario);
        renderer.setBackgroundSelectionColor(colorPrimario); 
        renderer.setTextNonSelectionColor(Color.WHITE);
        renderer.setTextSelectionColor(Color.WHITE);
        renderer.setBorderSelectionColor(new Color(0, 168, 204));
        
        renderer.setLeafIcon(new IconoSolicitante()); 
        renderer.setOpenIcon(new IconoCarpetaAsistente(true));  
        renderer.setClosedIcon(new IconoCarpetaAsistente(false)); 
        
        treeVisual.setCellRenderer(renderer);
        treeVisual.setBackground(colorSecundario);

        JScrollPane scrollTree = new JScrollPane(treeVisual);
        scrollTree.getViewport().setBackground(colorSecundario);
        scrollTree.setBorder(BorderFactory.createEmptyBorder());
        pnlArbolGrafico.add(scrollTree, BorderLayout.CENTER);
        pnlEstructuras.add(pnlArbolGrafico);

        add(pnlEstructuras, BorderLayout.CENTER);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
        pnlBotones.setBackground(colorFondo);
        
        JLabel lblF = new JLabel("Filtrar Estado:");
        lblF.setForeground(Color.WHITE);
        lblF.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlBotones.add(lblF);

        comboFiltroEstado = new JComboBox<>(new String[]{"PENDIENTE", "APROBADO", "RECHAZADO", "ESCALADO"});
        comboFiltroEstado.setPreferredSize(new Dimension(140, 32));
        comboFiltroEstado.addActionListener(e -> cargarSolicitudesYArbol());
        pnlBotones.add(comboFiltroEstado);
        
        JButton btnScore = crearBoton("Calcular Score", new Color(142, 68, 173)); 
        btnScore.addActionListener(e -> calcularScoreFinanciero());
        pnlBotones.add(btnScore);

        JButton btnAprobar = crearBoton("Aprobar Solicitud", colorExito);
        btnAprobar.addActionListener(e -> modificarEstadoSolicitud("APROBADO"));
        pnlBotones.add(btnAprobar);

        JButton btnRechazar = crearBoton("Rechazar Solicitud", new Color(231, 76, 60));
        btnRechazar.addActionListener(e -> modificarEstadoSolicitud("RECHAZADO"));
        pnlBotones.add(btnRechazar);

        JButton btnVolver = crearBoton("Volver al Menú", new Color(127, 140, 141));
        btnVolver.addActionListener(e -> this.dispose());
        pnlBotones.add(btnVolver);

        add(pnlBotones, BorderLayout.SOUTH);

        cargarSolicitudesYArbol();
        setVisible(true);
    }

    private void cargarSolicitudesYArbol() {
        modelo.setRowCount(0);
        String filtroEstado = (String) comboFiltroEstado.getSelectedItem();

        String sql = "SELECT s.id_solicitud, e.nombre, e.apellido, e.promedio_notas, s.monto_solicitado, s.cuotas_solicitadas, s.recomendacion_sistema " +
                     "FROM solicitudes_prestamo s " +
                     "INNER JOIN estudiantes e ON s.id_estudiante = e.id_estudiante " +
                     "WHERE s.estado_solicitud = ? ORDER BY s.id_solicitud DESC";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, filtroEstado);
            ResultSet rs = ps.executeQuery();

            DefaultMutableTreeNode nodoRaizVisual = new DefaultMutableTreeNode("Solicitudes: " + filtroEstado);
            DefaultMutableTreeNode ramaExcelente = new DefaultMutableTreeNode("Rendimiento Alto (Promedio >= 15)");
            DefaultMutableTreeNode ramaRegular = new DefaultMutableTreeNode("Rendimiento Regular (Promedio < 15)");

            boolean tieneExcelente = false;
            boolean tieneRegular = false;

            while (rs.next()) {
                int id = rs.getInt("id_solicitud");
                String alumno = rs.getString("nombre") + " " + rs.getString("apellido");
                double promedio = rs.getDouble("promedio_notas");
                double monto = rs.getDouble("monto_solicitado");
                int cuotas = rs.getInt("cuotas_solicitadas");
                String recomendacion = rs.getString("recomendacion_sistema");

                modelo.addRow(new Object[]{"SOL-" + String.format("%03d", id), alumno, promedio, "S/. " + monto, cuotas, recomendacion});

                DefaultMutableTreeNode nodoAlumno = new DefaultMutableTreeNode(alumno + " (Nota: " + promedio + ")");
                if (promedio >= 15.0) {
                    ramaExcelente.add(nodoAlumno);
                    tieneExcelente = true;
                } else {
                    ramaRegular.add(nodoAlumno);
                    tieneRegular = true;
                }
            }

            if (tieneExcelente) nodoRaizVisual.add(ramaExcelente);
            if (tieneRegular) nodoRaizVisual.add(ramaRegular);

            treeModel.setRoot(nodoRaizVisual);
            
            for (int i = 0; i < treeVisual.getRowCount(); i++) {
                treeVisual.expandRow(i);
            }

        } catch (SQLException e) {
            System.out.println("Error cargando mesa de control: " + e.getMessage());
        }
    }

    private void modificarEstadoSolicitud(String nuevoEstado) {
        System.out.println("\n--- INICIO DE PROCESO: " + nuevoEstado + " ---");
        int filaSeleccionada = tabla.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Primero selecciona una solicitud de la tabla.");
            return;
        }

        String idSolicitudStr = modelo.getValueAt(filaSeleccionada, 0).toString().replace("SOL-", "");
        int idSolicitudReal = Integer.parseInt(idSolicitudStr);

        // ==========================================
        // VALIDACIÓN OTP 
        // ==========================================
        if (nuevoEstado.equalsIgnoreCase("APROBADO")) {
            String montoStr = modelo.getValueAt(filaSeleccionada, 3).toString().replace("S/.", "").replace(",", "").trim();
            double montoReal = 0.0;

            try {
                montoReal = Double.parseDouble(montoStr);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error leyendo el monto: " + montoStr);
                return;
            }

            if (montoReal >= 2000.00) {
                long semillaTiempo = System.currentTimeMillis() / 1000 / 120; 
                String hashGenerado = SeguridadBCHP.calcularSHA256("BCHP-OTP-" + semillaTiempo);
                String tokenCorrecto = hashGenerado.substring(0, 6).toUpperCase();

                System.out.println("🔑 TOKEN OTP GENERADO (Expira en 2 min): " + tokenCorrecto);

                String tokenIngresado = JOptionPane.showInputDialog(this,
                    "La solicitud excede el umbral seguro de S/. 2,000.00.\nIngrese Token OTP:",
                    "BCHP Security Gateway", JOptionPane.WARNING_MESSAGE);

                if (tokenIngresado == null || !tokenIngresado.trim().toUpperCase().equals(tokenCorrecto)) {
                    JOptionPane.showMessageDialog(this, "Token incorrecto. Operación abortada.", "Fallo", JOptionPane.ERROR_MESSAGE);
                    return; 
                }
            }
        }

        // ==========================================
        // ACTUALIZACIÓN BD 
        // ==========================================
        String sqlUpdate = "UPDATE solicitudes_prestamo SET estado_solicitud = ?, atendido_por = 'ASISTENTE' WHERE id_solicitud = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
            
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idSolicitudReal);
            ps.executeUpdate();
            System.out.println("✅ BD Actualizada con éxito.");

            // ==========================================
            // GENERACIÓN DE PDF
            // ==========================================
            if (nuevoEstado.equalsIgnoreCase("APROBADO")) {
                System.out.println("⏳ Preparando datos para PDF...");
                try {
                    String nombreAlumno = modelo.getValueAt(filaSeleccionada, 1).toString();
                    String montoStr = modelo.getValueAt(filaSeleccionada, 3).toString().replace("S/.", "").replace(",", "").trim();
                    double montoParaPDF = Double.parseDouble(montoStr);
                    
                    System.out.println("Ejecutando generador...");
                    generarContratoPDF(nombreAlumno, montoParaPDF, 12, idSolicitudReal);
                    System.out.println("✅ Generador finalizó su ejecución.");
                } catch (Throwable e) {
                    System.out.println("🚨 ERROR FATAL AL LLAMAR AL PDF: " + e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "El PDF falló: " + e.getMessage(), "Error CRÍTICO", JOptionPane.ERROR_MESSAGE);
                }
            }

            JOptionPane.showMessageDialog(this, "Solicitud SOL-" + String.format("%03d", idSolicitudReal) + " actualizada a " + nuevoEstado);
            cargarSolicitudesYArbol(); 
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error BD: " + e.getMessage());
        }
    }

    private void generarContratoPDF(String nombreAlumno, double monto, int cuotas, int idSolicitud) {
        System.out.println("➤ Entró al método generarContratoPDF");
        String firmaDigital = SeguridadBCHP.calcularSHA256(nombreAlumno + monto + idSolicitud);
        String nombreArchivo = "Contrato_BCHP_SOL_" + idSolicitud + ".pdf";

        try {
            System.out.println("➤ Creando Documento...");
            com.itextpdf.text.Document documento = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new java.io.FileOutputStream(nombreArchivo));
            documento.open();
            System.out.println("➤ Documento abierto");

            com.itextpdf.text.Font fuenteTitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 18);
            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("SISTEMA BCHP - CONTRATO DE CRÉDITO EDUCATIVO\n\n", fuenteTitulo);
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(titulo);

            com.itextpdf.text.Font fuenteCuerpo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 12);
            String textoContrato = "Por medio del presente documento, se certifica la aprobación y desembolso del crédito universitario " +
                                   "a favor del estudiante: " + nombreAlumno.toUpperCase() + ".\n\n" +
                                   "CONDICIONES DEL FINANCIAMIENTO:\n" +
                                   "- Monto Adjudicado: S/. " + String.format("%.2f", monto) + "\n" +
                                   "- Plazo de Amortización: " + cuotas + " meses.\n" +
                                   "- Fecha de Aprobación: " + java.time.LocalDate.now() + "\n\n" +
                                   "El estudiante se compromete a cumplir con el cronograma de pagos establecido.\n\n" +
                                   "FIRMA DIGITAL Y AUDITORÍA:\n" +
                                   firmaDigital + "\n\n";
            
            documento.add(new com.itextpdf.text.Paragraph(textoContrato, fuenteCuerpo));
            System.out.println("➤ Texto añadido");

            documento.close();
            System.out.println("➤ Documento cerrado y guardado físicamente.");
            
            JOptionPane.showMessageDialog(this, "Contrato PDF generado con éxito:\n" + nombreArchivo, 
                "Generador de Contratos", JOptionPane.INFORMATION_MESSAGE);

        } catch (Throwable e) {
            System.out.println("🚨 ERROR DENTRO DEL PDF: " + e.toString());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error interno iText: " + e.getMessage(), "Error PDF", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void calcularScoreFinanciero() {
        int filaSeleccionada = tabla.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una solicitud.");
            return;
        }

        String idStr = modelo.getValueAt(filaSeleccionada, 0).toString().replace("SOL-", "");
        int idSolicitud = Integer.parseInt(idStr);

        String sql = "SELECT e.nombre, e.apellido, e.promedio_notas, e.porcentaje_asistencia, e.ciclo_actual " +
                     "FROM solicitudes_prestamo s INNER JOIN estudiantes e ON s.id_estudiante = e.id_estudiante " +
                     "WHERE s.id_solicitud = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setInt(1, idSolicitud);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String alumno = rs.getString("nombre") + " " + rs.getString("apellido");
                double promedio = rs.getDouble("promedio_notas");
                double asistencia = rs.getDouble("porcentaje_asistencia");
                int ciclo = rs.getInt("ciclo_actual");

                double ptsPromedio = (promedio / 20.0) * 40.0;     
                double ptsAsistencia = (asistencia / 100.0) * 40.0; 
                double ptsCiclo = 0;
                if (ciclo >= 4 && ciclo <= 7) ptsCiclo = 20.0; 
                else if (ciclo >= 1 && ciclo <= 3) ptsCiclo = 15.0; 
                else if (ciclo >= 8 && ciclo <= 10) ptsCiclo = 10.0; 

                int scoreTotal = (int) Math.round(ptsPromedio + ptsAsistencia + ptsCiclo);

                String dictamen;
                int tipoMensaje;
                if (scoreTotal >= 80) { dictamen = "🟢 EXCELENTE."; tipoMensaje = JOptionPane.INFORMATION_MESSAGE; }
                else if (scoreTotal >= 60) { dictamen = "🟡 ACEPTABLE."; tipoMensaje = JOptionPane.WARNING_MESSAGE; }
                else { dictamen = "🔴 CRÍTICO."; tipoMensaje = JOptionPane.ERROR_MESSAGE; }

                String reporte = "📊 REPORTE DE SCORE FINANCIERO 📊\nEstudiante: " + alumno + "\nPUNTUACIÓN FINAL: " + scoreTotal + " / 100\n" + dictamen;
                JOptionPane.showMessageDialog(this, reporte, "Score", tipoMensaje);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private JButton crearBoton(String t, Color c) {
        JButton b = new JButton(t);
        b.setBackground(c); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setOpaque(true); b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(170, 36));
        return b;
    }

    private class IconoSolicitante implements Icon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(230, 126, 34)); 
            g2.fillOval(x + 4, y + 1, 8, 8); 
            g2.fillArc(x + 1, y + 9, 14, 10, 0, 180); 
            g2.dispose();
        }
        @Override public int getIconWidth() { return 16; }
        @Override public int getIconHeight() { return 16; }
    }

    private class IconoCarpetaAsistente implements Icon {
        private boolean abierta;
        public IconoCarpetaAsistente(boolean abierta) { this.abierta = abierta; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(155, 89, 182)); 
            if (abierta) { g2.fillRect(x + 1, y + 4, 14, 10); } 
            else { g2.fillRect(x + 1, y + 3, 14, 11); }
            g2.dispose();
        }
        @Override public int getIconWidth() { return 16; }
        @Override public int getIconHeight() { return 16; }
    }
}