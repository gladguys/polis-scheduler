package com.gladguys.polisscheduler.services.firestore;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.gladguys.polisscheduler.model.Partido;
import com.google.cloud.firestore.Firestore;

import org.springframework.stereotype.Service;

@Service
public class FirestorePartidoService {

    private final Firestore db;

    public FirestorePartidoService(Firestore firestore) {
        this.db = firestore;
    }

    public void salvarPartidos(List<Partido> partidos) {
        partidos.forEach(p -> {
            db.collection("partidos").document(p.getId()).update(p.parseToMap());
        });
    }

    public Optional<Partido> getById(String partidoSigla) {
        try {
            var partido =
                    db.collection("partidos")
                            .whereEqualTo("sigla", partidoSigla)
                            .get()
                            .get()
                            .getDocuments()
                            .get(0);

            return Optional.of(partido.toObject(Partido.class));

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}