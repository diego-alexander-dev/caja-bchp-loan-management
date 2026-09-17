package caja;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Controlmorosidad extends JFrame {

    private DefaultTableModel modeloTabla;
    private JTable tablaDeudas;
    private JComboBox<String> comboVista;
    private JComboBox<String> comboOrden;
    private JComboBox<String> comboAlgoritmo;
    private JLabel lblEstado;

    private Colamorosidad colaNotificaciones;

    private static final Color COLOR_BG      = new Color(17, 28, 36);
    private static final Color COLOR_PANEL   = new Color(23, 33, 43);
    private static final Color COLOR_TABLA   = new Color(30, 42, 54);
    private static final Color COLOR_HEADER  = new Color(36, 47, 61);
    private static final Color COLOR_TURQUESA = new Color(0, 168, 204);
    private static final Color COLOR_VERDE   = new Color(39, 174, 96);
    private static final Color COLOR_ROJO    = new Color(231, 76, 60);
    private static final Color COLOR_NARANJA = new Color(230, 126, 34);
    private static final Color COLOR_AMARILLO = new Color(241, 196, 15);

    public Controlmorosidad() {
        colaNotificaciones = new Colamorosidad();

        setTitle("BCHP - Control de Morosidad");
        setSize(1200, 700);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BG);

        construirUI();
        cargarVista();
        setVisible(true);
    }

    private void construirUI() {
        JPanel pnlTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        pnlTitulo.setBackground(COLOR_HEADER);

        JLabel lblTitulo = new JLabel("Control de Morosidad y Deudas");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);

        lblEstado = new JLabel("Cargando...");
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEstado.setForeground(new Color(170, 190, 210));

        pnlTitulo.add(lblTitulo);
        pnlTitulo.add(lblEstado);
        add(pnlTitulo, BorderLayout.NORTH);

        JPanel pnlCentro = new JPanel(new BorderLayout());
        pnlCentro.setBackground(COLOR_PANEL);
        pnlCentro.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modeloTabla = new DefaultTableModel(new String[]{
            "ID Prestamo", "Estudiante", "DNI",
            "Monto Original", "Saldo Actual", "Dias Mora",
            "Interes Mora", "Estado", "Nivel Riesgo"
        }, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tablaDeudas = new JTable(modeloTabla);
        tablaDeudas.setBackground(COLOR_TABLA);
        tablaDeudas.setForeground(Color.WHITE);
        tablaDeudas.setGridColor(COLOR_HEADER);
        tablaDeudas.setRowHeight(32);
        tablaDeudas.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaDeudas.setSelectionBackground(new Color(0, 100, 130));
        tablaDeudas.setSelectionForeground(Color.WHITE);

        tablaDeudas.getColumnModel().getColumn(7).setCellRenderer(new EstadoMoraRenderer());
        tablaDeudas.getColumnModel().getColumn(8).setCellRenderer(new NivelRiesgoRenderer());

        JTableHeader header = tablaDeudas.getTableHeader();
        header.setBackground(COLOR_HEADER);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));

        tablaDeudas.getColumnModel().getColumn(0).setPreferredWidth(80);
        tablaDeudas.getColumnModel().getColumn(1).setPreferredWidth(160);
        tablaDeudas.getColumnModel().getColumn(2).setPreferredWidth(80);
        tablaDeudas.getColumnModel().getColumn(3).setPreferredWidth(110);
        tablaDeudas.getColumnModel().getColumn(4).setPreferredWidth(110);
        tablaDeudas.getColumnModel().getColumn(5).setPreferredWidth(80);
        tablaDeudas.getColumnModel().getColumn(6).setPreferredWidth(100);
        tablaDeudas.getColumnModel().getColumn(7).setPreferredWidth(100);
        tablaDeudas.getColumnModel().getColumn(8).setPreferredWidth(100);

        pnlCentro.add(new JScrollPane(tablaDeudas), BorderLayout.CENTER);
        add(pnlCentro, BorderLayout.CENTER);

        JPanel pnlSur = new JPanel();
        pnlSur.setBackground(COLOR_HEADER);
        pnlSur.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 12));

        JLabel lblVista = new JLabel("Vista:");
        lblVista.setForeground(Color.WHITE);
        lblVista.setFont(new Font("Segoe UI", Font.BOLD, 12));

        comboVista = new JComboBox<>(new String[]{
            "Deudas Activas",
            "Deudas Vencidas",
            "Estudiantes Morosos"
        });
        estilizarCombo(comboVista);
        comboVista.addActionListener(e -> cargarVista());

        JLabel lblOrden = new JLabel("Ordenar por:");
        lblOrden.setForeground(Color.WHITE);
        lblOrden.setFont(new Font("Segoe UI", Font.BOLD, 12));

        comboOrden = new JComboBox<>(new String[]{
            "Mayor deuda",
            "Menor deuda",
            "Mas dias de mora",
            "Nombre",
            "Fecha de inicio"
        });
        estilizarCombo(comboOrden);

        JLabel lblAlg = new JLabel("Algoritmo:");
        lblAlg.setForeground(Color.WHITE);
        lblAlg.setFont(new Font("Segoe UI", Font.BOLD, 12));

        comboAlgoritmo = new JComboBox<>(new String[]{
            "Burbuja",
            "Seleccion",
            "Insercion",
            "Merge Sort",
            "Quick Sort",
            "Heap Sort"
        });
        estilizarCombo(comboAlgoritmo);

        JButton btnOrdenar      = crearBoton("Ordenar",         COLOR_TURQUESA);
        JButton btnAplicarMora  = crearBoton("Aplicar Mora",    COLOR_NARANJA);
        JButton btnNotificar    = crearBoton("Notificar",       COLOR_AMARILLO);
        JButton btnProcesarCola = crearBoton("Procesar Cola",   COLOR_VERDE);
        JButton btnRefresh      = crearBoton("Actualizar",      COLOR_PANEL);
        JButton btnVolver       = crearBoton("Volver",          COLOR_ROJO);

        btnOrdenar.addActionListener(e -> ordenarTabla());
        btnAplicarMora.addActionListener(e -> aplicarMora());
        btnNotificar.addActionListener(e -> encolarNotificacion());
        btnProcesarCola.addActionListener(e -> procesarCola());
        btnRefresh.addActionListener(e -> cargarVista());
        btnVolver.addActionListener(e -> this.dispose());

        pnlSur.add(lblVista);
        pnlSur.add(comboVista);
        pnlSur.add(lblOrden);
        pnlSur.add(comboOrden);
        pnlSur.add(lblAlg);
        pnlSur.add(comboAlgoritmo);
        pnlSur.add(btnOrdenar);
        pnlSur.add(btnAplicarMora);
        pnlSur.add(btnNotificar);
        pnlSur.add(btnProcesarCola);
        pnlSur.add(btnRefresh);
        pnlSur.add(btnVolver);

        add(pnlSur, BorderLayout.SOUTH);
    }

    private void cargarVista() {
        int vista = comboVista.getSelectedIndex();
        modeloTabla.setRowCount(0);

        if (vista == 2) {
            cargarEstudiantesMorosos();
        } else {
            cargarPrestamos(vista);
        }
    }

    private void cargarPrestamos(int vistaIndex) {
        String condicion = vistaIndex == 0
            ? "p.estado = 'Activo' AND p.saldo > 0"
            : "p.estado = 'Activo' AND p.saldo > 0 AND p.dias_mora > 0";

        String sql = "SELECT p.id_prestamo, e.nombre || ' ' || e.apellido AS estudiante, e.dni, " +
                     "p.monto, p.saldo, p.dias_mora, p.interes_mora, p.estado, p.fecha_inicio " +
                     "FROM prestamos p " +
                     "JOIN estudiantes e ON p.id_estudiante = e.id_estudiante " +
                     "WHERE " + condicion + " ORDER BY p.id_prestamo ASC";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int total = 0;
            while (rs.next()) {
                int dias = rs.getInt("dias_mora");
                double saldo = rs.getDouble("saldo");
                String estadoMora = calcularEstadoMora(dias);
                String nivelRiesgo = calcularNivelRiesgo(dias, saldo);

                modeloTabla.addRow(new Object[]{
                    "P-" + String.format("%03d", rs.getInt("id_prestamo")),
                    rs.getString("estudiante"),
                    rs.getString("dni"),
                    "S/. " + String.format("%.2f", rs.getDouble("monto")),
                    "S/. " + String.format("%.2f", saldo),
                    dias,
                    "S/. " + String.format("%.2f", rs.getDouble("interes_mora")),
                    estadoMora,
                    nivelRiesgo
                });
                total++;
            }
            lblEstado.setText("Registros: " + total);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos: " + e.getMessage(),
                "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarEstudiantesMorosos() {
        modeloTabla.setColumnIdentifiers(new String[]{
            "Estudiante", "DNI", "Saldo Pendiente",
            "Dias de Mora", "Interes Mora", "Nivel Riesgo", "", "", ""
        });

        String sql = "SELECT e.nombre || ' ' || e.apellido AS estudiante, e.dni, " +
                     "SUM(p.saldo) AS saldo_total, MAX(p.dias_mora) AS max_mora, " +
                     "SUM(p.interes_mora) AS total_interes " +
                     "FROM prestamos p " +
                     "JOIN estudiantes e ON p.id_estudiante = e.id_estudiante " +
                     "WHERE p.dias_mora > 0 AND p.estado = 'Activo' " +
                     "GROUP BY e.id_estudiante, e.nombre, e.apellido, e.dni " +
                     "ORDER BY max_mora DESC";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int total = 0;
            while (rs.next()) {
                int dias = rs.getInt("max_mora");
                double saldo = rs.getDouble("saldo_total");
                String nivel = calcularNivelRiesgo(dias, saldo);

                modeloTabla.addRow(new Object[]{
                    rs.getString("estudiante"),
                    rs.getString("dni"),
                    "S/. " + String.format("%.2f", saldo),
                    dias,
                    "S/. " + String.format("%.2f", rs.getDouble("total_interes")),
                    nivel,
                    "", "", ""
                });
                total++;
            }
            lblEstado.setText("Morosos: " + total);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar morosos: " + e.getMessage(),
                "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aplicarMora() {
        int fila = tablaDeudas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un prestamo de la tabla.",
                "Sin seleccion", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String idStr = modeloTabla.getValueAt(fila, 0).toString().replace("P-", "").trim();
        int idPrestamo;
        try {
            idPrestamo = Integer.parseInt(idStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo obtener el ID del prestamo.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String inputDias = JOptionPane.showInputDialog(this,
            "Ingresa los dias de retraso para el prestamo P-" + String.format("%03d", idPrestamo) + ":",
            "Aplicar Mora", JOptionPane.PLAIN_MESSAGE);

        if (inputDias == null || inputDias.trim().isEmpty()) return;

        int dias;
        try {
            dias = Integer.parseInt(inputDias.trim());
            if (dias < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingresa un numero de dias valido (>= 0).",
                "Valor invalido", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sqlSaldo = "SELECT saldo FROM prestamos WHERE id_prestamo = ?";
        double saldoActual = 0;
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sqlSaldo)) {
            ps.setInt(1, idPrestamo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) saldoActual = rs.getDouble("saldo");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al obtener saldo: " + e.getMessage(),
                "Error BD", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double recargo = saldoActual * 0.02;
        double nuevoSaldo = saldoActual + recargo;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Prestamo: P-" + String.format("%03d", idPrestamo) + "\n" +
            "Saldo actual: S/. " + String.format("%.2f", saldoActual) + "\n" +
            "Recargo (2%): S/. " + String.format("%.2f", recargo) + "\n" +
            "Nuevo saldo:  S/. " + String.format("%.2f", nuevoSaldo) + "\n" +
            "Dias de mora: " + dias + "\n\n" +
            "Confirmar aplicacion de mora?",
            "Confirmar Mora", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        String sqlUpdate = "UPDATE prestamos SET saldo = ?, dias_mora = ?, interes_mora = ? WHERE id_prestamo = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
            ps.setDouble(1, nuevoSaldo);
            ps.setInt(2, dias);
            ps.setDouble(3, recargo);
            ps.setInt(4, idPrestamo);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,
                "Mora aplicada correctamente.\nNuevo saldo: S/. " + String.format("%.2f", nuevoSaldo),
                "Mora Aplicada", JOptionPane.INFORMATION_MESSAGE);
            cargarVista();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al aplicar mora: " + e.getMessage(),
                "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void encolarNotificacion() {
        int fila = tablaDeudas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un prestamo para notificar.",
                "Sin seleccion", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String idStr = modeloTabla.getValueAt(fila, 0).toString().replace("P-", "").trim();
        String estudiante = modeloTabla.getValueAt(fila, 1).toString();
        String saldo = modeloTabla.getValueAt(fila, 4).toString();

        int idPrestamo;
        try {
            idPrestamo = Integer.parseInt(idStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo obtener el ID del prestamo.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String mensaje = "Estimado " + estudiante + ":\n\n" +
            "Su prestamo presenta retraso en los pagos.\n" +
            "Saldo pendiente: " + saldo + "\n\n" +
            "Regularice su situacion para evitar mayores recargos.";

        colaNotificaciones.encolar(idPrestamo, estudiante, mensaje);
        colaNotificaciones.mostrarCola();

        JOptionPane.showMessageDialog(this,
            "Notificacion encolada para: " + estudiante + "\n" +
            "Pendientes en cola: " + colaNotificaciones.size(),
            "Encolado", JOptionPane.INFORMATION_MESSAGE);
    }

    private void procesarCola() {
        if (colaNotificaciones.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "La cola de notificaciones esta vacia.",
                "Cola vacia", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder resumen = new StringBuilder();
        resumen.append("Procesando cola FIFO de notificaciones:\n\n");
        int procesados = 0;

        while (!colaNotificaciones.isEmpty()) {
            String notif = colaNotificaciones.desencolar();
            resumen.append(">> ").append(notif, 0, Math.min(60, notif.length())).append("...\n");
            procesados++;
        }

        resumen.append("\nTotal procesados: ").append(procesados);
        JOptionPane.showMessageDialog(this, resumen.toString(),
            "Cola Procesada", JOptionPane.INFORMATION_MESSAGE);
    }

    private void ordenarTabla() {
        int criterio = comboOrden.getSelectedIndex();
        int algoritmo = comboAlgoritmo.getSelectedIndex();

        int filas = modeloTabla.getRowCount();
        if (filas < 2) return;

        List<Object[]> datos = new ArrayList<>();
        for (int i = 0; i < filas; i++) {
            Object[] fila = new Object[modeloTabla.getColumnCount()];
            for (int j = 0; j < modeloTabla.getColumnCount(); j++) {
                fila[j] = modeloTabla.getValueAt(i, j);
            }
            datos.add(fila);
        }

        switch (algoritmo) {
            case 0: ordenBurbuja(datos, criterio);    break;
            case 1: ordenSeleccion(datos, criterio);  break;
            case 2: ordenInsercion(datos, criterio);  break;
            case 3: mergeSort(datos, 0, datos.size() - 1, criterio); break;
            case 4: quickSort(datos, 0, datos.size() - 1, criterio); break;
            case 5: heapSort(datos, criterio);        break;
        }

        modeloTabla.setRowCount(0);
        for (Object[] fila : datos) {
            modeloTabla.addRow(fila);
        }

        String[] algoritmos = {"Burbuja", "Seleccion", "Insercion", "Merge Sort", "Quick Sort", "Heap Sort"};
        String[] criterios = {"Mayor deuda", "Menor deuda", "Mas dias de mora", "Nombre", "Fecha de inicio"};
        lblEstado.setText("Ordenado por: " + criterios[criterio] + " usando " + algoritmos[algoritmo]);
    }

    private double valorComparacion(Object[] fila, int criterio) {
        try {
            switch (criterio) {
                case 0: return -parseSoles(fila[4]);
                case 1: return  parseSoles(fila[4]);
                case 2: return -(fila[5] instanceof Integer ? (Integer) fila[5] : 0);
                case 3: return  fila[1].toString().charAt(0);
                case 4: return  0;
                default: return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseSoles(Object val) {
        if (val == null) return 0;
        String s = val.toString().replace("S/.", "").replace(",", "").trim();
        try { return Double.parseDouble(s); } catch (Exception e) { return 0; }
    }

    private void ordenBurbuja(List<Object[]> datos, int criterio) {
        int n = datos.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (valorComparacion(datos.get(j), criterio) > valorComparacion(datos.get(j + 1), criterio)) {
                    Object[] tmp = datos.get(j);
                    datos.set(j, datos.get(j + 1));
                    datos.set(j + 1, tmp);
                }
            }
        }
    }

    private void ordenSeleccion(List<Object[]> datos, int criterio) {
        int n = datos.size();
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (valorComparacion(datos.get(j), criterio) < valorComparacion(datos.get(minIdx), criterio)) {
                    minIdx = j;
                }
            }
            Object[] tmp = datos.get(minIdx);
            datos.set(minIdx, datos.get(i));
            datos.set(i, tmp);
        }
    }

    private void ordenInsercion(List<Object[]> datos, int criterio) {
        int n = datos.size();
        for (int i = 1; i < n; i++) {
            Object[] key = datos.get(i);
            double keyVal = valorComparacion(key, criterio);
            int j = i - 1;
            while (j >= 0 && valorComparacion(datos.get(j), criterio) > keyVal) {
                datos.set(j + 1, datos.get(j));
                j--;
            }
            datos.set(j + 1, key);
        }
    }

    private void mergeSort(List<Object[]> datos, int izq, int der, int criterio) {
        if (izq < der) {
            int mid = (izq + der) / 2;
            mergeSort(datos, izq, mid, criterio);
            mergeSort(datos, mid + 1, der, criterio);
            merge(datos, izq, mid, der, criterio);
        }
    }

    private void merge(List<Object[]> datos, int izq, int mid, int der, int criterio) {
        List<Object[]> izqList = new ArrayList<>(datos.subList(izq, mid + 1));
        List<Object[]> derList = new ArrayList<>(datos.subList(mid + 1, der + 1));
        int i = 0, j = 0, k = izq;
        while (i < izqList.size() && j < derList.size()) {
            if (valorComparacion(izqList.get(i), criterio) <= valorComparacion(derList.get(j), criterio)) {
                datos.set(k++, izqList.get(i++));
            } else {
                datos.set(k++, derList.get(j++));
            }
        }
        while (i < izqList.size()) datos.set(k++, izqList.get(i++));
        while (j < derList.size()) datos.set(k++, derList.get(j++));
    }

    private void quickSort(List<Object[]> datos, int bajo, int alto, int criterio) {
        if (bajo < alto) {
            int pi = particion(datos, bajo, alto, criterio);
            quickSort(datos, bajo, pi - 1, criterio);
            quickSort(datos, pi + 1, alto, criterio);
        }
    }

    private int particion(List<Object[]> datos, int bajo, int alto, int criterio) {
        double pivot = valorComparacion(datos.get(alto), criterio);
        int i = bajo - 1;
        for (int j = bajo; j < alto; j++) {
            if (valorComparacion(datos.get(j), criterio) <= pivot) {
                i++;
                Object[] tmp = datos.get(i);
                datos.set(i, datos.get(j));
                datos.set(j, tmp);
            }
        }
        Object[] tmp = datos.get(i + 1);
        datos.set(i + 1, datos.get(alto));
        datos.set(alto, tmp);
        return i + 1;
    }

    private void heapSort(List<Object[]> datos, int criterio) {
        int n = datos.size();
        for (int i = n / 2 - 1; i >= 0; i--) heapify(datos, n, i, criterio);
        for (int i = n - 1; i > 0; i--) {
            Object[] tmp = datos.get(0);
            datos.set(0, datos.get(i));
            datos.set(i, tmp);
            heapify(datos, i, 0, criterio);
        }
    }

    private void heapify(List<Object[]> datos, int n, int i, int criterio) {
        int mayor = i, izq = 2 * i + 1, der = 2 * i + 2;
        if (izq < n && valorComparacion(datos.get(izq), criterio) > valorComparacion(datos.get(mayor), criterio)) mayor = izq;
        if (der < n && valorComparacion(datos.get(der), criterio) > valorComparacion(datos.get(mayor), criterio)) mayor = der;
        if (mayor != i) {
            Object[] tmp = datos.get(i);
            datos.set(i, datos.get(mayor));
            datos.set(mayor, tmp);
            heapify(datos, n, mayor, criterio);
        }
    }

    private String calcularEstadoMora(int dias) {
        if (dias == 0)        return "AL DIA";
        else if (dias <= 15)  return "VENCIDO";
        else if (dias <= 30)  return "MOROSO";
        else                  return "CRITICO";
    }

    private String calcularNivelRiesgo(int dias, double saldo) {
        if (dias == 0)                        return "BAJO";
        else if (dias <= 15 && saldo < 2000)  return "MEDIO";
        else if (dias <= 30)                  return "ALTO";
        else                                  return "CRITICO";
    }

    private void estilizarCombo(JComboBox<String> combo) {
        combo.setBackground(COLOR_TABLA);
        combo.setForeground(Color.WHITE);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    }

    private JButton crearBoton(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(135, 36));
        return btn;
    }

    private class EstadoMoraRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            String txt = v != null ? v.toString() : "";
            switch (txt) {
                case "AL DIA":   setForeground(COLOR_VERDE);    break;
                case "VENCIDO":  setForeground(COLOR_AMARILLO); break;
                case "MOROSO":   setForeground(COLOR_NARANJA);  break;
                case "CRITICO":  setForeground(COLOR_ROJO);     break;
                default:         setForeground(Color.WHITE);    break;
            }
            setBackground(sel ? new Color(0, 100, 130) : COLOR_TABLA);
            return this;
        }
    }

    private class NivelRiesgoRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            String txt = v != null ? v.toString() : "";
            switch (txt) {
                case "BAJO":    setForeground(COLOR_VERDE);    break;
                case "MEDIO":   setForeground(COLOR_AMARILLO); break;
                case "ALTO":    setForeground(COLOR_NARANJA);  break;
                case "CRITICO": setForeground(COLOR_ROJO);     break;
                default:        setForeground(Color.WHITE);    break;
            }
            setBackground(sel ? new Color(0, 100, 130) : COLOR_TABLA);
            return this;
        }
    }
}