package com.gladguys.polisscheduler.services.firestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.gladguys.polisscheduler.model.Partido;
import com.gladguys.polisscheduler.model.Politico;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import org.springframework.stereotype.Service;

@Service
public class FirestorePoliticoService {

    private final Firestore db;

	public FirestorePoliticoService(Firestore firestore) {
		this.db = firestore;
	}

	public void addPolitico(Politico politico) {
		db.collection("politicos").document(politico.getId()).set(politico);
	}

	public List<Politico> getPoliticos() throws InterruptedException, ExecutionException {
		List<Politico> politicos = new ArrayList<>();
		ApiFuture<QuerySnapshot> future = db.collection("politicos").get();
		List<QueryDocumentSnapshot> documents = future.get().getDocuments();
		for (DocumentSnapshot document : documents) {
			politicos.add(document.toObject(Politico.class));
		}
		return politicos;
	}

	public void salvarPartidos(List<Partido> partidos) {
		partidos.forEach(p -> {
			db.collection("partidos").document(p.getId()).set(p);
		});
	}

    public Politico getPoliticoById(String id) throws ExecutionException, InterruptedException {
		ApiFuture<DocumentSnapshot> future = db.collection("politicos").document(id).get();
		return future.get().toObject(Politico.class);
	}
}