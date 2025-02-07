package FireBase;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.WriteResult;
import org.example.Conexion;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class RegistroServicio {

    public RegistroServicio() {
        Conexion.conectarFirebase(); // Asegurar la conexión
    }

    public boolean registrarUsuario(String nombre, String apellido, String usuario, String correo, String password, String rol) {
        if (nombre.isEmpty() || apellido.isEmpty() || usuario.isEmpty() || correo.isEmpty() || password.isEmpty()) {
            return false;
        }

        String passwordEncriptada = BCrypt.hashpw(password, BCrypt.gensalt());

        Firestore db = Conexion.db;
        if (db == null) {
            System.err.println("❌ Error: Firestore no está conectado.");
            return false;
        }

        Map<String, Object> user = new HashMap<>();
        user.put("nombre", nombre);
        user.put("apellido", apellido);
        user.put("usuario", usuario);
        user.put("correo", correo);
        user.put("contraseña", passwordEncriptada);
        user.put("tipo_usuario", rol);

        DocumentReference docRef = db.collection("users").document(usuario);

        try {
            WriteResult result = docRef.set(user).get();
            System.out.println("✅ Usuario registrado en Firestore.");
            return true;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }
}
//