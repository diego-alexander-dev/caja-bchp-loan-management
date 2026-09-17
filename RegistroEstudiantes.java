package caja;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.sql.*;

public class RegistroEstudiantes extends JFrame {
    JTextField txtBuscar;
    JTable tabla;
    DefaultTableModel modelo;
    JComboBox<String> comboOrden;
    ArrayList<Estudiante> listaLocal = new ArrayList<>();

    private final boolean soloLectura;

    Color colorPrimario   = new Color(41, 128, 185);
    Color colorSecundario = new Color(23, 33, 43);
    Color colorFondo      = new Color(17, 28, 36);
    Color colorExito      = new Color(46, 204, 113);
    Color colorEditar     = new Color(243, 156, 18);
    Color colorEliminar   = new Color(231, 76, 60);
    Color colorOrdenBoton = new Color(155, 89, 182);

    JTextField txtNombre, txtApellido, txtEdad, txtDni, txtTelefono, txtCorreo;

    public RegistroEstudiantes() {
        this(false);
    }

    public RegistroEstudiantes(boolean soloLectura) {
        this.soloLectura = soloLectura;

        setTitle("BCHP - " + (soloLectura ? "Consulta de Estudiantes" : "Registro de Estudiantes"));
        setSize(1100, 800);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(colorFondo);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel pnlTitulo = new JPanel();
        pnlTitulo.setBackground(colorSecundario);
        pnlTitulo.setPreferredSize(new Dimension(1100, 60));
        JLabel lbl = new JLabel(soloLectura ? "Consulta de Estudiantes del Sistema" : "Gestion de Estudiantes del Sistema");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setForeground(Color.WHITE);
        pnlTitulo.add(lbl);
        add(pnlTitulo, BorderLayout.NORTH);

        JPanel pnlCentral = new JPanel(new BorderLayout(15, 15));
        pnlCentral.setBackground(colorFondo);
        pnlCentral.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        if (!soloLectura) {
            JPanel pnlForm = new JPanel(new GridBagLayout());
            pnlForm.setBackground(colorSecundario);
            pnlForm.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(36, 47, 61)),
                "Datos del Estudiante",
                0, 0, new Font("Segoe UI", Font.BOLD, 13), Color.WHITE));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets  = new Insets(10, 12, 10, 12);
            gbc.fill    = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;

            gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
            pnlForm.add(crearEtiquetaForm("Nombres:"), gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            txtNombre = crearCampoTexto(); pnlForm.add(txtNombre, gbc);

            gbc.gridx = 2; gbc.weightx = 0;
            pnlForm.add(crearEtiquetaForm("Apellidos:"), gbc);
            gbc.gridx = 3; gbc.weightx = 1;
            txtApellido = crearCampoTexto(); pnlForm.add(txtApellido, gbc);

            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
            pnlForm.add(crearEtiquetaForm("DNI (8 digitos):"), gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            txtDni = crearCampoTexto(); pnlForm.add(txtDni, gbc);

            gbc.gridx = 2; gbc.weightx = 0;
            pnlForm.add(crearEtiquetaForm("Edad (18-50):"), gbc);
            gbc.gridx = 3; gbc.weightx = 1;
            txtEdad = crearCampoTexto(); pnlForm.add(txtEdad, gbc);

            gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
            pnlForm.add(crearEtiquetaForm("Telefono (9 dig.):"), gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            txtTelefono = crearCampoTexto(); pnlForm.add(txtTelefono, gbc);

            gbc.gridx = 2; gbc.weightx = 0;
            pnlForm.add(crearEtiquetaForm("Correo:"), gbc);
            gbc.gridx = 3; gbc.weightx = 1;
            txtCorreo = crearCampoTexto(); pnlForm.add(txtCorreo, gbc);

            pnlCentral.add(pnlForm, BorderLayout.NORTH);
        } else {
            JPanel pnlBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            pnlBusqueda.setBackground(colorSecundario);
            pnlBusqueda.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(36, 47, 61)),
                "Buscar Estudiante",
                0, 0, new Font("Segoe UI", Font.BOLD, 13), Color.WHITE));

            JLabel lblBuscar = new JLabel("Nombre / DNI:");
            lblBuscar.setForeground(Color.WHITE);
            lblBuscar.setFont(new Font("Segoe UI", Font.BOLD, 13));
            pnlBusqueda.add(lblBuscar);

            txtBuscar = new JTextField(25);
            txtBuscar.setBackground(colorFondo);
            txtBuscar.setForeground(Color.WHITE);
            txtBuscar.setCaretColor(Color.WHITE);
            txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            txtBuscar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 168, 204)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            pnlBusqueda.add(txtBuscar);

            JButton btnBuscar = crearBoton("Buscar", colorPrimario, 100);
            btnBuscar.addActionListener(e -> buscarEstudiante());
            pnlBusqueda.add(btnBuscar);

            JButton btnMostrarTodos = crearBoton("Ver Todos", new Color(100, 120, 140), 110);
            btnMostrarTodos.addActionListener(e -> { txtBuscar.setText(""); cargarDatosDesdePostgres(); });
            pnlBusqueda.add(btnMostrarTodos);

            pnlCentral.add(pnlBusqueda, BorderLayout.NORTH);
        }

        modelo = new DefaultTableModel() {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        modelo.setColumnIdentifiers(
            new Object[]{"ID", "Nombres", "Apellidos", "Edad", "DNI", "Telefono", "Correo"});

        tabla = new JTable(modelo);
        tabla.setBackground(colorSecundario);
        tabla.setForeground(Color.WHITE);
        tabla.setGridColor(new Color(36, 47, 61));
        tabla.setRowHeight(28);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setSelectionBackground(new Color(41, 128, 185));
        tabla.setSelectionForeground(Color.WHITE);

        JTableHeader header = tabla.getTableHeader();
        header.setBackground(new Color(36, 47, 61));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));

        if (!soloLectura) {
            tabla.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) cargarFilaSeleccionada();
            });
        }

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(colorSecundario);
        pnlCentral.add(scroll, BorderLayout.CENTER);
        add(pnlCentral, BorderLayout.CENTER);

        JPanel pnlInferior = new JPanel(new GridLayout(soloLectura ? 1 : 2, 1));
        pnlInferior.setBackground(colorFondo);

        JPanel pnlOrden = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        pnlOrden.setBackground(colorFondo);
        JLabel lblOrd = new JLabel("Ordenar lista por:");
        lblOrd.setForeground(Color.WHITE);
        lblOrd.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pnlOrden.add(lblOrd);
        comboOrden = new JComboBox<>(new String[]{"ID", "Nombres", "Apellidos", "Edad", "DNI"});
        comboOrden.setPreferredSize(new Dimension(130, 30));
        pnlOrden.add(comboOrden);
        JButton btnBurbuja = crearBoton("Aplicar Orden Burbuja", colorOrdenBoton, 200);
        btnBurbuja.addActionListener(e -> ordenarListaPorBurbuja());
        pnlOrden.add(btnBurbuja);
        pnlInferior.add(pnlOrden);

        if (!soloLectura) {
            JPanel pnlAcciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
            pnlAcciones.setBackground(colorFondo);

            JButton btnAgregar  = crearBoton("Agregar",  colorExito,   150);
            JButton btnEditar   = crearBoton("Editar",   colorEditar,  150);
            JButton btnEliminar = crearBoton("Eliminar", colorEliminar,150);
            JButton btnLimpiar  = crearBoton("Limpiar",  colorPrimario,150);
            JButton btnVolver   = crearBoton("Volver",   new Color(127, 140, 141), 150);

            btnAgregar.addActionListener (e -> insertarEstudianteEnPostgres());
            btnEditar.addActionListener  (e -> editarEstudianteEnPostgres());
            btnEliminar.addActionListener(e -> eliminarEstudianteEnPostgres());
            btnLimpiar.addActionListener (e -> limpiarFormulario());
            btnVolver.addActionListener  (e -> this.dispose());

            pnlAcciones.add(btnAgregar);
            pnlAcciones.add(btnEditar);
            pnlAcciones.add(btnEliminar);
            pnlAcciones.add(btnLimpiar);
            pnlAcciones.add(btnVolver);
            pnlInferior.add(pnlAcciones);
        } else {
            JPanel pnlAcciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
            pnlAcciones.setBackground(colorFondo);
            JLabel lblInfo = new JLabel("Modo consulta: solo lectura. Modificaciones restringidas al Administrador.");
            lblInfo.setForeground(new Color(170, 190, 210));
            lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            pnlAcciones.add(lblInfo);
            JButton btnVolver = crearBoton("Volver", new Color(127, 140, 141), 150);
            btnVolver.addActionListener(e -> this.dispose());
            pnlAcciones.add(btnVolver);
            pnlInferior.add(pnlAcciones);
        }

        add(pnlInferior, BorderLayout.SOUTH);

        cargarDatosDesdePostgres();
        setVisible(true);
    }

    private void cargarDatosDesdePostgres() {
        modelo.setRowCount(0);
        listaLocal.clear();
        String sql = "SELECT id_estudiante, dni, nombre, apellido, edad, telefono, correo " +
                     "FROM estudiantes ORDER BY id_estudiante ASC";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int    id     = rs.getInt("id_estudiante");
                String nom    = rs.getString("nombre");
                String ape    = rs.getString("apellido");
                int    eda    = rs.getInt("edad");
                String dni    = rs.getString("dni");
                String tel    = rs.getString("telefono");
                String correo = rs.getString("correo");
                listaLocal.add(new Estudiante(id, nom, ape, eda, dni, tel, correo));
                modelo.addRow(new Object[]{id, nom, ape, eda, dni, tel, correo});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar: " + ex.getMessage());
        }
    }

    private void buscarEstudiante() {
        String termino = txtBuscar.getText().trim().toUpperCase();
        if (termino.isEmpty()) { cargarDatosDesdePostgres(); return; }
        modelo.setRowCount(0);
        for (Estudiante est : listaLocal) {
            if (est.nombre.toUpperCase().contains(termino)
                    || est.apellido.toUpperCase().contains(termino)
                    || est.dni.contains(termino)) {
                modelo.addRow(new Object[]{est.id, est.nombre, est.apellido, est.edad, est.dni, est.telefono, est.correo});
            }
        }
        if (modelo.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No se encontraron estudiantes con ese criterio.", "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
            cargarDatosDesdePostgres();
        }
    }

    private void cargarFilaSeleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) return;
        if (txtNombre != null)   txtNombre.setText(modelo.getValueAt(fila, 1).toString());
        if (txtApellido != null) txtApellido.setText(modelo.getValueAt(fila, 2).toString());
        if (txtEdad != null)     txtEdad.setText(modelo.getValueAt(fila, 3).toString());
        if (txtDni != null)      txtDni.setText(modelo.getValueAt(fila, 4).toString());
        if (txtTelefono != null) txtTelefono.setText(modelo.getValueAt(fila, 5).toString());
        if (txtCorreo != null)   txtCorreo.setText(modelo.getValueAt(fila, 6).toString());
    }

    private void insertarEstudianteEnPostgres() {
        if (!validarCampos()) return;
        try {
            String nom    = txtNombre.getText().trim().toUpperCase();
            String ape    = txtApellido.getText().trim().toUpperCase();
            String dni    = txtDni.getText().trim();
            String tel    = txtTelefono.getText().trim();
            String correo = txtCorreo.getText().trim();
            int    eda    = Integer.parseInt(txtEdad.getText().trim());

            String sql = "INSERT INTO estudiantes (dni,nombre,apellido,edad,telefono,correo) VALUES (?,?,?,?,?,?)";
            try (Connection con = ConexionBD.conectar();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, dni); ps.setString(2, nom); ps.setString(3, ape);
                ps.setInt(4, eda);   ps.setString(5, tel); ps.setString(6, correo);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Estudiante agregado correctamente.");
                limpiarFormulario();
                cargarDatosDesdePostgres();
            }
        } catch (SQLException ex) {
            if (ex.getMessage().contains("duplicate") || ex.getMessage().contains("unique"))
                JOptionPane.showMessageDialog(this, "Ya existe un estudiante con ese DNI.");
            else
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "La edad debe ser un numero.");
        }
    }

    private void editarEstudianteEnPostgres() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Primero selecciona un estudiante de la tabla.");
            return;
        }
        if (!validarCampos()) return;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Confirmas los cambios para este estudiante?",
            "Confirmar edicion", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int idEstudiante = (int) modelo.getValueAt(fila, 0);
        try {
            String nom    = txtNombre.getText().trim().toUpperCase();
            String ape    = txtApellido.getText().trim().toUpperCase();
            String dni    = txtDni.getText().trim();
            String tel    = txtTelefono.getText().trim();
            String correo = txtCorreo.getText().trim();
            int    eda    = Integer.parseInt(txtEdad.getText().trim());

            String sql = "UPDATE estudiantes SET nombre=?,apellido=?,edad=?,dni=?,telefono=?,correo=? WHERE id_estudiante=?";
            try (Connection con = ConexionBD.conectar();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nom); ps.setString(2, ape); ps.setInt(3, eda);
                ps.setString(4, dni); ps.setString(5, tel); ps.setString(6, correo);
                ps.setInt(7, idEstudiante);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Estudiante actualizado correctamente.");
                limpiarFormulario();
                cargarDatosDesdePostgres();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al editar: " + ex.getMessage());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "La edad debe ser un numero.");
        }
    }

    private void eliminarEstudianteEnPostgres() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Primero selecciona un estudiante de la tabla.");
            return;
        }
        int    idEstudiante = (int) modelo.getValueAt(fila, 0);
        String nombre       = modelo.getValueAt(fila, 1) + " " + modelo.getValueAt(fila, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Eliminar a " + nombre + "?\nSe borraran tambien sus prestamos y pagos.",
            "Confirmar eliminacion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = ConexionBD.conectar()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM pagos WHERE id_prestamo IN (SELECT id_prestamo FROM prestamos WHERE id_estudiante=?)")) {
                ps.setInt(1, idEstudiante); ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM prestamos WHERE id_estudiante=?")) {
                ps.setInt(1, idEstudiante); ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM estudiantes WHERE id_estudiante=?")) {
                ps.setInt(1, idEstudiante); ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Estudiante eliminado correctamente.");
            limpiarFormulario();
            cargarDatosDesdePostgres();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage());
        }
    }

    private boolean validarCampos() {
        String nom  = txtNombre  != null ? txtNombre.getText().trim()  : "";
        String ape  = txtApellido!= null ? txtApellido.getText().trim(): "";
        String dni  = txtDni     != null ? txtDni.getText().trim()     : "";
        String tel  = txtTelefono!= null ? txtTelefono.getText().trim(): "";
        String edaS = txtEdad    != null ? txtEdad.getText().trim()    : "";

        if (nom.isEmpty() || ape.isEmpty() || dni.isEmpty() || tel.isEmpty() || edaS.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios."); return false;
        }
        if (nom.length() <= 2 || ape.length() <= 2) {
            JOptionPane.showMessageDialog(this, "Nombres y Apellidos demasiado cortos."); return false;
        }
        if (!dni.matches("\\d{8}")) {
            JOptionPane.showMessageDialog(this, "El DNI debe tener exactamente 8 digitos."); return false;
        }
        if (!tel.matches("\\d{9}")) {
            JOptionPane.showMessageDialog(this, "El telefono debe tener exactamente 9 digitos."); return false;
        }
        try {
            int eda = Integer.parseInt(edaS);
            if (eda < 18 || eda > 50) {
                JOptionPane.showMessageDialog(this, "Edad permitida: 18 a 50 anos."); return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "La edad debe ser un numero entero."); return false;
        }
        return true;
    }

    private void limpiarFormulario() {
        if (txtNombre   != null) txtNombre.setText("");
        if (txtApellido != null) txtApellido.setText("");
        if (txtDni      != null) txtDni.setText("");
        if (txtEdad     != null) txtEdad.setText("");
        if (txtTelefono != null) txtTelefono.setText("");
        if (txtCorreo   != null) txtCorreo.setText("");
        tabla.clearSelection();
    }

    private void ordenarListaPorBurbuja() {
        int criterio = comboOrden.getSelectedIndex();
        int n = listaLocal.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                Estudiante e1 = listaLocal.get(j), e2 = listaLocal.get(j + 1);
                boolean cambiar = false;
                switch (criterio) {
                    case 0: cambiar = e1.id > e2.id; break;
                    case 1: cambiar = e1.nombre.compareToIgnoreCase(e2.nombre) > 0; break;
                    case 2: cambiar = e1.apellido.compareToIgnoreCase(e2.apellido) > 0; break;
                    case 3: cambiar = e1.edad > e2.edad; break;
                    case 4: cambiar = e1.dni.compareTo(e2.dni) > 0; break;
                }
                if (cambiar) { listaLocal.set(j, e2); listaLocal.set(j + 1, e1); }
            }
        }
        modelo.setRowCount(0);
        for (Estudiante est : listaLocal)
            modelo.addRow(new Object[]{est.id, est.nombre, est.apellido, est.edad, est.dni, est.telefono, est.correo});
    }

    private JLabel crearEtiquetaForm(String texto) {
        JLabel jl = new JLabel(texto);
        jl.setForeground(Color.WHITE);
        jl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return jl;
    }

    private JTextField crearCampoTexto() {
        JTextField jt = new JTextField(18);
        jt.setBackground(colorFondo);
        jt.setForeground(Color.WHITE);
        jt.setCaretColor(Color.WHITE);
        jt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        jt.setPreferredSize(new Dimension(220, 32));
        jt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 168, 204)),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return jt;
    }

    private JButton crearBoton(String t, Color c, int ancho) {
        JButton b = new JButton(t);
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(ancho, 36));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setFocusPainted(false);
        return b;
    }
}