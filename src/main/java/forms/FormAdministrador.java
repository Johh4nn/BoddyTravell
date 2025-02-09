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
import com.google.api.core.ApiFuture;
import FireBase.AuthService;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;


public class FormAdministrador extends JFrame {
    private JTabbedPane tabbedPane;
    private JTable userTable;
    private DefaultTableModel userTableModel;
    private Color backgroundColor = new Color(102, 102, 153);
    private Firestore db;
    public JPanel JAdmin;
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
    private JPanel JPanel;
    private JButton cerrarSesionButton;
    private JTable tableUsuarios;
    private JTextField textField10;
    private JButton guardarButton;

    private JButton downloadButton;
    private JButton refreshButton;
    private JLabel nombre_usuario;
    private String emailUsuario;

    public FormAdministrador(String email) {
        this.emailUsuario = email;

        // Inicialización básica del frame
        setTitle("Panel de Administración");
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Inicializar Firebase
        initializeFirestore();

        // Cargar el nombre del usuario
        cargarNombreUsuario();

        // Crear el panel principal
        JAdmin = new JPanel(new BorderLayout());

        // Crear el TabbedPane
        tabbedPane1 = new JTabbedPane();
        tabbedPane1.addTab("Inicio", createHomePanel());
        tabbedPane1.addTab("Gestión de Paquetes", createPackagePanel());
        tabbedPane1.addTab("Usuarios", createUsersPanel());
        tabbedPane1.addTab("Reportes", createReportPanel());

        JAdmin.add(tabbedPane1, BorderLayout.CENTER);

        // Panel superior con información del usuario y botón de cerrar sesión
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(backgroundColor);

        nombre_usuario = new JLabel();
        nombre_usuario.setForeground(Color.WHITE);
        topPanel.add(nombre_usuario, BorderLayout.WEST);

        cerrarSesionButton = new JButton("Cerrar Sesión");
        cerrarSesionButton.addActionListener(e -> cerrarSesion());
        topPanel.add(cerrarSesionButton, BorderLayout.EAST);

        JAdmin.add(topPanel, BorderLayout.NORTH);

        setContentPane(JAdmin);
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

    private void cargarNombreUsuario() {
        AuthService authService = new AuthService();
        String nombre = authService.getUserName(emailUsuario);
        nombre_usuario.setText("  Bienvenido, " + nombre);
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(backgroundColor);

        JLabel welcomeLabel = new JLabel("Panel de Administración - BuddyTravel", SwingConstants.CENTER);
        welcomeLabel.setForeground(Color.WHITE);
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD);
        panel.add(welcomeLabel, BorderLayout.CENTER);

        return panel;
    }

