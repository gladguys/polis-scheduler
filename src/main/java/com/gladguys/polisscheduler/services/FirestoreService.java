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

	private Map<String, Object> getMapAttrHashValue() {
		final String hash = LocalDateTime.now().toString();
		final Map<String, Object> data = new HashMap<>();
		data.put("hash", hash);
		return data;
	}
}
