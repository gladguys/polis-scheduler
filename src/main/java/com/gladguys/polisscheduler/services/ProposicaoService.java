package com.gladguys.polisscheduler.services;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.gladguys.polisscheduler.model.PoliticoCompleto;
import com.gladguys.polisscheduler.model.Proposicao;
import com.gladguys.polisscheduler.model.ProposicaoCompleto;
import com.gladguys.polisscheduler.model.RetornoApiAutoresProposicao;
import com.gladguys.polisscheduler.model.RetornoApiPoliticosCompleto;
import com.gladguys.polisscheduler.model.RetornoApiProposicaoCompleto;
import com.gladguys.polisscheduler.model.RetornoApiProposicoes;
import com.gladguys.polisscheduler.model.RetornoApiSimples;
import com.gladguys.polisscheduler.utils.DataUtil;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ProposicaoService {

    private static final String URI_PROPOSICAO = "https://dadosabertos.camara.leg.br/api/v2/proposicoes";

    private final RestTemplate restTemplate;
    private final FirestoreService firestoreService;

    public ProposicaoService(RestTemplateBuilder restTemplateBuilder, FirestoreService firestoreService) {
        this.restTemplate = restTemplateBuilder.build();
        this.firestoreService = firestoreService;
    }

    // @Scheduled(cron = "0 48 05 * * ?")
    public void salvarProposicoes() throws InterruptedException, ExecutionException {

        List<String> politicosId = firestoreService.getPoliticos().stream().map(p -> p.getId())
                .collect(Collectors.toList());

        String urlProposicoes = URI_PROPOSICAO + "?dataInicio=" + DataUtil.getDataOntem() + "&dataFim="
                + DataUtil.getDataOntem() + "&itens=100000";

        List<RetornoApiSimples> retSimplesProposicoes = this.restTemplate.getForObject(urlProposicoes,
                RetornoApiProposicoes.class).dados;

        retSimplesProposicoes.stream().forEach(prop -> {
            ProposicaoCompleto proposicaoCompleto = this.restTemplate.getForObject(prop.getUri(),
                    RetornoApiProposicaoCompleto.class).dados;

            RetornoApiSimples retPolitico = this.restTemplate
                    .getForObject(proposicaoCompleto.getUriAutores(), RetornoApiAutoresProposicao.class).getDados()
                    .get(0);

            if (retPolitico.getUri() != null && retPolitico.getUri() != "") {

                PoliticoCompleto politicoRetorno = this.restTemplate.getForObject(retPolitico.getUri(),
                        RetornoApiPoliticosCompleto.class).dados;

                if (politicosId.contains(politicoRetorno.getId())) {

                    Proposicao proposicao = proposicaoCompleto.build();
                    proposicao.setNomePoliticoAutor(politicoRetorno.getUltimoStatus().getNomeEleitoral());
                    proposicao.setIdPoliticoAutor(politicoRetorno.getId());
                    firestoreService.salvarProposicao(proposicao);
                }
            }

        });
    }

}