package forms;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Font;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class FormAdministrador extends JPanel {
    private JTabbedPane tabbedPane;
    private JTable userTable;
    private DefaultTableModel userTableModel;
    private Firestore db;
    private JTabbedPane tabbedPane1;
    public JPanel JAdmin;
    private JTextField textField1;
    private JButton button1;
    private JButton button3;
    private JTable tableUsuarios;

    public FormAdministrador() {
        initializeFirestore();
        initializeUI();
    }

    private void initializeFirestore() {
        try {
            FileInputStream serviceAccount = new FileInputStream("src/main/resources/buddytravel.json");
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            db = FirestoreClient.getFirestore();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al inicializar Firestore: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Home", createHomePanel());
        tabbedPane.addTab("Ingresar Paquete", createPackagePanel());
        tabbedPane.addTab("Descargar Informe", createReportPanel());
        tabbedPane.addTab("Lista de Usuarios", createUsersPanel());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Bienvenido, Administrador", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(welcomeLabel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPackagePanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Formulario de ingreso de paquetes"));
        return panel;
    }

    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel instructionLabel = new JLabel("Haga click en el botón para descargar el informe");
        JButton downloadButton = new JButton("Descargar");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(instructionLabel, gbc);

        gbc.gridy = 1;
        panel.add(downloadButton, gbc);
        return panel;
    }

    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columns = {"ID", "Nombre", "Email", "Fecha de Registro"};
        userTableModel = new DefaultTableModel(columns, 0);
        userTable = new JTable(userTableModel);
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Actualizar Lista");
        refreshButton.addActionListener(e -> actualizarListaUsuarios());
        panel.add(refreshButton, BorderLayout.SOUTH);
        return panel;
    }

    private void actualizarListaUsuarios() {
        if (db == null) {
            JOptionPane.showMessageDialog(this, "Firestore no inicializado", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SwingUtilities.invokeLater(() -> {
            try {
                userTableModel.setRowCount(0);
                List<QueryDocumentSnapshot> documents = db.collection("usuarios").get().get().getDocuments();
                for (QueryDocumentSnapshot doc : documents) {
                    userTableModel.addRow(new Object[]{
                            doc.getId(),
                            doc.getString("nombre"),
                            doc.getString("email"),
                            doc.getString("fechaRegistro")
                    });
                }
            } catch (InterruptedException | ExecutionException e) {
                JOptionPane.showMessageDialog(this, "Error al cargar usuarios: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }



}
