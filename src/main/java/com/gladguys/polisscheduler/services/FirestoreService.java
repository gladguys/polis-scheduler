package com.gladguys.polisscheduler.services;

import com.gladguys.polisscheduler.model.Despesa;
import com.gladguys.polisscheduler.model.Politico;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreService {

	private final Firestore db;

	public FirestoreService(Firestore firestore) {
		this.db = firestore;
	}

	// @Scheduled(cron = "0 32 23 * * ?")
	// @Scheduled(fixedRate = 1000)
	public void getUsers() throws IOException, ExecutionException, InterruptedException {

		ApiFuture<QuerySnapshot> query = db.collection("users").get();
		QuerySnapshot querySnapshot = query.get();
		List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
		for (QueryDocumentSnapshot document : documents) {
			System.out.println("User: " + document.getId());
		}
	}

	public void addPolitico(Politico politico) {
		ApiFuture<WriteResult> future = db.collection("politicos").document(politico.getId()).set(politico);
	}

	public List<String> getPoliticosIds() throws InterruptedException, ExecutionException {
		List<String> politicosIds = new ArrayList<>();
		ApiFuture<QuerySnapshot> future = db.collection("politicos").get();
		List<QueryDocumentSnapshot> documents = future.get().getDocuments();
		for (DocumentSnapshot document : documents) {
			politicosIds.add(document.getId());	
		}
		
		return politicosIds;
	}

	public void salvarDespesas(List<Despesa> despesas, String politicoId) {
		despesas.forEach(d -> {
			db.collection("atividades").document(politicoId).collection("despesasPolitico").add(d); 
		});
	}
}
