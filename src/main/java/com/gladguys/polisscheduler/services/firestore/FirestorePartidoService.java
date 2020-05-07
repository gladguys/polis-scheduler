package com.gladguys.polisscheduler.services.firestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.gladguys.polisscheduler.model.Partido;
import com.google.cloud.firestore.DocumentReference;
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

	public Partido getById(String partidoId) {
		var partido = db.collection("partidos").document(partidoId).get();
		try {
			return partido.get().toObject(Partido.class);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
}