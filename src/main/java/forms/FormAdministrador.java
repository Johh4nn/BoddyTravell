package forms;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Font;
import com.google.api.core.ApiFuture;
import java.io.File;



import com.itextpdf.text.FontFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import java.util.concurrent.ExecutionException;

public class FormAdministrador extends JFrame {
    private JTabbedPane tabbedPane;
    private JTable userTable;
    private DefaultTableModel userTableModel;
    private Color backgroundColor = new Color(102, 102, 153);
    private Firestore db;
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
            JOptionPane.showMessageDialog(this,
                    "Error al inicializar Firestore: " + e.getMessage(),
                    "Error de Inicialización",
                    JOptionPane.ERROR_MESSAGE);
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
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);
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

        String[] labels = {"Nombre", "Descripción", "Precio", "Duración", "Fecha de Inicio",
                "Fecha de Retorno", "N° Personas", "Tipo de Viaje", "Incluye", "No Incluye"};
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
            if (db == null) {
                JOptionPane.showMessageDialog(this,
                        "Error: La conexión con Firestore no está inicializada",
                        "Error de Conexión",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Map<String, Object> paquete = new HashMap<>();
            for (int i = 0; i < labels.length; i++) {
                paquete.put(labels[i], fields[i].getText());
            }

            try {
                db.collection("paquetes").document()
                        .set(paquete)
                        .get();

                JOptionPane.showMessageDialog(this, "Paquete guardado exitosamente");
                for (JTextField field : fields) {
                    field.setText("");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al guardar el paquete: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        return panel;
    }

    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(backgroundColor);

        JLabel instructionLabel = new JLabel("Haga click para descargar el informe");
        instructionLabel.setForeground(Color.WHITE);

        JButton downloadButton = new JButton("Descargar");
        downloadButton.addActionListener(e -> {
            generarInformePDF();
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(instructionLabel, gbc);

        gbc.gridy = 1;
        panel.add(downloadButton, gbc);

        return panel;
    }

    private void generarInformePDF() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar Informe PDF");
            fileChooser.setSelectedFile(new File("Informe_Paquetes.pdf"));

            if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document,
                    new FileOutputStream(fileChooser.getSelectedFile()));

            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Informe de Paquetes Turísticos", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            ApiFuture<QuerySnapshot> future = db.collection("paquetes").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);

            float[] columnWidths = {2f, 4f, 2f, 2f, 2f};
            table.setWidths(columnWidths);

            String[] headers = {"Nombre", "Descripción", "Precio", "Duración", "Tipo de Viaje"};
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setPadding(5);
                table.addCell(cell);
            }

            Font contentFont = new Font(Font.FontFamily.HELVETICA, 10);

            for (QueryDocumentSnapshot doc : documents) {
                table.addCell(new Phrase(doc.getString("Nombre"), contentFont));
                table.addCell(new Phrase(doc.getString("Descripción"), contentFont));
                table.addCell(new Phrase(doc.getString("Precio"), contentFont));
                table.addCell(new Phrase(doc.getString("Duración"), contentFont));
                table.addCell(new Phrase(doc.getString("Tipo de Viaje"), contentFont));
            }

            document.add(table);

            Font dateFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);
            Paragraph date = new Paragraph("\nInforme generado el: " +
                    new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                            .format(new java.util.Date()), dateFont);
            date.setAlignment(Element.ALIGN_RIGHT);
            document.add(date);

            document.close();

            JOptionPane.showMessageDialog(this,
                    "Informe PDF generado exitosamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al generar el informe: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(backgroundColor);

        String[] columns = {"ID", "Nombre", "Email", "Fecha de Registro"};
        userTableModel = new DefaultTableModel(columns, 0);
        userTable = new JTable(userTableModel);

        userTable.setFillsViewportHeight(true);
        userTable.setBackground(Color.WHITE);
        userTable.setForeground(Color.BLACK);
        userTable.setGridColor(Color.LIGHT_GRAY);

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(backgroundColor);

        JButton refreshButton = new JButton("Actualizar Lista");
        refreshButton.addActionListener(e -> {
            actualizarListaUsuarios();
        });

        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void actualizarListaUsuarios() {
        try {
            userTableModel.setRowCount(0);

            db.collection("usuarios").get()
                    .get()
                    .forEach(document -> {
                        userTableModel.addRow(new Object[]{
                                document.getId(),
                                document.getString("nombre"),
                                document.getString("email"),
                                document.getString("fechaRegistro")
                        });
                    });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar usuarios: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new FormAdministrador().setVisible(true);
        });
    }
}