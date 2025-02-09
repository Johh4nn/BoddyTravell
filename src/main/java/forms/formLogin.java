package forms;

import FireBase.AuthService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class formLogin {
    private JTextField textcorreo;
    private JPasswordField password;
    private JButton ingresarButton;
    private JButton noCuentasConUnaButton;
    public JPanel mainLogin;
    private JLabel imagenLogin;
    private JFrame frame;

    private AuthService authService;

    public formLogin() {
        // Inicializar el servicio de autenticación
        authService = new AuthService();

        // Acción del botón "Ingresar"
        // Acción del botón "Ingresar"
        ingresarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obtener el correo y la contraseña ingresados
                String correo = textcorreo.getText();
                String contrasena = new String(password.getPassword());

                // Autenticar al usuario
                boolean autenticado = authService.loginUser(correo, contrasena);

                if (autenticado) {
                    // Obtener el rol del usuario desde Firestore
                    String rol = authService.getUserRole(correo);

                    System.out.println(rol);

                    // Evitar problemas con valores null
                    if (rol == null || rol.isEmpty()) {
                        rol = "Usuario";  // Si el rol está vacío o es nulo, asignar "Usuario" por defecto
                    }

                    // Mostrar mensaje de éxito
                    JOptionPane.showMessageDialog(mainLogin,
                            "✅ Inicio de sesión exitoso como " + rol,
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Obtener la ventana de login actual
                    JFrame loginFrame = (JFrame) SwingUtilities.getWindowAncestor(mainLogin);

                    // Obtener la posición de la ventana de Login
                    Point loginPosition = (loginFrame != null) ? loginFrame.getLocation() : new Point(100, 100);

                    // Determinar qué formulario abrir según el rol
                    JFrame newFrame;
                    if (rol.equalsIgnoreCase("Administrador")) {
                        // Panel de Administrador
                        newFrame = new JFrame("Panel de Administrador");
                        newFrame.setContentPane(new FormAdministrador(correo).JAdmin);
                    } else {
                        // Panel de Usuario
                        newFrame = new JFrame("Panel de Usuario");
                        newFrame.setContentPane(new formUser(correo).mainPanel);
                    }

                    // Configurar la nueva ventana
                    newFrame.setSize(1280, 720);
                    newFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    newFrame.setLocation(loginPosition); // Establecer la nueva ventana en la misma posición
                    newFrame.setVisible(true);

                    // Cerrar la ventana de login
                    if (loginFrame != null) {
                        loginFrame.dispose();
                    }
                } else {
                    // Mostrar mensaje de error
                    JOptionPane.showMessageDialog(mainLogin, "❌ Correo o contraseña incorrectos.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });


        // Acción del botón "No cuentas con una cuenta, Regístrate"
        noCuentasConUnaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obtener la ventana de login actual
                JFrame loginFrame = (JFrame) SwingUtilities.getWindowAncestor(mainLogin);

                // Obtener la posición de la ventana de Login
                Point loginPosition = (loginFrame != null) ? loginFrame.getLocation() : new Point(100, 100);

                // Abrir la ventana de registro en la misma posición
                JFrame registerFrame = new JFrame("Registro de Usuario");
                registerFrame.setContentPane(new formRegister().mainRegister);
                registerFrame.setSize(1280, 720);
                registerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                registerFrame.setLocation(loginPosition); // Establecer la nueva ventana en la misma posición
                registerFrame.setVisible(true);

                // Cerrar la ventana de login
                if (loginFrame != null) {
                    loginFrame.dispose();
                }
            }
        });

    }

    public void ventanaLogin() {
        frame = new JFrame("Login");
        frame.setContentPane(mainLogin);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 720);
        frame.setLocationRelativeTo(null); // Centra la ventana en la pantalla
        frame.setVisible(true);
    }

}
