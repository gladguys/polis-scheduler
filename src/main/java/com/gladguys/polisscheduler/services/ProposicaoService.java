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

    private List<String> politicosIds;
    private HashSet<String> politicosComProposicao;

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
        salvarProposicoesPorData(data);
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


    private void salvarProposicoesPorData(String data) throws InterruptedException, ExecutionException {
        politicosComProposicao = new HashSet<>();
        var urisDeProposicoes = getUrisDeProposicoesDaApi(data);
        politicosIds = firestorePoliticoService
                .getPoliticos()
                .parallelStream()
                .map(p -> p.getId())
                .collect(Collectors.toList());

        List<Proposicao> proposicoes = urisDeProposicoes
                .parallelStream()
                .map(uriProp -> buscarDadosCompletoProposicaoNaApi(uriProp))
                .map(ProposicaoCompleto::build)
                .filter(Proposicao::temTipoDescricaoValido)
                .collect(Collectors.toList());


        proposicoes.forEach(proposicao -> {
            if (politicosIds.contains(getIdPoliticoAutorDaProposicao(proposicao))) {
                Proposicao proposicaoSalva = salvarProposicaoNoFirestore(proposicao);
                if (proposicaoSalva != null) {
                    politicoProposicoesRepository.inserirRelacaoPoliticoProposicao(proposicaoSalva);
                    salvarTramitacoesParaProposicaoNoFirestore(proposicaoSalva, data);
                    politicosComProposicao.add(proposicaoSalva.getIdPoliticoAutor());
                }
            }
        });

        notificacaoFCMService.enviarNotificacaoParaSeguidoresDePoliticos("proposições", politicosComProposicao);

    }

    private Proposicao salvarProposicaoNoFirestore(Proposicao proposicao)  {
        Politico politicoDaProposicao = null;
        try {
            politicoDaProposicao = firestorePoliticoService.getPoliticoById(proposicao.getIdPoliticoAutor());
            proposicao.configuraDadosPoliticoNaProposicao(politicoDaProposicao);
            return firestoreProposicaoService.salvarProposicao(proposicao);

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void salvarTramitacoesParaProposicaoNoFirestore(Proposicao proposicao, String data) {
        var tramitacoes = getTramitacoesDaAPI(proposicao.getId(), data);
        if (tramitacoes.size() > 0) {
            proposicao.atualizaDadosUltimaTramitacao(
                    Collections.max(tramitacoes, Comparator.comparing(Tramitacao::getSequencia)));
            firestoreProposicaoService.salvarTramitacoesProposicao(tramitacoes, proposicao.getId());
        }
    }

    private String getIdPoliticoAutorDaProposicao(Proposicao proposicao) {

        var autoresSimples = this.restTemplate
                .getForObject(proposicao.getIdPoliticoAutor(), RetornoApiAutoresProposicao.class).getDados();

        RetornoApiSimples retPolitico = null;
        if (autoresSimples.size() > 0) {
            retPolitico = autoresSimples.get(0);
        }

        if (retPolitico != null && retPolitico.getUri() != null && retPolitico.getUri() != "") {
            PoliticoCompleto politicoCompleto = this.restTemplate.getForObject(
                    retPolitico.getUri(), RetornoApiPoliticosCompleto.class).dados;
            return politicoCompleto == null ? null : politicoCompleto.getId();
        } else {
            return null;
        }
    }

    private ProposicaoCompleto buscarDadosCompletoProposicaoNaApi(RetornoApiSimples uriProposicao) {
        return Objects.requireNonNull(
                this.restTemplate.getForObject(uriProposicao.getUri(), RetornoApiProposicaoCompleto.class)).dados;
    }

    private List<RetornoApiSimples> getUrisDeProposicoesDaApi(String data) {
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
        return retSimplesProposicoes;
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

    private void atualizarProposicaoComNovasTramitacoes(PoliticoProposicao politicoProposicao, Tramitacao
            ultimoTramite) {
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
}