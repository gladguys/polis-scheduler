package com.gladguys.polisscheduler.services;

import java.io.IOException;
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
        proposicao.setDataApresentacao("2020-05-15");
        proposicao.setDataAtualizacao("2020-05-15");
        proposicao.setEstadoPolitico("RN");
        proposicao.setSiglaPartido("REPUBLICANOS");

        firestoreProposicaoService.salvarProposicao(proposicao);
    }

    public void deletarTodasProposicoes() throws ExecutionException, InterruptedException {
        firestoreProposicaoService.deletarTodasProposicoes();
        zerarTotalizadorProjetosLeiPoliticos();
    }

    public void zerarTotalizadorProjetosLeiPoliticos() {
        firestorePoliticoService.limparTotalizadorProposicoesPoliticos();
    }

    private void salvarProposicoesPorData(String data) throws InterruptedException, ExecutionException {
        try {
            politicosComProposicao = new HashSet<>();
            var urisDeProposicoes = getUrisDeProposicoesDaApi(data);

            List<Proposicao> proposicoes = urisDeProposicoes
                    .parallelStream()
                    .map(uriProp -> buscarDadosCompletoProposicaoNaApi(uriProp))
                    .map(ProposicaoCompleto::build)
                    .filter(Proposicao::temTipoDescricaoValido)
                    .collect(Collectors.toList());


            proposicoes.forEach(proposicao -> {
                var tramitacoes = getTramitacoesDaAPI(proposicao.getId(), data);
                var politicosAutores = getPoliticosAutorDaProposicao(proposicao);
                proposicao.setAutores(politicosAutores);
                politicosAutores.forEach(politico -> {

                    proposicao.setIdPoliticoAutor(politico.getId());
                    Proposicao proposicaoSalva = salvarProposicaoNoFirestore(proposicao, tramitacoes);

                    if (proposicaoSalva != null) {
                        try {
                            politicoProposicoesRepository.inserirRelacaoPoliticoProposicao(proposicaoSalva);
                            salvarTramitacoesParaProposicaoNoFirestore(tramitacoes, proposicao.getId());
                            politicosComProposicao.add(proposicaoSalva.getIdPoliticoAutor());
                        } catch (Exception e) {
                            e.getStackTrace();
                            System.err.println(e);
                        }
                    }
                });
            });

            if (politicosComProposicao.size() > 0) {
                notificacaoFCMService.enviarNotificacaoParaSeguidoresDePoliticos("propostas apresentadas por político", politicosComProposicao);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Proposicao salvarProposicaoNoFirestore(Proposicao proposicao, List<Tramitacao> tramitacoes) {
        Politico politicoDaProposicao = null;
        try {
            //TODO: buscar politicos da base do scheduler
            politicoDaProposicao = firestorePoliticoService.getPoliticoById(proposicao.getIdPoliticoAutor());
            Tramitacao tramitacaoMaisRecente = Collections.max(tramitacoes, Comparator.comparing(Tramitacao::getSequencia));
            proposicao.montaObjetoProposicao(politicoDaProposicao, tramitacaoMaisRecente);
            return firestoreProposicaoService.salvarProposicao(proposicao);

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println(e);
        }
        return null;
    }

    private void salvarTramitacoesParaProposicaoNoFirestore(List<Tramitacao> tramitacoes, String proposicaoId) {
        if (tramitacoes.size() > 0) {
            firestoreProposicaoService.salvarTramitacoesProposicao(tramitacoes, proposicaoId);
        }
    }

    private List<PoliticoCompleto> getPoliticosAutorDaProposicao(Proposicao proposicao) {
        var politicosAutores = new ArrayList<PoliticoCompleto>();
        var autoresSimples = this.restTemplate
                .getForObject(proposicao.getUriAutores(), RetornoApiAutoresProposicao.class).getDados();

        if (autoresSimples.size() > 0) {
            autoresSimples.forEach(retPolitico -> {
                if (retPolitico != null && retPolitico.getUri() != null && retPolitico.getUri() != "") {
                    PoliticoCompleto politicoCompleto = this.restTemplate.getForObject(
                            retPolitico.getUri(), RetornoApiPoliticosCompleto.class).dados;
                    if (politicoCompleto != null && politicoCompleto.getId() != null) {
                        politicosAutores.add(politicoCompleto);
                    }
                }
            });
        }
        return politicosAutores;
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

    public void atualizaTramitacoes(String dataInformada) throws IOException {
        String dataBusca;
        if (dataInformada == null) {
            dataBusca = DataUtil.getDataOntem();
        } else {
            dataBusca = dataInformada;
        }
        List<PoliticoProposicao> politicoProposicoes = politicoProposicoesRepository.getTodos();
        int porcentagemAtual = 0;
        int porcentagemAntiga = -1;

        for (int i = 0; i < politicoProposicoes.size(); i++) {
            if (porcentagemAntiga != porcentagemAtual) {
                Runtime.getRuntime().exec("clear");
                System.out.print("Proposições Processadas: " + porcentagemAtual + "% [");
                for (int j = 0; j < 100; j++) {
                    if (j >= porcentagemAtual) System.out.print(" ");
                    else System.out.print("|");
                }
                System.out.println("] " + (i+1) + "/" + politicoProposicoes.size());
                porcentagemAntiga = porcentagemAtual;
            }

            atualizaTramitacao(politicoProposicoes.get(i), dataBusca);
            porcentagemAtual = ((i + 1) * 100) / politicoProposicoes.size();
        }
    }

    private void atualizaTramitacao(PoliticoProposicao politicoProposicao, String data) {
        try {
            List<Tramitacao> tramitacoesNovas = getTramitacoesDaAPI(politicoProposicao.getProposicao(), data);
            if (tramitacoesNovas != null) {
                var tramiteNovoMaisRecente = Collections.max(tramitacoesNovas, Comparator.comparing(Tramitacao::getSequencia));

                if (politicoProposicao.estaDesatualizado(tramiteNovoMaisRecente)) {
                    firestoreProposicaoService.salvarTramitacoesProposicao(tramitacoesNovas, politicoProposicao.getProposicao());
                    atualizarProposicaoComNovasTramitacoes(politicoProposicao, tramiteNovoMaisRecente);
                }
            }
        } catch (Exception e) {
            System.err.println("ERROR quando tentando atualizar tramitação para proposição " + politicoProposicao.getProposicao());
        }
    }

    private void atualizarProposicaoComNovasTramitacoes(PoliticoProposicao politicoProposicao, Tramitacao ultimoTramite) {

        Proposicao proposicao = firestoreProposicaoService.getById(politicoProposicao);

        proposicao.setFoiAtualizada(true);
        proposicao.setVisualizado(false);
        proposicao.setDescricaoSituacao(ultimoTramite.getDescricaoSituacao());
        proposicao.setDespacho(ultimoTramite.getDespacho());
        proposicao.setDescricaoTramitacao(ultimoTramite.getDescricaoTramitacao());
        proposicao.setSequencia(ultimoTramite.getSequencia());
        proposicao.setDataAtualizacao(ultimoTramite.getDataHora());
        proposicao.setDataPublicacao(ultimoTramite.getDataHora());

        firestoreProposicaoService.salvarProposicao(proposicao);
        politicoProposicoesRepository.updateDataAtualizacao(politicoProposicao, ultimoTramite.getDataHora());
    }


    private List<Tramitacao> getTramitacoesDaAPI(String proposicaoId, String data) {
        try {
            RetornoApiTramitacoes retorno = this.restTemplate.getForObject(
                    URI_PROPOSICAO + "/" + proposicaoId + "/tramitacoes?dataFim=" + data,
                    RetornoApiTramitacoes.class);

            if (retorno != null)
                return retorno.dados;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}