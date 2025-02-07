package FireBase;

import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.api.core.ApiFuture;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AuthService {
    private final FirebaseAuth auth;
    private static final String FIREBASE_WEB_API_KEY = "AIzaSyB4tNmjMhbzQr-dtF6cHU4NAksJU6MD2mc"; // 🔥 Agrega tu API Key aquí
    private final Firestore db;

    public AuthService() {
        FirebaseInit.initialize();  // 🔥 Asegurar que Firebase esté inicializado
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseInit.getFirestore();
    }

    // 🔐 Método para autenticar usuario con email y contraseña
    public boolean loginUser(String email, String password) {
        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        json.put("email", email);
        json.put("password", password);
        json.put("returnSecureToken", true);

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + FIREBASE_WEB_API_KEY)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("❌ Error de autenticación: " + response.message());
                return false;
            }
            return true; // Usuario autenticado correctamente
        } catch (IOException e) {
            System.err.println("❌ Error: " + e.getMessage());
            return false;
        }
    }

    // 🔥 Método para obtener el rol del usuario desde Firestore
    public String getUserRole(String email) {
        try {
            // Referencia a la colección "users" en Firestore
            CollectionReference usersRef = db.collection("users");

            // Crear la consulta para buscar el usuario por su email
            Query query = usersRef.whereEqualTo("email", email);

            // Ejecutar la consulta de manera asíncrona
            ApiFuture<QuerySnapshot> querySnapshot = query.get();

            // Obtener los documentos de la consulta
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

            // Verificar si se encontró algún documento
            if (!documents.isEmpty()) {
                // Tomar el primer documento encontrado
                DocumentSnapshot document = documents.get(0);

                // Depuración: mostrar los datos completos del documento
                System.out.println("📄 Datos del documento encontrado: " + document.getData());

                // Obtener el rol del documento
                String role = document.getString("rol");

                // Depuración: mostrar el rol obtenido
                System.out.println("🎭 Rol obtenido desde Firestore: " + role);

                // Si el rol es válido, devolverlo
                if (role != null && !role.isEmpty()) {
                    return role;
                } else {
                    System.out.println("⚠️ El campo 'rol' está vacío o es nulo.");
                }
            } else {
                System.out.println("⚠️ No se encontró ningún usuario con el correo: " + email);
            }

            // Si no se encuentra el usuario o el rol, devolver "Usuario"
            return "Usuario";

        } catch (InterruptedException | ExecutionException e) {
            // Mostrar el error si algo sale mal
            System.err.println("❌ Error obteniendo el rol: " + e.getMessage());
            return "Usuario"; // Si ocurre algún error, se asigna "Usuario" por defecto
        }
    }


}

