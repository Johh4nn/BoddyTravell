package FireBase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.Firestore;

import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseInit {
    private static boolean initialized = false;

    public static void initialize() {
        if (!initialized) {
            try {
                FileInputStream serviceAccount = new FileInputStream("src/main/resources/buddytravel.json");

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setDatabaseUrl("https://<TU_PROYECTO>.firebaseio.com")
                        .build();

                FirebaseApp.initializeApp(options);
                initialized = true;
                System.out.println("✅ Firebase inicializado correctamente.");
            } catch (IOException e) {
                throw new IllegalStateException("❌ Error al inicializar Firebase: " + e.getMessage(), e);
            }
        }
    }

    public static Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }
}


