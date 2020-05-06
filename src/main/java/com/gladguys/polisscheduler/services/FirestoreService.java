package com.gladguys.polisscheduler.services;

import com.gladguys.polisscheduler.model.Politico;
import com.gladguys.polisscheduler.model.Proposicao;
import com.gladguys.polisscheduler.services.firestore.FirestorePoliticoService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class FirestoreService {

	private final Firestore db;
	private FirestorePoliticoService firestorePoliticoService;

	public FirestoreService(final Firestore firestore, FirestorePoliticoService firestorePoliticoService) {
		this.db = firestore;
		this.firestorePoliticoService = firestorePoliticoService;
	}

	public void updateHashCodeSyncPartidos() {
		db.collection("sync_log").document("PARTIDOSYNC").set(getMapAttrHashValue());
	}

	public void updateHashCodeSyncPoliticos() {
		db.collection("sync_log").document("POLITICOSYNC").set(getMapAttrHashValue());
	}

	public void updateHashCodeSyncOrgaos() {
		db.collection("sync_log").document("ORGAOSYNC").set(getMapAttrHashValue());
	}

	public List<Proposicao> getProposicoes() throws InterruptedException, ExecutionException {
		final List<Proposicao> proposicoes = new ArrayList();

		final List<String> politicosIds = firestorePoliticoService.getPoliticos().stream().map(Politico::getId)
				.collect(Collectors.toList());

		politicosIds.forEach(id -> {
			try {
				proposicoes.addAll(getProposicoesPoliticoByIdPolitico(id));
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		});

		return proposicoes;
	}

	private List<Proposicao> getProposicoesPoliticoByIdPolitico(String idPolitico)
			throws InterruptedException, ExecutionException {
				
		List<Proposicao> proposicoesPolitico = new ArrayList();

		final ApiFuture<QuerySnapshot> future = 
			db.collection("atividades")
				.document(idPolitico)
				.collection("atividadesPolitico")
				.whereEqualTo("tipoAtividade", "PROPOSICAO")
				.get();
		
				final List<QueryDocumentSnapshot> documents = future.get().getDocuments();
		for (final DocumentSnapshot document : documents) {
			proposicoesPolitico.add(document.toObject(Proposicao.class));
		}
		return proposicoesPolitico;
	}

	private Map<String, Object> getMapAttrHashValue() {
		final String hash = LocalDateTime.now().toString();
		final Map<String, Object> data = new HashMap<>();
		data.put("hash", hash);
		return data;
	}
}
