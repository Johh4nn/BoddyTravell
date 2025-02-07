package FireBase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseInit {
    private static Firestore db;

    public static void initialize() {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                // Asegúrate de que este archivo existe en src/main/resources/
                FileInputStream serviceAccount = new FileInputStream("src/main/resources/serviceAccountKey.json");

                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setDatabaseUrl("https://tu-proyecto.firebaseio.com") // Cambia por la URL de tu Firebase
                        .build();

                FirebaseApp.initializeApp(options);
                db = FirestoreClient.getFirestore();
                System.out.println("🔥 Firebase Firestore inicializado correctamente.");
            } catch (IOException e) {
                System.err.println("❌ Error inicializando Firebase: " + e.getMessage());
            }
        }
    }

    public static Firestore getFirestore() {
        return db;
    }
}
