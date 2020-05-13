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

    public void salvarProposicoes(String data) throws InterruptedException, ExecutionException {
        if (data == null) {
            data = DataUtil.getDataOntem();
        }
        salvarProposicoesNoFirestore(data);
        atualizaTramitacoes(data);
    }

    private void salvarProposicoesNoFirestore(String data) throws InterruptedException, ExecutionException {
        List<String> politicosId =
                firestorePoliticoService
                        .getPoliticos()
                        .parallelStream()
                        .map(p -> p.getId())
                        .collect(Collectors.toList());

        String urlProposicoes = URI_PROPOSICAO + "?dataApresentacaoInicio=" + data
                + "&dataApresentacaoFim=" + data + "&itens=100000";

        RetornoApiProposicoes retornoApiProposicoes =
                this.restTemplate.getForObject(urlProposicoes, RetornoApiProposicoes.class);

        List<RetornoApiSimples> retSimplesProposicoes = retornoApiProposicoes.dados;

        int pagina = 2;
        while (retornoApiProposicoes.temMaisPaginasComConteudo()) {
            urlProposicoes = URI_PROPOSICAO + "?dataApresentacaoInicio=" + data
                    + "&dataApresentacaoFim=" + data + "&pagina=" + pagina
                    + "&itens=100000";
            retornoApiProposicoes = this.restTemplate.getForObject(urlProposicoes,
                    RetornoApiProposicoes.class);
            retSimplesProposicoes.addAll(retornoApiProposicoes.dados);
            pagina++;
        }

        try {
            retSimplesProposicoes.parallelStream().forEach(prop -> {
                System.out.println("Começando com proposicao " + prop.getUri() + " ...");
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
                        System.out.println("salvando proposicao " + proposicao.getId());

                        var tramitacoes = getTramitacoesDaAPI(proposicao, data);
                        if (tramitacoes.size() > 0) {
                            proposicao.atualizaDadosUltimaTramitacao(
                                    Collections.max(tramitacoes, Comparator.comparing(Tramitacao::getSequencia)));
                            System.out.println("tramitacoes a salvar da proposicao " + proposicao.getId());
                            firestoreProposicaoService.salvarTramitacoesProposicao(tramitacoes,
                                    proposicao.getId());
                            System.out.println("tramitacoes foram salvas");
                        }

                        firestoreProposicaoService.salvarProposicao(proposicao);
                        System.out.println("proposicao  " + proposicao.getId() + " foi salva");

                    }
                }
            });
        } catch (Exception e) {
            System.err.println(e);
            throw e;
        }
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

    public void atualizaTramitacoes(String data) {
        try {
            List<Proposicao> proposicoesNoFirestore = firestoreProposicaoService.getProposicoes();
            proposicoesNoFirestore.forEach(p -> atualizaTramitacao(p, data));
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void atualizaTramitacao(Proposicao proposicao, String data) {
        try {
            List<Tramitacao> tramitacoesNovas = getTramitacoesDaAPI(proposicao, data);

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

    private List<Tramitacao> getTramitacoesDaAPI(Proposicao proposicao, String data) {
        return this.restTemplate.getForObject(
                URI_PROPOSICAO + "/" + proposicao.getId() + "/tramitacoes?dataFim=" + data,
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
        //proposicao.setDataApresentacao(data);
        //proposicao.setDataAtualizacao(data);
        proposicao.setEstadoPolitico("RN");
        proposicao.setSiglaPartido("REPUBLICANOS");

        firestoreProposicaoService.salvarProposicao(proposicao);
    }

    public void deletarTodasProposicoes() throws ExecutionException, InterruptedException {
        firestoreProposicaoService.deletarTodasProposicoes();
        firestorePoliticoService.limparTotalizadorProposicoesPoliticos();
    }
}