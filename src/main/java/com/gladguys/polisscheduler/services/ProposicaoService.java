package com.gladguys.polisscheduler.services;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.gladguys.polisscheduler.model.*;
import com.gladguys.polisscheduler.repository.PoliticoProposicoesRepository;
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
    private final PoliticoProposicoesRepository politicoProposicoesRepository;
    private final NotificacaoFCMService notificacaoFCMService;

    public ProposicaoService(RestTemplateBuilder restTemplateBuilder,
                             FirestoreProposicaoService firestoreProposicaoService,
                             FirestorePoliticoService firestorePoliticoService,
                             FirestoreService firestoreService,
                             PoliticoProposicoesRepository politicoProposicoesRepository,
                             NotificacaoFCMService notificacaoFCMService) {

        this.restTemplate = restTemplateBuilder.build();
        this.firestoreProposicaoService = firestoreProposicaoService;
        this.firestorePoliticoService = firestorePoliticoService;
        this.firestoreService = firestoreService;
        this.politicoProposicoesRepository = politicoProposicoesRepository;
        this.notificacaoFCMService = notificacaoFCMService;
    }

    public void salvarProposicoes(String data) throws InterruptedException, ExecutionException {
        if (data == null) {
            data = DataUtil.getDataOntem();
        }
        salvarProposicoesNoFirestore(data);
    }

    private void salvarProposicoesNoFirestore(String data) throws InterruptedException, ExecutionException {

        Set<String> politicosComProposicao = new HashSet<>();

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

                var proposicaoCompleto =
                        Objects.requireNonNull(
                                this.restTemplate.getForObject(prop.getUri(), RetornoApiProposicaoCompleto.class)).dados;

                if (temTipoDescricaoValido(proposicaoCompleto)) {
                    var autores = this.restTemplate
                            .getForObject(proposicaoCompleto.getUriAutores(), RetornoApiAutoresProposicao.class).getDados();

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

                            var tramitacoes = getTramitacoesDaAPI(proposicao.getId(), data);

                            if (tramitacoes.size() > 0) {
                                proposicao.atualizaDadosUltimaTramitacao(
                                        Collections.max(tramitacoes, Comparator.comparing(Tramitacao::getSequencia)));
                                System.out.println("tramitacoes a salvar da proposicao " + proposicao.getId());
                                firestoreProposicaoService.salvarTramitacoesProposicao(tramitacoes,
                                        proposicao.getId());
                                System.out.println("tramitacoes foram salvas");
                            }

                            //firestoreProposicaoService.salvarProposicao(proposicao);
                            /*politicoProposicoesRepository.inserirRelacaoPoliticoProposicao(
                                    proposicao.getIdPoliticoAutor(), proposicao.getId(), proposicao.getDataAtualizacao());
                            System.out.println("proposicao  " + proposicao.getId() + " foi salva");*/
                            politicosComProposicao.add(proposicao.getIdPoliticoAutor());
                        }
                    }
                }

            });
        } catch (Exception e) {
            System.err.println(e);
            throw e;
        }

        notificacaoFCMService.enviarNotificacaoParaSeguidoresDePoliticos("proposições", politicosComProposicao);


    }

    private boolean temTipoDescricaoValido(ProposicaoCompleto proposicaoCompleto) {
        var descricaoTipo = proposicaoCompleto.getDescricaoTipo();
        if (descricaoTipo.equals("Projeto de Lei") ||
                descricaoTipo.equals("Indicação") ||
                descricaoTipo.startsWith("Requerimento")) {
            return true;
        }
        return false;
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

    public void atualizaTramitacoes(String dataInformada) {
        String dataBusca;
        if (dataInformada == null) {
            dataBusca = DataUtil.getDataOntem();
        } else {
            dataBusca = dataInformada;
        }
        List<PoliticoProposicao> politicoProposicoes = politicoProposicoesRepository.getTodos();
        politicoProposicoes.forEach(p -> atualizaTramitacao(p, dataBusca));

    }

    private void atualizaTramitacao(PoliticoProposicao politicoProposicao, String data) {
        List<Tramitacao> tramitacoesNovas = getTramitacoesDaAPI(politicoProposicao.getProposicao(), data);
        var tramiteNovoMaisRecente =
                Collections.max(tramitacoesNovas, Comparator.comparing(Tramitacao::getSequencia));
        firestoreProposicaoService.salvarTramitacoesProposicao(tramitacoesNovas, politicoProposicao.getProposicao());
        atualizarProposicaoComNovasTramitacoes(politicoProposicao, tramiteNovoMaisRecente);
    }

    private void atualizarProposicaoComNovasTramitacoes(PoliticoProposicao politicoProposicao, Tramitacao ultimoTramite) {
        if (politicoProposicao.estaDesatualizado(ultimoTramite)) {
            Proposicao proposicao = firestoreProposicaoService.getById(politicoProposicao);

            proposicao.setFoiAtualizada(true);
            proposicao.setVisualizado(false);
            proposicao.setDescricaoSituacao(ultimoTramite.getDescricaoSituacao());
            proposicao.setDespacho(ultimoTramite.getDespacho());
            proposicao.setDescricaoTramitacao(ultimoTramite.getDescricaoTramitacao());
            proposicao.setSequencia(ultimoTramite.getSequencia());
            proposicao.setDataAtualizacao(ultimoTramite.getDataHora());

            firestoreProposicaoService.salvarProposicao(proposicao);
            politicoProposicoesRepository.updateDataAtualizacao(politicoProposicao, ultimoTramite.getDataHora());
        }
    }

    private List<Tramitacao> getTramitacoesDaAPI(String proposicaoId, String data) {
        return this.restTemplate.getForObject(
                URI_PROPOSICAO + "/" + proposicaoId + "/tramitacoes?dataFim=" + data,
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