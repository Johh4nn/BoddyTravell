package forms;

import FireBase.AuthService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

public class formUser extends JFrame {

    public JPanel mainPanel;
    private JTabbedPane tabbedPane1;
    private JPanel Home;
    private JTable TablaHome;
    private JTextField textField1;
    private JButton cerrarSesionButton;
    private JButton buscarButton;
    public JTable table2;
    private JLabel nombre_usuario;
    public JPanel Buscar_paquete;
    private JButton confirmarButton;

    private String emailUsuario;
    private Firestore db;
    private static final String RESERVAS_COLLECTION = "reservas";

    public formUser(String email) {
        this.emailUsuario = email;

        // Inicializar la instancia de Firestore
        db = FirestoreClient.getFirestore();

        // Configurar la tabla Buscar Paquetes
        DefaultTableModel modeloBusqueda = new DefaultTableModel(
                new Object[]{"Nombre del Paquete", "Descripción", "Precio", "Duración", "Tipo de Viaje"},
                0
        );
        table2.setModel(modeloBusqueda);

        // Configurar la tabla Home
        String[] columnNames = {"Nombre del Paquete", "Descripción", "Precio", "Duración", "Tipo de Viaje"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        TablaHome.setModel(model);

        // Cargar paquetes turísticos
        cargarPaquetesHome();

        // Mostrar nombre del usuario en el JLabel
        cargarNombreUsuario();

        // Ajustar tamaño de columnas y filas
        ajustarTamañoColumnas(TablaHome);
        ajustarTamañoFilas(TablaHome);

        // Acción del botón "Cerrar Sesión"
        cerrarSesionButton.addActionListener(e -> cerrarSesion());

        // Acción del botón de búsqueda
        buscarButton.addActionListener(e -> {
            String searchQuery = textField1.getText().trim();
            buscarPaquete(searchQuery);
        });

        // Acción del botón confirmar
        confirmarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table2.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(formUser.this,
                            "Por favor, seleccione un paquete para confirmar.",
                            "Selección Requerida",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String nombrePaquete = (String) table2.getValueAt(selectedRow, 0);
                String precio = (String) table2.getValueAt(selectedRow, 2);

                int confirmacion = JOptionPane.showConfirmDialog(formUser.this,
                        "¿Desea confirmar la reserva del paquete: " + nombrePaquete + "?\n" +
                                "Precio: " + precio,
                        "Confirmar Reserva",
                        JOptionPane.YES_NO_OPTION);

                if (confirmacion == JOptionPane.YES_OPTION) {
                    realizarReserva(nombrePaquete, precio);
                }
            }
        });
    }

    private void realizarReserva(String nombrePaquete, String precio) {
        try {
            // Crear un documento con los datos de la reserva
            java.util.Map<String, Object> reserva = new java.util.HashMap<>();
            reserva.put("emailUsuario", emailUsuario);
            reserva.put("nombrePaquete", nombrePaquete);
            reserva.put("precio", precio);
            reserva.put("fechaReserva", new java.util.Date());
            reserva.put("estado", "Pendiente");

            // Guardar la reserva en Firestore
            ApiFuture<com.google.cloud.firestore.WriteResult> future =
                    db.collection(RESERVAS_COLLECTION).document().set(reserva);

            future.get(); // Esperar a que se complete la operación

            JOptionPane.showMessageDialog(this,
                    "Reserva realizada con éxito para el paquete: " + nombrePaquete,
                    "Reserva Confirmada",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al realizar la reserva: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void cargarNombreUsuario() {
        AuthService authService = new AuthService();
        String nombre = authService.getUserName(emailUsuario);
        nombre_usuario.setText("  Bienvenido,  " + nombre);
    }

    private void cargarPaquetesHome() {
        DefaultTableModel tableModel = (DefaultTableModel) TablaHome.getModel();
        tableModel.setRowCount(0);

        try {
            ApiFuture<QuerySnapshot> future = db.collection("paquetes").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            for (QueryDocumentSnapshot document : documents) {
                String nombre = document.getString("Nombre del Paquete");
                String descripcion = document.getString("Descripción");
                String precio = document.getString("Precio");
                String duracion = document.getString("Duración");
                String tipoViaje = document.getString("Tipo de Viaje");

                tableModel.addRow(new Object[]{nombre, descripcion, precio, duracion, tipoViaje});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar los paquetes: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void buscarPaquete(String query) {
        DefaultTableModel tableModel = (DefaultTableModel) table2.getModel();
        tableModel.setRowCount(0);

        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, ingresa un término de búsqueda.",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            ApiFuture<QuerySnapshot> future = db.collection("paquetes").get();
            QuerySnapshot querySnapshot = future.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            boolean encontrado = false;

            for (QueryDocumentSnapshot document : documents) {
                String nombrePaquete = document.getString("Nombre del Paquete");
                if (nombrePaquete == null) continue;

                if (nombrePaquete.toLowerCase().contains(query.toLowerCase())) {
                    encontrado = true;

                    tableModel.addRow(new Object[]{
                            nombrePaquete,
                            document.getString("Descripción"),
                            document.getString("Precio"),
                            document.getString("Duración"),
                            document.getString("Tipo de Viaje")
                    });
                }
            }

            if (!encontrado) {
                JOptionPane.showMessageDialog(this,
                        "No se encontraron paquetes con ese nombre.",
                        "Sin Resultados",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al buscar paquetes: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cerrarSesion() {
        JFrame userFrame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);
        Point currentPosition = userFrame != null ? userFrame.getLocation() : new Point(100, 100);

        if (userFrame != null) {
            userFrame.dispose();
        }

        JFrame loginFrame = new JFrame("Login");
        loginFrame.setContentPane(new formLogin().mainLogin);
        loginFrame.setSize(1280, 720);
        loginFrame.setLocation(currentPosition);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setVisible(true);
    }

    private void ajustarTamañoColumnas(JTable table) {
        for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
            int width = 0;
            for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++) {
                TableCellRenderer renderer = table.getCellRenderer(rowIndex, columnIndex);
                Component comp = table.prepareRenderer(renderer, rowIndex, columnIndex);
                width = Math.max(comp.getPreferredSize().width, width);
            }
            table.getColumnModel().getColumn(columnIndex).setPreferredWidth(width + 10);
        }
    }

    private void ajustarTamañoFilas(JTable table) {
        for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++) {
            int rowHeight = 0;
            for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
                TableCellRenderer renderer = table.getCellRenderer(rowIndex, columnIndex);
                Component comp = table.prepareRenderer(renderer, rowIndex, columnIndex);
                rowHeight = Math.max(comp.getPreferredSize().height, rowHeight);
            }
            table.setRowHeight(rowIndex, rowHeight + 5);
        }
    }
}