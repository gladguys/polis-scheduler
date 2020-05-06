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
import com.gladguys.polisscheduler.model.RetornoApiTramitacoes;
import com.gladguys.polisscheduler.model.Tramitacao;
import com.gladguys.polisscheduler.services.firestore.FirestorePoliticoService;
import com.gladguys.polisscheduler.services.firestore.FirestoreProposicaoService;
import com.gladguys.polisscheduler.utils.DataUtil;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ProposicaoService {

        private static final String URI_PROPOSICAO = "https://dadosabertos.camara.leg.br/api/v2/proposicoes";

        private final RestTemplate restTemplate;
        private final FirestoreProposicaoService firestoreService;
        private final FirestorePoliticoService firestorePoliticoService;

        public ProposicaoService(RestTemplateBuilder restTemplateBuilder, FirestoreProposicaoService firestoreService,
                        FirestorePoliticoService firestorePoliticoService) {
                this.restTemplate = restTemplateBuilder.build();
                this.firestoreService = firestoreService;
                this.firestorePoliticoService = firestorePoliticoService;
        }

        // @Scheduled(cron = "0 48 05 * * ?")
        public void salvarProposicoes() throws InterruptedException, ExecutionException {

                List<String> politicosId = firestorePoliticoService.getPoliticos().stream().map(p -> p.getId())
                                .collect(Collectors.toList());

                String urlProposicoes = URI_PROPOSICAO + "?dataApresentacaoInicio=" + DataUtil.getDataOntem()
                                + "&dataApresentacaoFim=" + DataUtil.getDataOntem() + "&itens=100000";

                RetornoApiProposicoes retornoApiProposicoes = this.restTemplate.getForObject(urlProposicoes,
                                RetornoApiProposicoes.class);

                List<RetornoApiSimples> retSimplesProposicoes = retornoApiProposicoes.dados;
                int pagina = 2;
                while (retornoApiProposicoes.temMaisPaginasComConteudo()) {
                        urlProposicoes = URI_PROPOSICAO + "?dataApresentacaoInicio=" + DataUtil.getDataOntem()
                                        + "&dataApresentacaoFim=" + DataUtil.getDataOntem() + "&pagina=" + pagina
                                        + "&itens=100000";
                        retornoApiProposicoes = this.restTemplate.getForObject(urlProposicoes,
                                        RetornoApiProposicoes.class);

                        retSimplesProposicoes.addAll(retornoApiProposicoes.dados);

                        pagina++;
                }

                try {

                        retSimplesProposicoes.stream().forEach(prop -> {
                                ProposicaoCompleto proposicaoCompleto = this.restTemplate.getForObject(prop.getUri(),
                                                RetornoApiProposicaoCompleto.class).dados;

                                List<RetornoApiSimples> autores = this.restTemplate
                                                .getForObject(proposicaoCompleto.getUriAutores(),
                                                                RetornoApiAutoresProposicao.class)
                                                .getDados();

                                RetornoApiSimples retPolitico = null;

                                if (autores.size() > 0) {
                                        retPolitico = autores.get(0);
                                }

                                if (retPolitico != null && retPolitico.getUri() != null && retPolitico.getUri() != "") {

                                        PoliticoCompleto politicoRetorno = this.restTemplate.getForObject(
                                                        retPolitico.getUri(), RetornoApiPoliticosCompleto.class).dados;

                                        if (politicoRetorno.getId() != null
                                                        && politicosId.contains(politicoRetorno.getId())) {

                                                Proposicao proposicao = proposicaoCompleto.build();
                                                proposicao.configuraDadosPoliticoNaProposicao(politicoRetorno);
                                                firestoreService.salvarProposicao(proposicao);

                                                List<Tramitacao> tramitacoes = getTramitacoes(proposicao);
                                                firestoreService.salvarTramitacoesProposicao(tramitacoes,proposicao.getId());
                                        }
                                }

                        });
                } catch (Exception e) {
                        System.err.println(e);
                        throw e;
                }

        }

        private List<Tramitacao> getTramitacoes(Proposicao proposicao) {
                return this.restTemplate.getForObject(
                        URI_PROPOSICAO + 
                        "/" + 
                        proposicao.getId() + 
                        "/tramitacoes",
                        RetornoApiTramitacoes.class).dados;
                                                
        }

        public void deletaProposicoes() {
                firestoreService.deleteAllProposicoes();
        }

}