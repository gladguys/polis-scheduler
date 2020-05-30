package com.gladguys.polisscheduler.services.firestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.gladguys.polisscheduler.model.Politico;
import com.gladguys.polisscheduler.model.PoliticoProposicao;
import com.gladguys.polisscheduler.model.Proposicao;
import com.gladguys.polisscheduler.model.Tramitacao;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;

import org.springframework.stereotype.Service;

@Service
public class FirestoreProposicaoService {

    public static final String ATIVIDADES_POLITICO = "atividadesPolitico";
    public static final String ATIVIDADES = "atividades";
    public static final String TRAMITACOES = "tramitacoes";
    public static final String TRAMITACOES_PROPOSICAO = "tramitacoesProposicao";
    public static final String TIPO_ATIVIDADE = "tipoAtividade";
    public static final String PROPOSICAO = "PROPOSICAO";
    private final Firestore db;
    private final FirestorePoliticoService firestorePoliticoService;

    public FirestoreProposicaoService(Firestore firestore, FirestorePoliticoService firestorePoliticoService) {
        this.db = firestore;
        this.firestorePoliticoService = firestorePoliticoService;
    }

    public Proposicao salvarProposicao(Proposicao proposicao) {

        try {
            db.collection(ATIVIDADES).document(proposicao.getIdPoliticoAutor()).collection(ATIVIDADES_POLITICO)
                    .document(proposicao.getId()+proposicao.getIdPoliticoAutor()).set(proposicao);
            return proposicao;
        } catch (Exception e) {
            return null;
        }
    }

    public void deletarTodasProposicoes() throws ExecutionException, InterruptedException {
        List<String> politicosId = firestorePoliticoService.getPoliticos().stream().map(p -> p.getId())
                .collect(Collectors.toList());
        politicosId.forEach(p -> deletarProposicoesPorPoliticoId(p));

    }

    public void salvarTramitacoesProposicao(List<Tramitacao> tramitacoes, String id) {

        db.collection(TRAMITACOES).document(id).delete();

        tramitacoes.parallelStream().forEach(t ->
                db.collection(TRAMITACOES)
                        .document(id)
                        .collection(TRAMITACOES_PROPOSICAO)
                        .document(String.valueOf(t.getSequencia())).set(t));
    }

    public List<Proposicao> getProposicoes() throws InterruptedException, ExecutionException {
        final List<Proposicao> proposicoes = new ArrayList<>();

        final List<String> politicosIds =
                firestorePoliticoService
                        .getPoliticos()
                        .stream()
                        .map(Politico::getId)
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

        return db.collection(TRAMITACOES)
                .document(proposicaoId)
                .collection(TRAMITACOES_PROPOSICAO)
                .get()
                .get()
                .getDocuments().size();
    }

    private List<Proposicao> getProposicoesPoliticoByIdPolitico(String idPolitico)
            throws InterruptedException, ExecutionException {

        List<Proposicao> proposicoesPolitico = new ArrayList<>();

        final ApiFuture<QuerySnapshot> future =
                db.collection(ATIVIDADES)
                        .document(idPolitico)
                        .collection(ATIVIDADES_POLITICO)
                        .whereEqualTo(TIPO_ATIVIDADE, PROPOSICAO)
                        .get();

        final List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        for (final DocumentSnapshot document : documents) {
            proposicoesPolitico.add(document.toObject(Proposicao.class));
        }
        return proposicoesPolitico;
    }

    private void deletarProposicoesPorPoliticoId(String p) {
        QuerySnapshot queryDocumentSnapshots = null;
        try {
            queryDocumentSnapshots = db.collection(ATIVIDADES)
                    .document(p)
                    .collection(ATIVIDADES_POLITICO)
                    .whereEqualTo(TIPO_ATIVIDADE, PROPOSICAO)
                    .get()
                    .get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (queryDocumentSnapshots.getDocuments().size() > 0) {
            queryDocumentSnapshots.getDocuments().parallelStream().forEach(document -> {
                document.getReference().delete();
            });
        }
    }

    public Proposicao getById(PoliticoProposicao politicoProposicao) {
        try {
            return db.collection(ATIVIDADES)
                    .document(politicoProposicao.getPolitico())
                    .collection(ATIVIDADES_POLITICO)
                    .document(politicoProposicao.getId())
                    .get()
                    .get()
                    .toObject(Proposicao.class);
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateTotalizadorPLsPolitico(String politicoId) throws Exception {

        try {
            var queryDocumentSnapshots  = db.collection(ATIVIDADES)
                    .document(politicoId)
                    .collection(ATIVIDADES_POLITICO)
                    .whereEqualTo("descricaoTipo", "Projeto de Lei")
                    .whereEqualTo(TIPO_ATIVIDADE, PROPOSICAO)
                    .get()
                    .get();
            var qntPls = queryDocumentSnapshots.getDocuments().size();

            firestorePoliticoService.atualizaTotalizadorPLs(politicoId, qntPls);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}