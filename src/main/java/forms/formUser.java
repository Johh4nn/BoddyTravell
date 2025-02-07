package forms;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.database.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class formUser extends JFrame {

    public JPanel mainPanel;
    private JTable tablePromociones;
    private JButton btnActualizar;
    private JButton btnSalir;
    private JTextField searchBar;
    private JButton btnSearch;
    private JLabel lblPagination;

    private DefaultTableModel promocionesModel;
    private DatabaseReference database;

    public formUser() {
        initializeFirebase();
        initializeUI();
        loadPromotions();
    }

    /**
     * Inicializa la conexión con Firebase si no está inicializada.
     */
    private void initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FileInputStream serviceAccount = new FileInputStream("src/main/resources/buddytravel.json");
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setDatabaseUrl("https://buddytravel-e497e.firebaseio.com/")
                        .build();
                FirebaseApp.initializeApp(options);
            }

            database = FirebaseDatabase.getInstance().getReference("promociones");
            System.out.println("✅ Firebase conectado correctamente.");

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "❌ Error al conectar con Firebase: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Inicializa la interfaz gráfica.
     */
    private void initializeUI() {
        setTitle("Promociones y Paquetes - Buddy Travel");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout());

        // Modelo de la tabla
        String[] columnNames = {"Nombre", "Descripción", "Precio", "Duración"};
        promocionesModel = new DefaultTableModel(columnNames, 0);
        tablePromociones = new JTable(promocionesModel);
        JScrollPane scrollPane = new JScrollPane(tablePromociones);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Barra de búsqueda
        JPanel searchPanel = new JPanel();
        searchBar = new JTextField(20);
        btnSearch = new JButton("Buscar");
        btnSearch.addActionListener(e -> searchPromotions());
        searchPanel.add(searchBar);
        searchPanel.add(btnSearch);
        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // Botones de actualización y salir
        btnActualizar = new JButton("Actualizar Promociones");
        btnActualizar.addActionListener(e -> loadPromotions());
        btnSalir = new JButton("Salir");
        btnSalir.addActionListener(e -> dispose()); // Cerrar solo la ventana actual

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnActualizar);
        buttonPanel.add(btnSalir);

        // Etiqueta de paginación (por ahora sin funcionalidad de paginación real)
        lblPagination = new JLabel("Cargando datos...");
        JPanel paginationPanel = new JPanel();
        paginationPanel.add(lblPagination);
        mainPanel.add(paginationPanel, BorderLayout.SOUTH);

        // Panel de botones
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    /**
     * Carga las promociones desde Firebase y las muestra en la tabla.
     */
    private void loadPromotions() {
        promocionesModel.setRowCount(0); // Limpiar la tabla antes de cargar nuevos datos
        lblPagination.setText("Cargando datos...");

        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                SwingUtilities.invokeLater(() -> {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Map<String, Object> promo = (Map<String, Object>) data.getValue();
                        if (promo != null) {
                            String nombre = promo.getOrDefault("Nombre", "").toString();
                            String descripcion = promo.getOrDefault("Descripción", "").toString();
                            String precio = promo.getOrDefault("Precio", "").toString();
                            String duracion = promo.getOrDefault("Duración", "").toString();

                            promocionesModel.addRow(new Object[]{nombre, descripcion, precio, duracion});
                        }
                    }
                    lblPagination.setText("Total promociones: " + snapshot.getChildrenCount());
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                JOptionPane.showMessageDialog(null, "❌ Error al cargar promociones: " + error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Busca promociones basadas en el término de búsqueda ingresado.
     */
    private void searchPromotions() {
        String searchTerm = searchBar.getText().trim().toLowerCase();
        if (!searchTerm.isEmpty()) {
            promocionesModel.setRowCount(0); // Limpiar la tabla antes de cargar los resultados de búsqueda
            lblPagination.setText("Buscando...");

            database.orderByChild("Nombre").startAt(searchTerm).endAt(searchTerm + "\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    SwingUtilities.invokeLater(() -> {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Map<String, Object> promo = (Map<String, Object>) data.getValue();
                            if (promo != null) {
                                String nombre = promo.getOrDefault("Nombre", "").toString();
                                String descripcion = promo.getOrDefault("Descripción", "").toString();
                                String precio = promo.getOrDefault("Precio", "").toString();
                                String duracion = promo.getOrDefault("Duración", "").toString();

                                promocionesModel.addRow(new Object[]{nombre, descripcion, precio, duracion});
                            }
                        }
                        lblPagination.setText("Resultados encontrados: " + snapshot.getChildrenCount());
                    });
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    JOptionPane.showMessageDialog(null, "❌ Error al buscar promociones: " + error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }
}
