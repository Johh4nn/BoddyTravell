package forms;

import FireBase.AuthService;
import FireBase.UserService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;


public class formUser extends JFrame {

    public JPanel mainPanel;
    private JTabbedPane tabbedPane1;
    private JPanel Home;
    private JTable TablaHome;
    private JTextField textField1;
    private JButton cerrarSesionButton;
    private JButton buscarButton;
    private JTable table2;
    private JLabel nombre_usuario; // Mostrar nombre del usuario
    private JPanel Buscar_paquete;
    private JPanel Mis_Paquetes;
    private JTable tablaMisPaquetes;
    private JButton btnConfirmar;
    private JButton btnEliminar;



    private String emailUsuario; // Almacena el email del usuario autenticado
    private Firestore db; // Instancia de Firestore

    public formUser(String email) {
        this.emailUsuario = email; // Guardar el email del usuario autenticado

        // Inicializar la instancia de Firestore
        db = FirestoreClient.getFirestore(); // Esto inicializa Firestore

        // Configurar la tabla
        String[] columnNames = {"Atributo", "Valor"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        TablaHome.setModel(model);

        // Cargar paquetes turísticos
        cargarPaquetesHome();

        // Mostrar nombre del usuario en el JLabel
        cargarNombreUsuario();

        // Establecer el renderizador para la columna "Imagen"
        TablaHome.getColumnModel().getColumn(1).setCellRenderer(new ImageCellRenderer());

        // Ajustar el tamaño de las columnas y filas
        ajustarTamañoColumnas(TablaHome);
        ajustarTamañoFilas(TablaHome);

        // Acción del botón "Cerrar Sesión"
        cerrarSesionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obtener la ventana actual (formUser)
                JFrame userFrame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);

                // Obtener la posición actual antes de cerrar
                Point currentPosition = userFrame != null ? userFrame.getLocation() : new Point(100, 100);

                // Cerrar la ventana actual
                if (userFrame != null) {
                    userFrame.dispose();
                }
                // En el constructor, justo después de initComponents() o donde se inicialicen los componentes
                DefaultTableModel modelBusqueda = new DefaultTableModel(
                        new Object[]{"Nombre del Paquete", "Descripción", "Precio", "Duración", "Tipo de Viaje"},
                        0
                );
                table2.setModel(modelBusqueda);


                // Volver a abrir la ventana de Login en la misma posición
                JFrame loginFrame = new JFrame("Login");
                loginFrame.setContentPane(new formLogin().mainLogin);
                loginFrame.setSize(1280, 720);
                loginFrame.setLocation(currentPosition);
                loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                loginFrame.setVisible(true);
            }
        });

        // Acción del botón de búsqueda
        buscarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchQuery = textField1.getText().trim().toLowerCase();
                buscarPaquete(searchQuery);
            }
        });
    }

    // Clase personalizada para el renderizado de imágenes en la tabla
    class ImageCellRenderer extends JLabel implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof ImageIcon) {
                setIcon((ImageIcon) value); // Establecer la imagen en el JLabel
                setText(""); // No mostrar texto
            } else {
                setText(value != null ? value.toString() : ""); // Mostrar el texto si no es una imagen
                setIcon(null); // No mostrar imagen si es un valor de texto
            }
            return this; // Devolver el JLabel con la imagen o texto
        }
    }

    // Método para cargar el nombre del usuario en el JLabel
    private void cargarNombreUsuario() {
        AuthService authService = new AuthService();
        String nombre = authService.getUserName(emailUsuario); // Obtener el nombre del usuario
        nombre_usuario.setText("  Bienvenido,  " + nombre); // Mostrar en el JLabel
    }

    // Método para cargar los paquetes turísticos en la JTable (Home)
    private void cargarPaquetesHome() {
        DefaultTableModel tableModel = (DefaultTableModel) TablaHome.getModel();
        tableModel.setRowCount(0); // Limpiar la tabla antes de cargar nuevos datos

        try {
            // Acceder a la colección "paquetes" en Firebase
            ApiFuture<QuerySnapshot> future = db.collection("paquetes").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            for (QueryDocumentSnapshot document : documents) {
                // Obtener los datos de cada paquete
                String nombre = document.getString("Nombre del Paquete");
                String descripcion = document.getString("Descripción");
                String precio = document.getString("Precio");
                String duracion = document.getString("Duración");
                String tipoViaje = document.getString("Tipo de Viaje");

                // Agregar una fila a la tabla con los datos del paquete
                tableModel.addRow(new Object[]{
                        nombre,
                        descripcion,
                        precio,
                        duracion,
                        tipoViaje,
                });
            }
        } catch (Exception ex) {
            // Manejar cualquier error que ocurra al cargar los paquetes
            JOptionPane.showMessageDialog(this,
                    "Error al cargar los paquetes: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }




    // Método para buscar paquetes en Firebase
    private void buscarPaquete(String query) {
        DefaultTableModel tableModel = (DefaultTableModel) table2.getModel();
        tableModel.setRowCount(0); // Limpiar la tabla antes de cargar los resultados

        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingresa un término de búsqueda.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Obtener todos los documentos de la colección "paquetes"
            ApiFuture<QuerySnapshot> future = db.collection("paquetes").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            boolean found = false; // Bandera para saber si se encontró algún paquete

            for (QueryDocumentSnapshot document : documents) {
                // Obtener el campo "Nombre del Paquete"
                String nombreField = document.getString("Nombre del Paquete");

                if (nombreField == null) {
                    System.out.println("El documento " + document.getId() + " no tiene el campo 'Nombre del Paquete'");
                    continue;
                }

                // Comparar en minúsculas para una búsqueda insensible a mayúsculas
                if (nombreField.toLowerCase().contains(query.toLowerCase())) {
                    found = true;

                    // Extraer los demás campos
                    String descripcion = document.getString("Descripción");
                    String precio = document.getString("Precio");
                    String duracion = document.getString("Duración");
                    String tipoViaje = document.getString("Tipo de Viaje");

                    // Agregar la fila a la tabla con todos los datos
                    tableModel.addRow(new Object[]{
                            nombreField,
                            descripcion,
                            precio,
                            duracion,
                            tipoViaje
                    });
                }
            }

            if (!found) {
                JOptionPane.showMessageDialog(this, "No se encontraron paquetes con ese nombre.", "Sin Resultados", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al buscar el paquete: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }


    // Ajuste de tamaño automático de las columnas
    private void ajustarTamañoColumnas(JTable table) {
        for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
            int width = 0;
            for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++) {
                TableCellRenderer renderer = table.getCellRenderer(rowIndex, columnIndex);
                Component comp = table.prepareRenderer(renderer, rowIndex, columnIndex);
                width = Math.max(comp.getPreferredSize().width, width);
            }
            table.getColumnModel().getColumn(columnIndex).setPreferredWidth(width + 10);  // Un pequeño margen
        }
    }

    // Método para ajustar el tamaño de las filas a su contenido
    private void ajustarTamañoFilas(JTable table) {
        for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++) {
            int rowHeight = 0;
            for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
                TableCellRenderer renderer = table.getCellRenderer(rowIndex, columnIndex);
                Component comp = table.prepareRenderer(renderer, rowIndex, columnIndex);
                rowHeight = Math.max(comp.getPreferredSize().height, rowHeight);
            }
            table.setRowHeight(rowIndex, rowHeight + 5);  // Un pequeño margen
        }
    }
}
