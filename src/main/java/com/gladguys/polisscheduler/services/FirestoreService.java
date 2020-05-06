package com.gladguys.polisscheduler.services;

import com.gladguys.polisscheduler.services.firestore.FirestorePoliticoService;
import com.google.cloud.firestore.Firestore;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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
