package forms;

import FireBase.AuthService;
import javax.swing.*;
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

                    // Evitar problemas con valores null
                    if (rol == null || rol.isEmpty()) {
                        rol = "Usuario";  // Si el rol está vacío o es nulo, asignar como Usuario por defecto
                    }

                    // Mostrar mensaje de éxito
                    JOptionPane.showMessageDialog(mainLogin,
                            "✅ Inicio de sesión exitoso como " + rol,
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Determinar qué formulario abrir según el rol
                    JFrame newFrame;
                    if (rol.equalsIgnoreCase("Administrador")) {
                        // Panel de Administrador
                        newFrame = new JFrame("Panel de Administrador");
                        newFrame.setContentPane(new FormAdministrador().JAdmin);
                    } else {
                        // Panel de Usuario
                        newFrame = new JFrame("Panel de Usuario");
                        newFrame.setContentPane(new formUser().mainPanel);
                    }

                    // Configurar y mostrar la nueva ventana
                    newFrame.setSize(800, 600);
                    newFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    newFrame.setVisible(true);

                    // Cerrar la ventana de login
                    JFrame loginFrame = (JFrame) SwingUtilities.getWindowAncestor(mainLogin);
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
                // Obtener la ventana actual de login y cerrarla
                JFrame loginFrame = (JFrame) SwingUtilities.getWindowAncestor(mainLogin);
                if (loginFrame != null) {
                    loginFrame.dispose(); // Cierra la ventana de login
                }

                // Abrir la ventana de registro
                JFrame registerFrame = new JFrame("Registro de Usuario");
                formRegister registerForm = new formRegister();  // Instanciar el formulario de registro
                registerFrame.setContentPane(registerForm.mainRegister);
                registerFrame.setSize(800, 600);
                registerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                registerFrame.setVisible(true);
            }
        });
    }

    public void ventanaLogin() {
        frame = new JFrame("Login");
        frame.setContentPane(mainLogin);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}
