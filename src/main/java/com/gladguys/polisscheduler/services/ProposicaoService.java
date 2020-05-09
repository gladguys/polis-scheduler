package com.gladguys.polisscheduler.services;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.gladguys.polisscheduler.model.*;
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
    private final FirestoreProposicaoService firestoreProposicaoService;
    private final FirestorePoliticoService firestorePoliticoService;
    private final FirestoreService firestoreService;

    public ProposicaoService(RestTemplateBuilder restTemplateBuilder,
                             FirestoreProposicaoService firestoreProposicaoService,
                             FirestorePoliticoService firestorePoliticoService, FirestoreService firestoreService) {

        this.restTemplate = restTemplateBuilder.build();
        this.firestoreProposicaoService = firestoreProposicaoService;
        this.firestorePoliticoService = firestorePoliticoService;
        this.firestoreService = firestoreService;
    }

    // @Scheduled(cron = "0 48 05 * * ?")
    public void salvarProposicoes() throws InterruptedException, ExecutionException {

        List<String> politicosId =
                firestorePoliticoService
                        .getPoliticos()
                        .parallelStream()
                        .map(p -> p.getId())
                        .collect(Collectors.toList());

        String urlProposicoes = URI_PROPOSICAO + "?dataApresentacaoInicio=" + DataUtil.getDataOntem()
                + "&dataApresentacaoFim=" + DataUtil.getDataOntem() + "&itens=100000";

        RetornoApiProposicoes retornoApiProposicoes =
                this.restTemplate.getForObject(urlProposicoes, RetornoApiProposicoes.class);

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
            retSimplesProposicoes.parallelStream().forEach(prop -> {
                var proposicaoCompleto = Objects.requireNonNull(this.restTemplate.getForObject(prop.getUri(),
                        RetornoApiProposicaoCompleto.class)).dados;
                List<RetornoApiSimples> autores = this.restTemplate
                        .getForObject(proposicaoCompleto.getUriAutores(),
                                RetornoApiAutoresProposicao.class)
                        .getDados();
                RetornoApiSimples retPolitico = null;
                if (autores.size() > 0) {
                    retPolitico = autores.get(0);
                }
                if (retPolitico != null && retPolitico.getUri() != null && retPolitico.getUri() != "") {
                    var politicoRetorno =
                            Objects.requireNonNull(this.restTemplate.getForObject(
                                    retPolitico.getUri(), RetornoApiPoliticosCompleto.class)).dados;

                    if (politicoRetorno.getId() != null && politicosId.contains(politicoRetorno.getId())) {

                        var proposicao = proposicaoCompleto.build();
                        setPartidoLogoParaProposicao(politicoRetorno, proposicao);
                        proposicao.configuraDadosPoliticoNaProposicao(politicoRetorno);

                        firestoreProposicaoService.salvarProposicao(proposicao);

                        var tramitacoes = getTramitacoesDaAPI(proposicao);
                        firestoreProposicaoService.salvarTramitacoesProposicao(tramitacoes,
                                proposicao.getId());
                    }
                }
            });
        } catch (Exception e) {
            System.err.println(e);
            throw e;
        }
        atualizaTramitacoes();
    }

    private void setPartidoLogoParaProposicao(PoliticoCompleto politicoRetorno, Proposicao proposicao) {
        try {
            var politicoProposicao =
                    firestorePoliticoService.getPoliticoById(politicoRetorno.getId());
            proposicao.setUrlPartidoLogo(politicoProposicao.getUrlPartidoLogo());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void atualizaTramitacoes() {
        try {
            List<Proposicao> proposicoesNoFirestore = firestoreProposicaoService.getProposicoes();
            proposicoesNoFirestore.forEach(p -> atualizaTramitacao(p));
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void atualizaTramitacao(Proposicao proposicao) {
        try {
            List<Tramitacao> tramitacoesNovas = getTramitacoesDaAPI(proposicao);

            int quantidadeTramitacoesAtual = firestoreProposicaoService
                    .getQuantidadeTramitacoes(proposicao.getId());

            if (haAtualizacaoDeTramitacoes(tramitacoesNovas, quantidadeTramitacoesAtual)) {
                var tramiteNovoMaisRecente =
                        Collections.max(tramitacoesNovas, Comparator.comparing(Tramitacao::getSequencia));

                firestoreProposicaoService.salvarTramitacoesProposicao(tramitacoesNovas,
                        proposicao.getId());
                atualizarProposicaoComNovasTramitacoes(proposicao, tramiteNovoMaisRecente);

            }

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private boolean haAtualizacaoDeTramitacoes(List<Tramitacao> tramitacoesNovas, int quantidadeTramitacoesAtual) {
        return tramitacoesNovas.size() > 0
                && tramitacoesNovas.size() > quantidadeTramitacoesAtual;
    }

    private void atualizarProposicaoComNovasTramitacoes(Proposicao proposicao, Tramitacao ultimoTramite) {
        proposicao.setFoiAtualizada(true);
        proposicao.setVisualizado(false);
        proposicao.setDescricaoSituacao(ultimoTramite.getDescricaoSituacao());
        proposicao.setDespacho(ultimoTramite.getDespacho());
        proposicao.setDescricaoTramitacao(ultimoTramite.getDescricaoTramitacao());
        proposicao.setSequencia(ultimoTramite.getSequencia());
        proposicao.setDataAtualizacao(ultimoTramite.getDataHora());

        firestoreProposicaoService.salvarProposicao(proposicao);
    }

    private List<Tramitacao> getTramitacoesDaAPI(Proposicao proposicao) {
        return this.restTemplate.getForObject(URI_PROPOSICAO + "/" + proposicao.getId() + "/tramitacoes?dataFim=" + DataUtil.getDataOntem(),
                RetornoApiTramitacoes.class).dados;
    }

    public void deletaProposicoes() throws ExecutionException, InterruptedException {
        firestoreProposicaoService.deletarTodasProposicoes();
    }

    public void criarDummyProposicao() {

        var proposicao = new Proposicao();
        proposicao.setId(UUID.randomUUID().toString());
        proposicao.setSequencia(1);
        proposicao.setVisualizado(false);
        proposicao.setFoiAtualizada(false);
        proposicao.setIdPoliticoAutor("109429");
        proposicao.setNomePolitico("Benes Leocádio");
        proposicao.setEmenta("Uma proposicao dummy criada para teste");
        proposicao.setFotoPolitico("https://www.camara.leg.br/internet/deputado/bandep/109429.jpg");
        proposicao.setDataApresentacao("2019-01-01");
        proposicao.setDataAtualizacao("2019-01-01");
        proposicao.setEstadoPolitico("RN");
        proposicao.setSiglaPartido("REPUBLICANOS");

        firestoreProposicaoService.salvarProposicao(proposicao);
    }

    public void deletarTodasProposicoes() throws ExecutionException, InterruptedException {
        firestoreProposicaoService.deletarTodasProposicoes();
        firestorePoliticoService.limparTotalizadorProposicoesPoliticos();
    }
}