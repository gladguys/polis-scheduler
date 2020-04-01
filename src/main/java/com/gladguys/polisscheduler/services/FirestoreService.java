package com.gladguys.polisscheduler.services;

import com.gladguys.polisscheduler.model.Despesa;
import com.gladguys.polisscheduler.model.Partido;
import com.gladguys.polisscheduler.model.Politico;
import com.gladguys.polisscheduler.model.Proposicao;
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
import java.util.stream.Collectors;

@Service
public class FirestoreService {

	private final Firestore db;

	public FirestoreService(Firestore firestore) {
		this.db = firestore;
	}

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

	public List<Politico> getPoliticos() throws InterruptedException, ExecutionException {
		List<Politico> politicos = new ArrayList<>();
		ApiFuture<QuerySnapshot> future = db.collection("politicos").get();
		List<QueryDocumentSnapshot> documents = future.get().getDocuments();
		for (DocumentSnapshot document : documents) {
			politicos.add(document.toObject(Politico.class));
		}
		return politicos;
	}

	public void salvarDespesas(List<Despesa> despesas, String politicoId) {
		despesas.forEach(d -> {
			db.collection("atividades").document(politicoId).collection("despesasPolitico").add(d);
		});
	}

	public void salvarPartidos(List<Partido> partidos) {
		partidos.forEach(p -> {
			db.collection("partidos").document(p.getId()).set(p);
		});
	}

	public void salvarProposicao(Proposicao proposicao) {

		db.collection("atividades").document(proposicao.getIdPoliticoAutor()).collection("proposicoesPolitico")
				.document(proposicao.getId()).create(proposicao);
	}

	public void deleteAllProposicoes() {

		try {
			List<String> politicosId = getPoliticos().stream().map(p -> p.getId()).collect(Collectors.toList());
			politicosId.forEach(p -> {
				db.collection("atividades").document(p).collection("proposicoesPolitico").listDocuments().forEach(d -> d.delete());
				db.collection("atividades").document(p).collection("despesasPolitico").listDocuments().forEach(d -> d.delete());
			});
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
	}

}
