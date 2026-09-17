package caja;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MenuPrincipal extends JFrame {

    private String rolActual;
    private String nombreUsuario;
    private JButton btnEstudiantes, btnPrestamos, btnPagos, btnMonitoreo;
    private JButton btnEvalSolicitudes, btnMorosidad, btnVolver;
    private JLabel lblModuloActual;

    private PilaNavegacion historialNavegacion;

    public MenuPrincipal(String rol, String usuario) {
        this.rolActual           = rol;
        this.nombreUsuario       = usuario;
        this.historialNavegacion = new PilaNavegacion();
        historialNavegacion.push("MenuPrincipal", this);

        setTitle("BCHP System - Control Panel");
        setSize(1150, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(23, 33, 43));
        sidebar.setPreferredSize(new Dimension(280, 720));

        JLabel lblLogo = new JLabel("  BCHP System");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
        sidebar.add(lblLogo);

        String textoEstudiantes = rolActual.equals("ASISTENTE")
            ? "  Consultar Estudiantes"
            : "  Registro de Estudiantes";

        btnEstudiantes = crearBotonMenu(textoEstudiantes);
        btnEstudiantes.addActionListener(e -> {
            boolean soloLectura = rolActual.equals("ASISTENTE");
            abrirModulo("RegistroEstudiantes", new RegistroEstudiantes(soloLectura));
        });

        btnPrestamos = crearBotonMenu("  Gestionar Prestamos");
        btnPrestamos.addActionListener(e -> abrirModulo("RegistroPrestamos", new RegistroPrestamos()));

        btnPagos = crearBotonMenu("  Procesar Amortizaciones");
        btnPagos.addActionListener(e -> abrirModulo("RegistroPagos", new RegistroPagos()));

        btnMonitoreo = crearBotonMenu("  Monitoreo Academico");
        btnMonitoreo.addActionListener(e -> abrirModulo("VentanaMonitoreo", new VentanaMonitoreo()));

        btnEvalSolicitudes = crearBotonMenu("  Evaluacion Solicitudes");
        btnEvalSolicitudes.setForeground(new Color(0, 168, 204));
        btnEvalSolicitudes.addActionListener(e -> abrirModulo("EvaluacionSolicitudes", new EvaluacionSolicitudes()));

        btnMorosidad = crearBotonMenu("  Control Morosidad");
        btnMorosidad.setForeground(new Color(230, 126, 34));
        btnMorosidad.addActionListener(e -> abrirModulo("ControlMorosidad", new Controlmorosidad()));

        btnVolver = crearBotonMenu("  <- Volver");
        btnVolver.addActionListener(e -> volverModuloAnterior());

        sidebar.add(btnEstudiantes);
        sidebar.add(btnPrestamos);
        sidebar.add(btnPagos);
        sidebar.add(btnMonitoreo);
        sidebar.add(btnEvalSolicitudes);
        sidebar.add(btnMorosidad);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(btnVolver);
        sidebar.add(Box.createVerticalGlue());

        JButton btnSalir = crearBotonMenu("  Cerrar Sesion");
        btnSalir.addActionListener(e -> {
            historialNavegacion.vaciar();
            new Login().setVisible(true);
            this.dispose();
        });
        sidebar.add(btnSalir);

        add(sidebar, BorderLayout.WEST);

        JPanel pnlCentro = new JPanel(new GridBagLayout());
        pnlCentro.setBackground(new Color(17, 28, 36));

        JLabel welcome = new JLabel("Bienvenido, " + nombreUsuario.toUpperCase());
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 38));
        welcome.setForeground(Color.WHITE);

        JLabel info = new JLabel("Nivel de Acceso: " + rolActual);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        info.setForeground(new Color(0, 168, 204));

        lblModuloActual = new JLabel("Modulo actual: MenuPrincipal");
        lblModuloActual.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblModuloActual.setForeground(new Color(100, 120, 140));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 0);

        gbc.gridx = 0; gbc.gridy = 0; pnlCentro.add(welcome, gbc);
        gbc.gridy = 1; pnlCentro.add(Box.createVerticalStrut(10), gbc);
        gbc.gridy = 2; pnlCentro.add(info, gbc);
        gbc.gridy = 3; pnlCentro.add(lblModuloActual, gbc);

        add(pnlCentro, BorderLayout.CENTER);

        aplicarPermisos();
        setVisible(true);
    }

    private void abrirModulo(String nombre, JFrame ventana) {
        ventana.setVisible(true);
        historialNavegacion.push(nombre, ventana);
        actualizarLblModulo();
        historialNavegacion.mostrarPila();
    }

    private void volverModuloAnterior() {
        if (historialNavegacion.size() <= 1) {
            JOptionPane.showMessageDialog(this,
                "Ya estas en el modulo inicial (MenuPrincipal).",
                "Navegacion", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFrame ventanaActual = historialNavegacion.pop();
        if (ventanaActual != null && ventanaActual != this) {
            ventanaActual.dispose();
        }

        JFrame ventanaAnterior = historialNavegacion.peekVentana();
        if (ventanaAnterior != null && ventanaAnterior != this) {
            ventanaAnterior.setVisible(true);
            ventanaAnterior.toFront();
        }

        actualizarLblModulo();
        historialNavegacion.mostrarPila();
    }

    private void actualizarLblModulo() {
        String actual = historialNavegacion.peekNombre();
        if (actual != null) {
            lblModuloActual.setText("Modulo actual: " + actual);
        }
    }

    private void aplicarPermisos() {
        if (rolActual.equals("ESTUDIANTE")) {
            btnEstudiantes.setVisible(false);
            btnPrestamos.setVisible(false);
            btnPagos.setVisible(false);
            btnMonitoreo.setVisible(false);
            btnEvalSolicitudes.setVisible(false);
            btnMorosidad.setVisible(false);
            btnVolver.setVisible(false);
        } else if (rolActual.equals("ASISTENTE")) {
            btnMonitoreo.setVisible(false);
        }
    }

    private JButton crearBotonMenu(String texto) {
        JButton btn = new JButton(texto);
        btn.setMaximumSize(new Dimension(280, 55));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setBackground(new Color(23, 33, 43));
        btn.setForeground(new Color(200, 214, 229));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(36, 47, 61));
                btn.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(23, 33, 43));
                btn.setForeground(new Color(200, 214, 229));
            }
        });
        return btn;
    }
}