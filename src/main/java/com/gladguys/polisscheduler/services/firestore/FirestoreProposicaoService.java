package com.gladguys.polisscheduler.services.firestore;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.gladguys.polisscheduler.model.Proposicao;
import com.gladguys.polisscheduler.model.Tramitacao;
import com.google.cloud.firestore.Firestore;

import org.springframework.stereotype.Service;

@Service
public class FirestoreProposicaoService {

    private final Firestore db;
    private final FirestorePoliticoService firestorePoliticoService;

    public FirestoreProposicaoService(Firestore firestore, FirestorePoliticoService firestorePoliticoService) {
        this.db = firestore;
        this.firestorePoliticoService = firestorePoliticoService;
    }

    public void salvarProposicao(Proposicao proposicao) {

        db.collection("atividades").document(proposicao.getIdPoliticoAutor()).collection("atividadesPolitico")
                .document(proposicao.getId()).set(proposicao);
    }

    public void deleteAllProposicoes() {

        try {
            List<String> politicosId = firestorePoliticoService.getPoliticos().stream().map(p -> p.getId())
                    .collect(Collectors.toList());
            politicosId.forEach(p -> {

                db.collection("atividades").document(p).collection("atividadesPolitico").listDocuments()
                        .forEach(d -> d.delete());
            });
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }

    public void salvarTramitacoesProposicao(List<Tramitacao> tramitacoes, String id) {

        db.collection("tramitacoes").document(id).delete();

        tramitacoes.forEach(t -> db.collection("tramitacoes").document(id).collection("tramitacoesProposicao").add(t));
    }

}