    private void buscarPaquete(Map<String, JTextField> fields, JTable packageTable) {
        String nombrePaquete = fields.get("Nombre del Paquete").getText().trim();

        if (nombrePaquete.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor ingrese el nombre del paquete a buscar",
                    "Campo Vacío",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            ApiFuture<QuerySnapshot> future = db.collection("paquetes")
                    .whereEqualTo("Nombre del Paquete", nombrePaquete)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (documents.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No se encontró ningún paquete con ese nombre",
                        "Paquete No Encontrado",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Suponemos que solo hay un paquete con ese nombre
            QueryDocumentSnapshot document = documents.get(0);

            // Llenar los campos con la información del paquete
            fields.forEach((key, field) -> field.setText(document.getString(key)));

            // Seleccionar el paquete en la tabla
            DefaultTableModel tableModel = (DefaultTableModel) packageTable.getModel();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (tableModel.getValueAt(i, 0).equals(nombrePaquete)) {
                    packageTable.setRowSelectionInterval(i, i);
                    packageTable.scrollRectToVisible(packageTable.getCellRect(i, 0, true));
                    break;
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al buscar el paquete: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void eliminarPaquete(JTable packageTable) {
        int selectedRow = packageTable.getSelectedRow(); // Obtener la fila seleccionada

        if (selectedRow == -1) { // Si no hay ninguna fila seleccionada
            JOptionPane.showMessageDialog(this,
                    "Por favor seleccione un paquete de la tabla para eliminar",
                    "Ningún Paquete Seleccionado",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obtener el nombre del paquete de la fila seleccionada
        String nombrePaquete = (String) packageTable.getValueAt(selectedRow, 0);

        try {
            // Buscar el paquete en Firestore por su nombre
            ApiFuture<QuerySnapshot> future = db.collection("paquetes")
                    .whereEqualTo("Nombre del Paquete", nombrePaquete)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (documents.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No se encontró ningún paquete con ese nombre",
                        "Paquete No Encontrado",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Eliminar el documento
            QueryDocumentSnapshot document = documents.get(0);
            db.collection("paquetes").document(document.getId()).delete().get();

            JOptionPane.showMessageDialog(this,
                    "Paquete eliminado exitosamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);

            // Actualizar la tabla después de eliminar
            cargarPaquetesEnTabla(packageTable);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al eliminar el paquete: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private JPanel createPackagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(backgroundColor);

        // Crear la tabla para mostrar los paquetes
        String[] columns = {"Nombre del Paquete", "Descripción", "Precio", "Duración", "Tipo de Viaje"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        JTable packageTable = new JTable(tableModel);

        packageTable.setFillsViewportHeight(true);
        packageTable.setBackground(Color.WHITE);
        packageTable.setForeground(Color.BLACK);
        packageTable.setGridColor(Color.LIGHT_GRAY);

        JScrollPane scrollPane = new JScrollPane(packageTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel para los campos de entrada y botones
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Definimos los campos y sus etiquetas
        String[] labels = {
                "Nombre del Paquete", "Descripción", "Precio", "Duración",
                "Fecha de Inicio", "Fecha de Retorno", "Número de Personas",
                "Tipo de Viaje", "Incluye", "No Incluye"
        };

        Map<String, JTextField> fields = new HashMap<>();

        // Creamos los campos y los agregamos al panel
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;

            JLabel label = new JLabel(labels[i]);
            label.setForeground(Color.WHITE);
            inputPanel.add(label, gbc);

            gbc.gridx = 1;
            JTextField field = new JTextField(20);
            fields.put(labels[i], field);
            inputPanel.add(field, gbc);
        }

        // Panel para botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(backgroundColor);

        JButton clearButton = new JButton("Limpiar");
        JButton saveButton = new JButton("Guardar Paquete");
        JButton searchButton = new JButton("Buscar Paquete");
        JButton deleteButton = new JButton("Eliminar Paquete");

        clearButton.addActionListener(e -> fields.values().forEach(field -> field.setText("")));

        saveButton.addActionListener(e -> guardarPaquete(fields, packageTable));
        searchButton.addActionListener(e -> buscarPaquete(fields, packageTable));
        deleteButton.addActionListener(e -> eliminarPaquete(packageTable));

        buttonPanel.add(clearButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(deleteButton);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        inputPanel.add(buttonPanel, gbc);

        // Agregar la tabla y el panel de entrada al panel principal
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        // Cargar los paquetes en la tabla al iniciar
        cargarPaquetesEnTabla(packageTable);

        return panel;
    }

    private void cargarPaquetesEnTabla(JTable packageTable) {
        DefaultTableModel tableModel = (DefaultTableModel) packageTable.getModel();
        tableModel.setRowCount(0); // Limpiar la tabla antes de cargar nuevos datos

        try {
            ApiFuture<QuerySnapshot> future = db.collection("paquetes").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            for (QueryDocumentSnapshot document : documents) {
                tableModel.addRow(new Object[]{
                        document.getString("Nombre del Paquete"),
                        document.getString("Descripción"),
                        document.getString("Precio"),
                        document.getString("Duración"),
                        document.getString("Tipo de Viaje")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar los paquetes: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void guardarPaquete(Map<String, JTextField> fields, JTable packageTable) {
        // Validación básica
        boolean camposVacios = fields.values().stream()
                .anyMatch(field -> field.getText().trim().isEmpty());

        if (camposVacios) {
            JOptionPane.showMessageDialog(this,
                    "Por favor complete todos los campos",
                    "Campos Incompletos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Crear el mapa de datos
        Map<String, Object> paqueteData = new HashMap<>();
        fields.forEach((key, field) -> paqueteData.put(key, field.getText().trim()));

        // Agregar metadata
        paqueteData.put("fechaCreacion", com.google.cloud.Timestamp.now());
        paqueteData.put("estado", "activo");

        // Guardar en Firestore
        try {
            DocumentReference docRef = db.collection("paquetes").document();

            // Versión síncrona más simple
            docRef.set(paqueteData).get(); // Espera a que se complete la operación

            // Mostrar mensaje de éxito
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "Paquete guardado exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                fields.values().forEach(field -> field.setText(""));
            });

            // Actualizar la tabla después de guardar
            cargarPaquetesEnTabla(packageTable);

        } catch (Exception ex) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "Error al intentar guardar: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            });
            ex.printStackTrace();
        }
    }

    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(backgroundColor);

        String[] columns = {"ID", "Nombre", "Email", "nombreUsuario"};
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

        refreshButton = new JButton("Actualizar Lista");
        refreshButton.addActionListener(e -> actualizarListaUsuarios());

        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void actualizarListaUsuarios() {
        try {
            userTableModel.setRowCount(0);

            ApiFuture<QuerySnapshot> future = db.collection("users").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            for (QueryDocumentSnapshot document : documents) {
                userTableModel.addRow(new Object[]{
                        document.getId(),
                        document.getString("nombre"),
                        document.getString("email"),
                        document.getString("nombreUsuario")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar usuarios: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(backgroundColor);

        JLabel instructionLabel = new JLabel("Generar informe de paquetes turísticos");
        instructionLabel.setForeground(Color.WHITE);
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD);

        downloadButton = new JButton("Descargar Informe PDF");
        downloadButton.addActionListener(e -> generarInformePDF());

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
                table.addCell(new Phrase(doc.getString("Nombre del Paquete"), contentFont));
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

    private void cerrarSesion() {
        // Obtener la ventana actual
        JFrame userFrame = (JFrame) SwingUtilities.getWindowAncestor(JAdmin);

        // Obtener la posición actual antes de cerrar
        Point currentPosition = userFrame != null ? userFrame.getLocation() : new Point(100, 100);

        // Cerrar la ventana actual
        if (userFrame != null) {
            userFrame.dispose();
        }

        // Volver a abrir la ventana de Login en la misma posición
        JFrame loginFrame = new JFrame("Login");
        loginFrame.setContentPane(new formLogin().mainLogin);
        loginFrame.setSize(1280, 720);
        loginFrame.setLocation(currentPosition);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setVisible(true);
    }
}