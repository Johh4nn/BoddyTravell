package forms;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.api.core.ApiFuture;
import org.example.Conexion;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class formRegister extends JFrame {
    public JPanel mainRegister;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField5;
    private JTextField textField6;
    private JPasswordField passwordField2;
    private JComboBox<String> comboBox2;
    private JButton enviarButton;
    private JButton regresarButton;
    private FirebaseAuth auth;
    private Firestore db;

    public formRegister() {
        setTitle("Registro de Usuario");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setContentPane(mainRegister);

        // Conectar Firebase
        Conexion.conectarFirebase();
        auth = FirebaseAuth.getInstance();
        db = Conexion.db;

        if (db == null) {
            JOptionPane.showMessageDialog(this, "❌ Error: No se pudo conectar a Firestore.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        enviarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarUsuario();
            }
        });

        // Acción para el botón "Regresar"
        regresarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cerrarVentana(); // Cierra la ventana de registro antes de abrir login
                abrirLogin(); // Abre la ventana de login
            }
        });
    }

    private void registrarUsuario() {
        String nombre = textField1.getText().trim();
        String apellido = textField5.getText().trim();
        String nombreUsuario = textField2.getText().trim();
        String email = textField6.getText().trim();
        String password = new String(passwordField2.getPassword());
        String confirmPassword = new String(passwordField2.getPassword());
        String role = (String) comboBox2.getSelectedItem();

        if (nombre.isEmpty() || apellido.isEmpty() || nombreUsuario.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Por favor, complete todos los campos", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "⚠️ Las contraseñas no coinciden", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            UserRecord existingUser = auth.getUserByEmail(email);
            if (existingUser != null) {
                JOptionPane.showMessageDialog(this, "⚠️ El correo ya está registrado.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (Exception ignored) {
            // Si no está registrado, se continúa
        }

        try {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password);

            UserRecord userRecord = auth.createUser(request);
            String uid = userRecord.getUid();

            Map<String, Object> userData = new HashMap<>();
            userData.put("nombre", nombre);
            userData.put("apellido", apellido);
            userData.put("nombreUsuario", nombreUsuario);
            userData.put("email", email);
            userData.put("rol", role);

            ApiFuture<WriteResult> future = db.collection("users").document(uid).set(userData);
            future.get();

            JOptionPane.showMessageDialog(this, "✅ Registro exitoso!", "Éxito", JOptionPane.INFORMATION_MESSAGE);

            cerrarVentana(); // Cierra la ventana de registro
            abrirLogin(); // Abre el login automáticamente

        } catch (InterruptedException | ExecutionException e) {
            JOptionPane.showMessageDialog(this, "❌ Error en Firestore: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Error en Firebase Authentication: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para cerrar la ventana de registro correctamente
    private void cerrarVentana() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(mainRegister);
            if (frame != null) {
                frame.dispose(); // Cierra la ventana actual correctamente
            } else {
                dispose(); // Alternativa para asegurarse de que la ventana se cierre
            }
        });
    }

    // Método para abrir el formulario de Login
    private void abrirLogin() {
        SwingUtilities.invokeLater(() -> {
            formLogin loginForm = new formLogin();
            loginForm.ventanaLogin(); // Llama al método que abre la ventana de login
        });
    }
}
