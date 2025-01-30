package org.example;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.FileInputStream;
import java.io.IOException;

public class Conexion {

    public static Firestore db;

    public static void conectarFirebase() {
        try {
            FileInputStream sa = new FileInputStream("src/main/resources/buddytravel.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(sa))
                    .build();

            FirebaseApp.initializeApp(options);
            db = FirestoreClient.getFirestore();

            System.out.println("✅ Firestore conectado correctamente.");
        } catch (IOException e) {
            System.err.println("❌ Error al conectar Firestore: " + e.getMessage());
        }
    }
}