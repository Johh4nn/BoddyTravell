package forms;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class adminform {
    private JPanel JAdmin;
    private JTabbedPane tabbedPane1;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JTextField textField5;
    private JTextField textField6;
    private JTextField textField7;
    private JTextField textField8;
    private JPasswordField passwordField1;
    private JTextField textField9;
    private JButton button3;
    private javax.swing.JPanel JPanel;
    private JButton button1;
    private JTable tableUsuarios;

    public static class formadmini extends JFrame {
        private JTabbedPane tabbedPane;
        private JTable userTable;
        private DefaultTableModel userTableModel;
        private Color backgroundColor = new Color(102, 102, 153);
        private DatabaseReference database;

        public formadmini() {
            initializeFirebase();
            initializeUI();
        }

        private void initializeFirebase() {
            try {
                FileInputStream serviceAccount = new FileInputStream("path/to/buddytravel.json");

                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setDatabaseUrl("https://buddytravel-e497e.firebaseio.com/")  // Solo la URL del Realtime Database
                        .build();

                FirebaseApp.initializeApp(options);
                database = FirebaseDatabase.getInstance().getReference("paquetes");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void initializeUI() {
            setTitle("Panel de Administración");
            setSize(800, 600);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            tabbedPane = new JTabbedPane();
            tabbedPane.addTab("Home", createHomePanel());
            tabbedPane.addTab("Ingresar Paquete", createPackagePanel());
            tabbedPane.addTab("Descargar Informe", createReportPanel());
            tabbedPane.addTab("Lista de Usuarios", createUsersPanel());

            add(tabbedPane);
        }

        private JPanel createHomePanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(backgroundColor);

            JLabel welcomeLabel = new JLabel("Bienvenido, Administrador");
            welcomeLabel.setForeground(Color.WHITE);
            welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
            welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

            panel.add(welcomeLabel, BorderLayout.NORTH);

            return panel;
        }

        private JPanel createPackagePanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(backgroundColor);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);

            String[] labels = {"Nombre", "Descripción", "Precio", "Duración", "Fecha de Inicio", "Fecha de Retorno", "N° Personas", "Tipo de Viaje", "Incluye", "No Incluye"};
            JTextField[] fields = new JTextField[labels.length];

            for (int i = 0; i < labels.length; i++) {
                gbc.gridx = 0;
                gbc.gridy = i;
                JLabel label = new JLabel(labels[i]);
                label.setForeground(Color.WHITE);
                panel.add(label, gbc);

                gbc.gridx = 1;
                fields[i] = new JTextField(20);
                panel.add(fields[i], gbc);
            }

            JButton saveButton = new JButton("Guardar");
            gbc.gridy++;
            gbc.gridx = 1;
            gbc.anchor = GridBagConstraints.LINE_END;
            panel.add(saveButton, gbc);

            saveButton.addActionListener(e -> {
                Map<String, String> paquete = new HashMap<>();
                for (int i = 0; i < labels.length; i++) {
                    paquete.put(labels[i], fields[i].getText());
                }
                database.push().setValueAsync(paquete);
                JOptionPane.showMessageDialog(this, "Paquete guardado en Firebase");
            });

            return panel;
        }

        private JPanel createReportPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(backgroundColor);

            JLabel instructionLabel = new JLabel("Haga click para descargar el informe");
            instructionLabel.setForeground(Color.WHITE);

            JButton downloadButton = new JButton("Descargar");

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(instructionLabel, gbc);

            gbc.gridy = 1;
            panel.add(downloadButton, gbc);

            return panel;
        }

        private JPanel createUsersPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(backgroundColor);

            String[] columns = {"ID", "Nombre", "Email", "Fecha de Registro"};
            userTableModel = new DefaultTableModel(columns, 0);
            userTable = new JTable(userTableModel);

            JScrollPane scrollPane = new JScrollPane(userTable);
            panel.add(scrollPane, BorderLayout.CENTER);

            return panel;
        }

        public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> new formadmini().setVisible(true));
        }
    }
}
