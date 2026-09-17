package caja;

import javax.swing.*;
import java.awt.*;

public class Login extends JFrame {
    private JTextField txtUsuario;
    private JPasswordField txtPass;
    private int intentosFallidos = 0;
    private static final int MAX_INTENTOS = 3;

    public Login() {
        setTitle("BCHP - Acceso Seguro");
        setSize(850, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new GridLayout(1, 2));

        JPanel pnlIzquierdo = new JPanel(new GridBagLayout());
        pnlIzquierdo.setBackground(new Color(11, 35, 72));

        JLabel lblLogo = new JLabel("BCHP");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 54));
        lblLogo.setForeground(Color.WHITE);

        JLabel lblSlogan = new JLabel("Banca de Credito Estudiantil");
        lblSlogan.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSlogan.setForeground(new Color(170, 190, 210));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(5, 0, 5, 0);
        pnlIzquierdo.add(lblLogo, gbc);
        gbc.gridy = 1;
        pnlIzquierdo.add(lblSlogan, gbc);

        JPanel pnlDerecho = new JPanel(new GridBagLayout());
        pnlDerecho.setBackground(new Color(17, 28, 36));
        pnlDerecho.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        JLabel lblIngresar = new JLabel("Ingresar al Sistema");
        lblIngresar.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblIngresar.setForeground(Color.WHITE);

        txtUsuario = new JTextField(15);
        estilizarCampo(txtUsuario);

        txtPass = new JPasswordField(15);
        estilizarCampo(txtPass);

        JButton btnEntrar = new JButton("INGRESAR");
        btnEntrar.setBackground(new Color(0, 168, 204));
        btnEntrar.setForeground(Color.WHITE);
        btnEntrar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnEntrar.setOpaque(true);
        btnEntrar.setBorderPainted(false);
        btnEntrar.setFocusPainted(false);
        btnEntrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEntrar.addActionListener(e -> validarAcceso());

        txtPass.addActionListener(e -> validarAcceso());

        GridBagConstraints gbcD = new GridBagConstraints();
        gbcD.insets = new Insets(10, 0, 10, 0);
        gbcD.fill = GridBagConstraints.HORIZONTAL;

        gbcD.gridx = 0; gbcD.gridy = 0; pnlDerecho.add(lblIngresar, gbcD);
        gbcD.gridy = 1; pnlDerecho.add(crearEtiquetaForm("DNI / Codigo de Usuario"), gbcD);
        gbcD.gridy = 2; pnlDerecho.add(txtUsuario, gbcD);
        gbcD.gridy = 3; pnlDerecho.add(crearEtiquetaForm("Contrasena"), gbcD);
        gbcD.gridy = 4; pnlDerecho.add(txtPass, gbcD);
        gbcD.gridy = 5; pnlDerecho.add(Box.createVerticalStrut(15), gbcD);
        gbcD.gridy = 6; pnlDerecho.add(btnEntrar, gbcD);

        add(pnlIzquierdo);
        add(pnlDerecho);
    }

    private void estilizarCampo(JTextField campo) {
        campo.setBackground(new Color(25, 41, 53));
        campo.setForeground(Color.WHITE);
        campo.setCaretColor(Color.WHITE);
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 168, 204)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private JLabel crearEtiquetaForm(String texto) {
        JLabel jl = new JLabel(texto);
        jl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        jl.setForeground(new Color(120, 144, 156));
        return jl;
    }

    private void validarAcceso() {
        if (intentosFallidos >= MAX_INTENTOS) {
            JOptionPane.showMessageDialog(this,
                "Acceso bloqueado por multiples intentos fallidos.\nReinicie el sistema.",
                "Acceso Bloqueado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String user = txtUsuario.getText().trim();
        String pass = new String(txtPass.getPassword());

        if (user.equals("admin") && pass.equals("1234")) {
            intentosFallidos = 0;
            new MenuPrincipal("ADMINISTRADOR", user);
            this.dispose();

        } else if (user.equals("asistente") && pass.equals("1234")) {
            intentosFallidos = 0;
            JOptionPane.showMessageDialog(this,
                "Bienvenido/a, Asistente BCHP.\nTu sesion tiene acceso a Estudiantes, Prestamos y Pagos.",
                "Acceso Asistente", JOptionPane.INFORMATION_MESSAGE);
            new MenuPrincipal("ASISTENTE", user);
            this.dispose();

        } else if (user.toUpperCase().startsWith("U") && pass.equals("123")) {
            intentosFallidos = 0;
            new PortalEstudiante(user.toUpperCase()).setVisible(true);
            this.dispose();

        } else {
            intentosFallidos++;
            int restantes = MAX_INTENTOS - intentosFallidos;
            String msg = restantes > 0
                ? "Credenciales incorrectas. Intentos restantes: " + restantes
                : "Has agotado los intentos permitidos. Acceso bloqueado.";
            JOptionPane.showMessageDialog(this, msg, "Error de Autenticacion", JOptionPane.ERROR_MESSAGE);
            txtPass.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}

