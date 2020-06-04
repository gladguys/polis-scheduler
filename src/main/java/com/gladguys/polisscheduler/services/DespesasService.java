package com.gladguys.polisscheduler.services;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.gladguys.polisscheduler.model.Despesa;
import com.gladguys.polisscheduler.model.Politico;
import com.gladguys.polisscheduler.model.RetornoDespesas;
import com.gladguys.polisscheduler.model.TipoAtividade;
import com.gladguys.polisscheduler.repository.DespesasRepository;
import com.gladguys.polisscheduler.services.firestore.FirestoreDespesaService;
import com.gladguys.polisscheduler.services.firestore.FirestorePoliticoService;
import com.gladguys.polisscheduler.utils.DataUtil;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DespesasService {

    private static final String URI_POLITICOS = "https://dadosabertos.camara.leg.br/api/v2/deputados/";
    private final RestTemplate restTemplate;
    private final FirestoreDespesaService firestoreService;
    private final FirestorePoliticoService firestorePoliticoService;
    private final NotificacaoFCMService notificacaoFCMService;
    private HashSet<String> politicosComDespesaNova;
    private DespesasRepository despesasRepository;

    public DespesasService(RestTemplateBuilder restTemplateBuilder,
                           FirestoreDespesaService firestoreService,
                           FirestorePoliticoService firestorePoliticoService,
                           NotificacaoFCMService notificacaoFCMService,
                           DespesasRepository despesasRepository) {
        this.restTemplate = restTemplateBuilder.build();
        this.firestoreService = firestoreService;
        this.firestorePoliticoService = firestorePoliticoService;
        this.notificacaoFCMService = notificacaoFCMService;
        this.despesasRepository = despesasRepository;
    }

    public void salvarDespesasMesAtualEAnterior(Integer mes, Integer ano) throws InterruptedException, ExecutionException {
        politicosComDespesaNova = new HashSet<>();

        //TODO: buscar politicos da base do scheduler
        List<Politico> politicos = firestorePoliticoService.getPoliticos();

        int numeroMes = mes != null ? mes : DataUtil.getNumeroMes();
        int numeroAno = ano != null ? ano : DataUtil.getNumeroAno();

        try {
            politicos.parallelStream().forEach(p -> {
                String urlParaDespesasPoliticoMesVigente =
                        URI_POLITICOS + p.getId() + "/despesas?ano=" + numeroAno + "&mes=" + numeroMes + "&ordem=ASC&ordenarPor=ano";
                List<Despesa> despesasDoMesVigente = this.restTemplate
                        .getForObject(urlParaDespesasPoliticoMesVigente, RetornoDespesas.class).getDados();

                String urlParaDespesasPoliticoMesPassado = getUrlParaDespesasPoliticoMesAnterior(numeroMes, numeroAno, p);
                List<Despesa> despesasMesPassado = this.restTemplate
                        .getForObject(urlParaDespesasPoliticoMesPassado, RetornoDespesas.class).getDados();

                despesasDoMesVigente.addAll(despesasMesPassado);

                despesasDoMesVigente
                        .stream()
                        .filter(d -> d.getDataDocumento() != null)
                        .forEach(d -> d.montaDespesa(p));

                var despesasNovas = despesasDoMesVigente
                        .stream()
                        .filter(d -> d.getDataDocumento() != null)
                        .filter(this::ehNovaDespesa).collect(Collectors.toList());

                despesasNovas.forEach(d -> {
                    try {
                        firestoreService.salvarDespesa(d);
                        politicosComDespesaNova.add(p.getId());
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();

                    }
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (politicosComDespesaNova.size() > 0) {
            notificacaoFCMService.enviarNotificacaoParaSeguidoresDePoliticos(
                    "despesas de político",
                    politicosComDespesaNova.stream().distinct().collect(Collectors.toSet()));
        }
    }

    public String getUrlParaDespesasPoliticoMesAnterior(int numeroMes, int numeroAno, Politico p) {
        String urlParaDespesasPoliticoMesPassado;
        if (numeroMes == 1) {
            urlParaDespesasPoliticoMesPassado =
                    URI_POLITICOS + p.getId() + "/despesas?ano=" + numeroAno + "&mes=12&ordem=ASC&ordenarPor=ano";
        } else {
            urlParaDespesasPoliticoMesPassado =
                    URI_POLITICOS +
                            p.getId() + "/despesas?ano=" + numeroAno + "&mes=" + (numeroMes - 1) + "&ordem=ASC&ordenarPor=ano";
        }
        return urlParaDespesasPoliticoMesPassado;
    }

    private boolean ehNovaDespesa(Despesa despesa) {
        return !this.despesasRepository.exists(despesa);
    }

    public void totalizadorDespesasPorAnoEmes(String ano, String mes) throws ExecutionException, InterruptedException {

       try {
           //pega todos os deputados da base
           List<Politico> politicos = firestorePoliticoService.getPoliticos();
           politicos.parallelStream().forEach(politico -> {
               String urlParaDespesasPolitico = montaUrlParaDespesasPolitico(ano, mes, politico);

               List<Despesa> despesas = this.restTemplate
                       .getForObject(urlParaDespesasPolitico, RetornoDespesas.class).getDados();

               Optional<BigDecimal> valorMes = despesas.parallelStream()
                       .map(d -> new BigDecimal(d.getValorLiquido()))
                       .reduce((v1, v2) -> v1.add(v2));

               try {
                   firestoreService.salvarTotalDespesaPoliticoPorMes(politico.getId(), ano, mes, valorMes.orElse(new BigDecimal(0.0)));
                   firestorePoliticoService.atualizarTotalizadorDespesaPolitico(politico.getId(), valorMes.orElse(new BigDecimal(0.0)));
               } catch (Exception e) {
                   e.printStackTrace();
               }

           });
           //  notificacaoFCMService.enviarNotificacaoTotalDespesasAtualizado();
       } catch (Exception e) {
           e.printStackTrace();
           throw e;
       }
    }

    private String montaUrlParaDespesasPolitico(String ano, String mes, Politico politico) {
        return URI_POLITICOS + politico.getId() + "/despesas?ano=" + ano + "&mes=" + mes
                + "&pagina=1&itens=100000000&ordem=ASC&ordenarPor=ano";
    }

    public void deletarTodasDespesas() throws ExecutionException, InterruptedException {
        this.firestoreService.deletarTodasDespesas();
        this.firestorePoliticoService.zerarTotalizadorDespesas();
    }

    public void resetaTotalizadorDespesasPoliticos() {
        this.firestorePoliticoService.zerarTotalizadorDespesas();
        //TODO: this.firestorePoliticoService.calculaTotalDespesas();
    }

    public String criarDespesaMock() {
        var despesa = new Despesa();
        despesa.setAno("2020");
        despesa.setCnpjCpfFornecedor("03482208000182");
        despesa.setCodDocumento("6821340");
        despesa.setDataDocumento("2020-05-06");
        despesa.setEstadoPolitico("RN");
        despesa.setFotoPolitico("https://www.camara.leg.br/internet/deputado/bandep/109429.jpg");
        despesa.setIdPolitico("109429");
        despesa.setMes("5");
        despesa.setNomeFornecedor("AUTO POSTO JK LTDA");
        despesa.setNomePolitico("Benes Leocádio");
        despesa.setSiglaPartido("REPUBLICANOS");
        despesa.setTipoAtividade(TipoAtividade.DESPESA);
        despesa.setTipoDespesa("COMBUSTÍVEIS E LUBRIFICANTES.");
        despesa.setTipoDocumento("Nota Fiscal Eletrônica");
        despesa.setUrlDocumento("http://camara.leg.br/cota-parlamentar/nota-fiscal-eletronica?ideDocumentoFiscal=6821340");
        despesa.setValorDocumento("231.42");
        despesa.setValorGlosa("0.0");
        despesa.setValorLiquido("200.42");

        try {
            return firestoreService.salvarDespesa(despesa);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}