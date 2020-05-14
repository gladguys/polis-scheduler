package com.gladguys.polisscheduler.services.firestore;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.gladguys.polisscheduler.model.Despesa;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;

import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import org.springframework.stereotype.Service;

@Service
public class FirestoreDespesaService {

    private final Firestore db;
    private final FirestorePoliticoService firestorePoliticoService;

    public FirestoreDespesaService(Firestore firestore, FirestorePoliticoService firestorePoliticoService) {
        this.db = firestore;
        this.firestorePoliticoService = firestorePoliticoService;

    }

    public void salvarDespesas(List<Despesa> despesas, String politicoId) {
        try {
            despesas.forEach(d -> {
                db.collection("atividades")
                        .document(politicoId)
                        .collection("atividadesPolitico")
                        .document(montaIdDespesa(d))
                        .create(d);
            });
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public String salvarDespesa(Despesa despesa) {
        var despesaId = UUID.randomUUID().toString();
        db.collection("atividades")
                .document(despesa.getIdPolitico())
                .collection("atividadesPolitico")
                .document(despesaId)
                .create(despesa);
        return despesaId;
    }

    public void deletarTodasDespesas() throws ExecutionException, InterruptedException {
        List<String> politicosId = firestorePoliticoService.getPoliticos().stream().map(p -> p.getId())
                .collect(Collectors.toList());
        politicosId.forEach(politicoId -> deletarDespesasPorPoliticoId(politicoId));
    }

    private void deletarDespesasPorPoliticoId(String politicoId) {
        QuerySnapshot queryDocumentSnapshots = null;
        try {
            queryDocumentSnapshots = db.collection("atividades")
                    .document(politicoId)
                    .collection("atividadesPolitico")
                    .whereEqualTo("tipoAtividade", "DESPESA")
                    .get()
                    .get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (queryDocumentSnapshots.getDocuments().size() > 0) {
            queryDocumentSnapshots.getDocuments().parallelStream().forEach(qd -> {
                qd.getReference().delete();
            });
        }
    }

    private String montaIdDespesa(Despesa d) {
        return d.getDataDocumento().replace("-", "") + d.getIdPolitico()
                + d.getValorDocumento().replace(".", "") + d.getCodDocumento();
    }


    public void salvarTotalDespesaPoliticoPorMes(String id, String ano , String mes, BigDecimal totalDespesas) {
        Map<String, Object> mesValor = new HashMap<>();
        mesValor.put("total", totalDespesas.doubleValue());
        db.collection("totalizador_despesas")
                .document(id)
                .collection("totaisAno")
                .document(ano)
                .collection("totalMes")
                .document(mes)
                .set(mesValor);
    }
}