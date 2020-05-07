package com.gladguys.polisscheduler.services.firestore;

import java.util.List;

import com.gladguys.polisscheduler.model.Despesa;
import com.google.cloud.firestore.Firestore;

import org.springframework.stereotype.Service;

@Service
public class FirestoreDespesaService {

    private final Firestore db;

	public FirestoreDespesaService(Firestore firestore) {
		this.db = firestore;
    }
    
    public void salvarDespesas(List<Despesa> despesas, String politicoId) {
		try {
			despesas.forEach(d -> {
				System.out.println("ID POLITICO: " + d.getIdPolitico());
				db.collection("atividades").document(politicoId).collection("atividadesPolitico")
						.document(d.getDataDocumento().replace("-", "") + d.getIdPolitico()
								+ d.getValorDocumento().replace(".", "") + d.getCodDocumento())
						.create(d);
			});
		} catch (Exception e) {
			System.err.println(e);
		}
	}
}