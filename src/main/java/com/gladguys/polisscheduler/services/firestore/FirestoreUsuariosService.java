package com.gladguys.polisscheduler.services.firestore;

import com.gladguys.polisscheduler.model.Usuario;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreUsuariosService {

    private final Firestore db;

    public FirestoreUsuariosService(Firestore db) {
        this.db = db;
    }

    public List<QueryDocumentSnapshot> getUsuarioSeguidoresQueryDocSnapshot(String politicoId) {
        try {
            List<QueryDocumentSnapshot> documents = db.collection("usuarios_seguindo")
                    .document(politicoId)
                    .collection("usuariosSeguindo")
                    .get().get().getDocuments();
            return documents;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean haPermissaoParaNotificacao(String usuarioId) {
        try {
            Usuario usuario = db.collection("users").document(usuarioId).get().get().toObject(Usuario.class);
            if (usuario.getUserConfigs() != null && usuario.getUserConfigs().get("isNotificationEnabled") != null) {
                return (Boolean) usuario.getUserConfigs().get("isNotificationEnabled");
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return true;
    }

    public Set<Usuario> getTodosUsuarios() {
        Set<Usuario> usuarios = new HashSet<>();

        try {
            List<QueryDocumentSnapshot> users = db.collection("users").get().get().getDocuments();

            for (var doc : users) {
                usuarios.add(doc.toObject(Usuario.class));
            }

            return usuarios;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }
}
