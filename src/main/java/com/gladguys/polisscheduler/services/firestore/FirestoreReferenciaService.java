package com.gladguys.polisscheduler.services.firestore;

import java.util.List;

import com.gladguys.polisscheduler.model.Orgao;
import com.google.cloud.firestore.Firestore;

import org.springframework.stereotype.Service;

@Service
public class FirestoreReferenciaService {

    private final Firestore db;

    public FirestoreReferenciaService(Firestore firestore) {
        this.db = firestore;
    }

    public void salvarOrgaos(List<Orgao> orgaos) {
        orgaos.forEach(orgao -> {
            String sigla = orgao.getSigla();
            if (sigla != null && !sigla.equals("")) {
                sigla = sigla.replaceAll("/| |\\.", "-");
                db.collection("orgaos").document(sigla).set(orgao);
            }
        });
    }

}