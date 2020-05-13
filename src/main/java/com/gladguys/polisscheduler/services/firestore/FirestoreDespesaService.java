package com.gladguys.polisscheduler.services.firestore;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    private String montaIdDespesa(Despesa d) {
        return d.getDataDocumento().replace("-", "") + d.getIdPolitico()
                + d.getValorDocumento().replace(".", "") + d.getCodDocumento();
    }

    public void salvarTotalDespesaPoliticoPorMes(String id, String mes, BigDecimal totalDespesas) {
        Map<String, Object> mesValor = new HashMap<>();
        mesValor.put("total", totalDespesas.doubleValue());
        db.collection("totalizador_despesas")
                .document(id)
                .collection("totalPorMes")
                .document(mes)
                .set(mesValor);
    }
}