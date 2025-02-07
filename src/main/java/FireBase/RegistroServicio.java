package FireBase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.WriteResult;
import com.google.api.core.ApiFuture;
import org.example.Conexion;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class RegistroServicio {
    private FirebaseAuth auth;
    private Firestore db;

    public RegistroServicio() {
        // Conectar con Firebase
        Conexion.conectarFirebase();
        auth = FirebaseAuth.getInstance();
        db = Conexion.db;

        if (db == null) {
            System.err.println("❌ Error: Firestore no está conectado.");
        }
    }

    public boolean registrarUsuario(String nombre, String apellido, String usuario, String correo, String password, String rol) {
        if (nombre.isEmpty() || apellido.isEmpty() || usuario.isEmpty() || correo.isEmpty() || password.isEmpty()) {
            System.err.println("⚠️ Error: Todos los campos son obligatorios.");
            return false;
        }

        // Encriptar contraseña
        String passwordEncriptada = BCrypt.hashpw(password, BCrypt.gensalt());

        // Verificar conexión con Firestore
        if (db == null) {
            System.err.println("❌ Error: No hay conexión con Firestore.");
            return false;
        }

        // Datos del usuario
        Map<String, Object> user = new HashMap<>();
        user.put("nombre", nombre);
        user.put("apellido", apellido);
        user.put("usuario", usuario);
        user.put("correo", correo);
        user.put("contraseña", passwordEncriptada);
        user.put("tipo_usuario", rol);

        // Guardar en Firebase Authentication
        try {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(correo)
                    .setPassword(password);

            UserRecord userRecord = auth.createUser(request);
            String uid = userRecord.getUid();

            // Guardar datos en Firestore
            DocumentReference docRef = db.collection("users").document(uid);
            ApiFuture<WriteResult> future = docRef.set(user);
            WriteResult result = future.get();

            System.out.println("✅ Usuario registrado correctamente en Firestore con UID: " + uid);
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al registrar usuario en Firestore: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("❌ Error al registrar usuario en Firebase Auth: " + e.getMessage());
            return false;
        }
    }
}
