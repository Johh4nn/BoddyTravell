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
    private JPanel mianLoginImagen;
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

                // Mostrar mensaje de éxito o error
                if (autenticado) {
                    JOptionPane.showMessageDialog(mainLogin, "✅ Inicio de sesión exitoso.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(mainLogin, "❌ Correo o contraseña incorrectos.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Acción del botón "No cuentas con una"
        noCuentasConUnaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Aquí puedes abrir una ventana de registro
                JOptionPane.showMessageDialog(mainLogin, "Redirigiendo al formulario de registro...", "Registro", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public void ventanaLogin(){
        frame = new JFrame("Login");
        frame.setContentPane(mainLogin);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800,600);
        frame.setVisible(true);
    }


}