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
import java.util.List;
import java.util.Map;

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

    private String emailUsuario; // Almacena el email del usuario autenticado

    public formUser(String email) {
        this.emailUsuario = email; // Guardar el email del usuario autenticado

        // Configurar la tabla
        String[] columnNames = {"Atributo", "Valor"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        TablaHome.setModel(model);

        // Cargar paquetes turísticos
        cargarPaquetes();

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

                // Volver a abrir la ventana de Login en la misma posición
                JFrame loginFrame = new JFrame("Login");
                loginFrame.setContentPane(new formLogin().mainLogin);
                loginFrame.setSize(1280, 720);
                loginFrame.setLocation(currentPosition);
                loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                loginFrame.setVisible(true);
            }
        });
    }

    // Método para cargar el nombre del usuario en el JLabel
    private void cargarNombreUsuario() {
        AuthService authService = new AuthService();
        String nombre = authService.getUserName(emailUsuario); // Obtener el nombre del usuario
        nombre_usuario.setText("  Bienvenido,  " + nombre); // Mostrar en el JLabel
    }

    // Método para cargar los paquetes turísticos en la JTable
    private void cargarPaquetes() {
        UserService userService = new UserService();

        try {
            List<Map<String, Object>> paquetes = userService.obtenerPaquetes();

            DefaultTableModel model = (DefaultTableModel) TablaHome.getModel();

            for (Map<String, Object> paquete : paquetes) {
                String nombre = (String) paquete.get("nombre");
                String descripcion = (String) paquete.get("descripción");
                double precio = (double) paquete.get("precio");
                String duracion = (String) paquete.get("duracion");
                String imagenURL = (String) paquete.get("imagenURL");

                // Crear un ImageIcon para la imagen
                ImageIcon imagenIcon = cargarImagen(imagenURL);

                // Agregar los datos a la tabla (ahora las columnas se convierten en filas)
                model.addRow(new Object[]{"Nombre", nombre});
                model.addRow(new Object[]{"Descripción", descripcion});
                model.addRow(new Object[]{"Precio", precio});
                model.addRow(new Object[]{"Duración", duracion});
                model.addRow(new Object[]{"Imagen", imagenIcon});
                model.addRow(new Object[]{"", ""}); // Fila vacía para separar paquetes
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar los paquetes", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para cargar la imagen desde la URL
    private ImageIcon cargarImagen(String imagenURL) {
        try {
            URL url = new URL(imagenURL); // Cargar la URL de la imagen
            ImageIcon imagenIcon = new ImageIcon(url);
            Image img = imagenIcon.getImage(); // Convertir a imagen
            Image newImg = img.getScaledInstance(300, 200, Image.SCALE_SMOOTH); // Escalar la imagen más grande
            return new ImageIcon(newImg);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Retorna null si hubo un error cargando la imagen
        }
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
