package com.gladguys.polisscheduler.services.firestore;

import java.util.List;
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
			db.collection("partidos").document(p.getId()).set(p);
		});
	}
}