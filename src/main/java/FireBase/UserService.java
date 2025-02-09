package FireBase;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class UserService {

    private Firestore db;

    public UserService() {
        db = FirestoreClient.getFirestore();
    }

    // Método para obtener los paquetes turísticos desde Firestore
    public List<Map<String, Object>> obtenerPaquetes() throws InterruptedException, ExecutionException {
        List<Map<String, Object>> paquetes = new ArrayList<>();

        // Obtener los documentos de la colección "paquetes"
        ApiFuture<QuerySnapshot> future = db.collection("paquetes").get();
        QuerySnapshot querySnapshot = future.get();

        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            // Extraer los datos del paquete
            Map<String, Object> paquete = new HashMap<>();
            paquete.put("nombre", document.getString("nombre"));
            paquete.put("descripción", document.getString("descripción"));
            paquete.put("precio", document.getDouble("precio"));
            paquete.put("duracion", document.getString("duracion"));
            paquete.put("imagenURL", document.getString("imagenURL"));

            paquetes.add(paquete);
        }

        return paquetes;
    }
}
