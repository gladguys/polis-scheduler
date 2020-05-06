package com.gladguys.polisscheduler.services.firestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.gladguys.polisscheduler.model.Politico;
import com.gladguys.polisscheduler.model.Proposicao;
import com.gladguys.polisscheduler.model.Tramitacao;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

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

    public List<Proposicao> getProposicoes() throws InterruptedException, ExecutionException {
        final List<Proposicao> proposicoes = new ArrayList<>();

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

    public int getQuantidadeTramitacoes(String proposicaoId) throws InterruptedException, ExecutionException {

        return db.collection("tramitacoes").document(proposicaoId).collection("tramitacoesProposicao").get().get()
                .getDocuments().size();
    }

    private List<Proposicao> getProposicoesPoliticoByIdPolitico(String idPolitico)
            throws InterruptedException, ExecutionException {

        List<Proposicao> proposicoesPolitico = new ArrayList<>();

        final ApiFuture<QuerySnapshot> future = db.collection("atividades").document(idPolitico)
                .collection("atividadesPolitico").whereEqualTo("tipoAtividade", "PROPOSICAO").get();

        final List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        for (final DocumentSnapshot document : documents) {
            proposicoesPolitico.add(document.toObject(Proposicao.class));
        }
        return proposicoesPolitico;
    }

}