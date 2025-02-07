package forms;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.api.core.ApiFuture;
import org.example.Conexion;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class formRegister extends JFrame {
    public JPanel mainRegister;
    private JTextField textField1, textField2, textField3, textField4;
    private JPasswordField passwordField1;
    private JComboBox<String> comboBox1;
    private JButton enviarButton;
    private FirebaseAuth auth;
    private Firestore db;

    public formRegister() {
        setTitle("REGISTRO");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Conectar Firebase
        Conexion.conectarFirebase();
        auth = FirebaseAuth.getInstance();
        db = Conexion.db;

        if (db == null) {
            JOptionPane.showMessageDialog(this, "❌ Error: No se pudo conectar a Firestore.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Panel de fondo
        JPanel backgroundPanel = new JPanel();
        backgroundPanel.setLayout(null);

        // Título
        JLabel registroLabel = new JLabel("REGISTRO");
        registroLabel.setForeground(Color.BLACK);
        registroLabel.setFont(new Font("Consolas", Font.BOLD, 30));
        registroLabel.setBounds(320, 20, 300, 40);
        backgroundPanel.add(registroLabel);

        // Campos de texto
        agregarCampo("Nombre", 50, 120, backgroundPanel);
        textField1 = new JTextField();
        textField1.setBounds(50, 150, 300, 30);
        backgroundPanel.add(textField1);

        agregarCampo("Apellido", 450, 120, backgroundPanel);
        textField2 = new JTextField();
        textField2.setBounds(450, 150, 300, 30);
        backgroundPanel.add(textField2);

        agregarCampo("Nombre de usuario", 50, 200, backgroundPanel);
        textField3 = new JTextField();
        textField3.setBounds(50, 230, 300, 30);
        backgroundPanel.add(textField3);

        agregarCampo("Correo", 450, 200, backgroundPanel);
        textField4 = new JTextField();
        textField4.setBounds(450, 230, 300, 30);
        backgroundPanel.add(textField4);

        agregarCampo("Contraseña", 50, 280, backgroundPanel);
        passwordField1 = new JPasswordField();
        passwordField1.setBounds(50, 310, 300, 30);
        backgroundPanel.add(passwordField1);

        // ComboBox para el rol
        JLabel tipoLabel = new JLabel("Tipo de usuario");
        tipoLabel.setForeground(Color.BLACK);
        tipoLabel.setFont(new Font("Consolas", Font.BOLD, 18));
        tipoLabel.setBounds(50, 360, 200, 30);
        backgroundPanel.add(tipoLabel);

        comboBox1 = new JComboBox<>(new String[]{"Administrador", "Usuario"});
        comboBox1.setBounds(450, 360, 300, 30);
        backgroundPanel.add(comboBox1);

        // Botón Enviar
        enviarButton = new JButton("Enviar");
        enviarButton.setBounds(350, 450, 100, 30);
        enviarButton.addActionListener(e -> {
            System.out.println("🔘 Botón 'Enviar' presionado.");

            // Verifica si Firebase está inicializado
            if (auth == null || db == null) {
                JOptionPane.showMessageDialog(this, "❌ Error: Firebase no está conectado.", "Error", JOptionPane.ERROR_MESSAGE);
                System.out.println("❌ Firebase no está conectado.");
                return;
            }

            System.out.println("✅ Firebase conectado correctamente. Procediendo con el registro...");

            // Intenta registrar usuario
            registrarUsuario();
        });

        backgroundPanel.add(enviarButton);

        // Panel principal
        mainRegister = new JPanel(new BorderLayout());
        mainRegister.add(backgroundPanel);
        setContentPane(mainRegister);
    }

    private void agregarCampo(String texto, int x, int y, JPanel panel) {
        JLabel label = new JLabel(texto);
        label.setForeground(Color.BLACK);
        label.setFont(new Font("Consolas", Font.PLAIN, 18));
        label.setBounds(x, y, 200, 30);
        panel.add(label);
    }

    private void registrarUsuario() {
        System.out.println("🔘 Botón 'Enviar' fue presionado.");

        String nombre = textField1.getText().trim();
        String apellido = textField2.getText().trim();
        String nombreUsuario = textField3.getText().trim();
        String email = textField4.getText().trim();
        String password = new String(passwordField1.getPassword());
        String role = comboBox1.getSelectedItem().toString();

        if (nombre.isEmpty() || apellido.isEmpty() || nombreUsuario.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Por favor, complete todos los campos", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (auth == null || db == null) {
            JOptionPane.showMessageDialog(this, "❌ Firebase no está conectado.", "Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("❌ Error: Firebase no está conectado.");
            return;
        }
        System.out.println("✅ Firebase conectado correctamente.");

        try {
            System.out.println("🔍 Verificando si el correo ya está registrado...");
            try {
                UserRecord existingUser = auth.getUserByEmail(email);
                if (existingUser != null) {
                    JOptionPane.showMessageDialog(this, "⚠️ El correo ya está registrado.", "Error", JOptionPane.ERROR_MESSAGE);
                    System.out.println("⚠️ El correo ya está registrado.");
                    return;
                }
            } catch (Exception ignored) {
                System.out.println("✔️ El correo no está registrado, continuando...");
            }

            System.out.println("📝 Creando usuario en Firebase Authentication...");
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password);

            UserRecord userRecord = auth.createUser(request);
            String uid = userRecord.getUid();
            System.out.println("✅ Usuario creado con UID: " + uid);

            System.out.println("📝 Guardando usuario en Firestore...");
            Map<String, Object> userData = new HashMap<>();
            userData.put("nombre", nombre);
            userData.put("apellido", apellido);
            userData.put("nombreUsuario", nombreUsuario);
            userData.put("email", email);
            userData.put("rol", role);

            ApiFuture<WriteResult> future = db.collection("users").document(uid).set(userData);
            WriteResult result = future.get();
            System.out.println("✅ Usuario guardado en Firestore en: " + result.getUpdateTime());

            JOptionPane.showMessageDialog(this, "✅ Registro exitoso!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (InterruptedException | ExecutionException e) {
            JOptionPane.showMessageDialog(this, "❌ Error en Firestore: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("❌ Error en Firestore: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Error en Firebase Authentication: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("❌ Error en autenticación: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
