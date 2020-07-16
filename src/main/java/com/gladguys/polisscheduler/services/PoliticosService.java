package com.gladguys.polisscheduler.services;

import com.gladguys.polisscheduler.builder.PoliticoBuilder;
import com.gladguys.polisscheduler.exceptions.ApiCamaraDeputadosException;
import com.gladguys.polisscheduler.model.*;
import com.gladguys.polisscheduler.repository.PoliticoRepository;
import com.gladguys.polisscheduler.services.firestore.FirestorePartidoService;
import com.gladguys.polisscheduler.services.firestore.FirestorePoliticoService;

import com.gladguys.polisscheduler.services.firestore.FirestoreProposicaoService;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class PoliticosService {

    private static final String URI_POLITICOS = "https://dadosabertos.camara.leg.br/apiiii/v22/deputados?ordem=ASC&ordenarPor=nome";

    private final RestTemplate restTemplate;
    private final FirestoreService firestoreService;
    private final FirestorePoliticoService firestorePoliticoService;
    private final FirestorePartidoService firestorePartidoService;
    private final FirestoreProposicaoService firestoreProposicaoService;
    private final PoliticoRepository politicoRepository;


    public PoliticosService(RestTemplateBuilder restTemplateBuilder,
                            FirestoreService firestoreService,
                            FirestorePoliticoService firestorePoliticoService,
                            FirestorePartidoService firestorePartidoService,
                            FirestoreProposicaoService firestoreProposicaoService,
                            PoliticoRepository politicoRepository) {
        this.restTemplate = restTemplateBuilder.build();
        this.firestoreService = firestoreService;
        this.firestorePoliticoService = firestorePoliticoService;
        this.firestorePartidoService = firestorePartidoService;
        this.firestoreProposicaoService = firestoreProposicaoService;
        this.politicoRepository = politicoRepository;
    }

    public void salvaPoliticos() {
        try {
            RetornoApiPoliticosSimples retornoApiPoliticos = this.restTemplate.getForObject(URI_POLITICOS, RetornoApiPoliticosSimples.class);
            List<PoliticoSimples> politicos = retornoApiPoliticos.getDados();
            politicos.forEach(this::salvaPolitico);
            this.firestoreService.updateHashCodeSyncPoliticos();
        } catch (RestClientException restClientException) {
            throw new ApiCamaraDeputadosException("Erro ao tentar acessar a API camara dos deputados na url: " + URI_POLITICOS, restClientException);
        }
    }

    private void salvaPolitico(PoliticoSimples ps) {
        PoliticoCompleto pCompleto = this.restTemplate.getForObject(ps.getUri(),
                RetornoApiPoliticosCompleto.class).dados;
        Politico politico = PoliticoBuilder.build(pCompleto);

        var partidoOpt = firestorePartidoService.getById(politico.getSiglaPartido());
        politico.setUrlPartidoLogo(partidoOpt.orElse(null).getLogo());

        firestorePoliticoService.addPolitico(politico);
    }

    public void atualizarRankingDespesas() throws ExecutionException, InterruptedException {
        var resultadoRanking = new ComparativoRankingDespesas();

        List<Politico> politicos = this.firestorePoliticoService.getPoliticos();
        politicos.sort(Comparator.comparing(p -> p.getTotalDespesas()));

        List<Politico> politicosMenosGasto =
                politicos.stream().filter(p -> p.getTotalDespesas().equals(0.0)).collect(Collectors.toList());

        if (politicosMenosGasto.size() > 0) {
            resultadoRanking.setDadosPoliticoPrimeiro(politicosMenosGasto);
        } else {
            resultadoRanking.setDadosPoliticoPrimeiro(List.of(politicos.get(0)));
        }
        resultadoRanking.setDadosPoliticoUltimo(politicos.get(politicos.size() - 1));

        Double totalDespesasTodosPoliticos = politicos.stream().map(Politico::getTotalDespesas).reduce(Double::sum).get();
        resultadoRanking.setDespesaMedia(totalDespesasTodosPoliticos / politicos.size());

        for (int pos = politicos.size(); pos >= 1; pos--) {
            firestorePoliticoService.atualizarPosicaoRankingDespesaPolitico(politicos.get(pos - 1).getId(), pos);
        }
        firestorePoliticoService.salvarResultadosRanking(resultadoRanking);
    }

    public void updateTotalizadorPLsPoliticos() throws ExecutionException, InterruptedException {
        var politicos = firestorePoliticoService.getPoliticos();
        politicos.forEach(p -> {
            try {
                firestoreProposicaoService.updateTotalizadorPLsPolitico(p.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
