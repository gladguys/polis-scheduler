package com.gladguys.polisscheduler.services.firestore;

import com.gladguys.polisscheduler.model.Usuario;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreUsuariosService {

    private final Firestore db;

    public FirestoreUsuariosService(Firestore db) {
        this.db = db;
    }

    public List<Usuario> getUsuariosSeguidoresDoPolitico(String politicoId) {
        List<Usuario> usuarios = new ArrayList<>();
        try {
            List<QueryDocumentSnapshot> documents = db.collection("usuarios_seguindo")
                    .document(politicoId)
                    .collection("usuariosSeguindo")
                    .get().get().getDocuments();

            for (var doc: documents) {
                usuarios.add(doc.toObject(Usuario.class));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return usuarios;
    }
}
