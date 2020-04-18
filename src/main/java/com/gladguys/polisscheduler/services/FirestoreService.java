package com.gladguys.polisscheduler.services;

import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FirestoreService {

	private final Firestore db;

	public FirestoreService(Firestore firestore) {
		this.db = firestore;
	}

	public void updateHashCodeSyncPartidos() {
		String hash = LocalDateTime.now().toString();
		db.collection("sync_log").document("PARTIDOSYNC").update("hash", hash);
	}

	public void updateHashCodeSyncPoliticos() {
		String hash = LocalDateTime.now().toString();
		db.collection("sync_log").document("POLITICOSYNC").update("hash", hash);
	}
}
