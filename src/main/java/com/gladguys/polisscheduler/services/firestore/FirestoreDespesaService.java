package com.gladguys.polisscheduler.services.firestore;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.gladguys.polisscheduler.model.Despesa;
import com.gladguys.polisscheduler.repository.DespesasRepository;
import com.google.cloud.firestore.Firestore;

import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.stereotype.Service;

@Service
public class FirestoreDespesaService {

    private final Firestore db;
    private final FirestorePoliticoService firestorePoliticoService;
    private DespesasRepository despesasRepository;

    public FirestoreDespesaService(Firestore firestore,
                                   FirestorePoliticoService firestorePoliticoService,
                                   DespesasRepository despesasRepository) {
        this.db = firestore;
        this.firestorePoliticoService = firestorePoliticoService;

        this.despesasRepository = despesasRepository;
    }

    public void salvarDespesas(Despesa despesa, String politicoId) {
        try {
            db.collection("atividades")
                    .document(politicoId)
                    .collection("atividadesPolitico")
                    .document(despesa.getId())
                    .set(despesa).get();

            despesasRepository.inserirDespesa(despesa);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String salvarDespesa(Despesa despesa) throws ExecutionException, InterruptedException {
            var despesaId = UUID.randomUUID().toString();
        db.collection("atividades")
                .document(despesa.getIdPolitico())
                .collection("atividadesPolitico")
                .document(despesaId)
                .set(despesa).get();
        despesasRepository.inserirDespesa(despesa);
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

    public void salvarTotalDespesaPoliticoPorMes(String id, String ano, String mes, BigDecimal totalDespesas) {
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