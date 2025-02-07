package forms;

import FireBase.FirebaseInit;
import com.google.cloud.firestore.Firestore;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class formLogin {
    private JTextField textcorreo;
    private JPasswordField password;
    private JButton ingresarButton;
    private JButton noCuentasConUnaButton;
    public JPanel mainLogin;
    private JFrame frame;
    private Firestore db;

    public formLogin() {
        // Inicializar Firebase
        FirebaseInit.initialize();
        db = FirebaseInit.getFirestore();

        // Acción del botón "Ingresar"
        ingresarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String correo = textcorreo.getText();
                String contrasena = new String(password.getPassword());

                if (autenticarUsuario(correo, contrasena)) {
                    JOptionPane.showMessageDialog(mainLogin,
                            "✅ Inicio de sesión exitoso.",
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Abrir solo el panel de usuario (se elimina la opción de Administrador)
                    abrirPanelUsuario();
                } else {
                    JOptionPane.showMessageDialog(mainLogin,
                            "❌ Correo o contraseña incorrectos.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Acción del botón "No cuentas con una"
        noCuentasConUnaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame registerFrame = new JFrame("Registro de Usuario");
                registerFrame.setContentPane(new formRegister().mainRegister);
                registerFrame.setSize(400, 300);
                registerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                registerFrame.setVisible(true);
            }
        });
    }

    private boolean autenticarUsuario(String email, String password) {
        // Simulación de autenticación. Reemplázalo con lógica real usando FirebaseAuth si es necesario.
        return email.equals("usuario@example.com") && password.equals("123456");
    }

    private void abrirPanelUsuario() {
        JFrame newFrame = new JFrame("Panel de Usuario");
        newFrame.setContentPane(new formUser().mainPanel);
        newFrame.setSize(800, 600);
        newFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        newFrame.setVisible(true);

        // Cerrar la ventana de login
        JFrame loginFrame = (JFrame) SwingUtilities.getWindowAncestor(mainLogin);
        if (loginFrame != null) {
            loginFrame.dispose();
        }
    }

    public void ventanaLogin() {
        frame = new JFrame("Login");
        frame.setContentPane(mainLogin);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}
