package FireBase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;

import java.io.FileInputStream;
import java.io.IOException;

public class AuthService {
    private final FirebaseAuth auth;

    public AuthService() {
        initializeFirebase(); // Asegurarnos de inicializar Firebase correctamente
        this.auth = FirebaseAuth.getInstance();
    }

    // Metodo para inicializar Firebase
    private void initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                // Ruta al archivo google-services.json o credenciales de Firebase
                FileInputStream serviceAccount = new FileInputStream("src/main/resources/buddytravel.json");

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setDatabaseUrl("https://<Buddytravel>.firebaseio.com")
                        .build();

                FirebaseApp.initializeApp(options);
                System.out.println("✅ Firebase inicializado correctamente.");
            }
        } catch (IOException e) {
            throw new IllegalStateException("❌ Error al inicializar Firebase: " + e.getMessage(), e);
        }
    }

    // Metodo para obtener información del usuario por correo electrónico
    public UserRecord getUserByEmail(String email) {
        try {
            UserRecord userRecord = auth.getUserByEmail(email);
            System.out.println("✅ Usuario encontrado: " + userRecord.getEmail());
            return userRecord;
        } catch (FirebaseAuthException e) {
            System.err.println("❌ Error al obtener el usuario: " + e.getMessage());
            return null;
        }
    }

    // Metodo para verificar si un usuario existe
    public boolean userExists(String email) {
        UserRecord userRecord = getUserByEmail(email);
        return userRecord != null;
    }

    // Metodo para autenticar al usuario
    public boolean loginUser(String email, String password) {
        try {
            UserRecord userRecord = auth.getUserByEmail(email);
            if (userRecord != null) {
                System.out.println("✅ Usuario autenticado: " + userRecord.getEmail());
                return true;
            } else {
                System.out.println("❌ Usuario no encontrado.");
                return false;
            }
        } catch (FirebaseAuthException e) {
            System.err.println("❌ Error en login: " + e.getMessage());
            return false;
        }
    }
}