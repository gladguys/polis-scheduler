package com.gladguys.polisscheduler.services;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.gladguys.polisscheduler.model.Despesa;
import com.gladguys.polisscheduler.model.Politico;
import com.gladguys.polisscheduler.model.RetornoDespesas;
import com.gladguys.polisscheduler.model.TipoAtividade;
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

    public DespesasService(RestTemplateBuilder restTemplateBuilder, FirestoreDespesaService firestoreService,
            FirestorePoliticoService firestorePoliticoService) {
        this.restTemplate = restTemplateBuilder.build();
        this.firestoreService = firestoreService;
        this.firestorePoliticoService = firestorePoliticoService;
    }

    // @Scheduled(cron = "0 48 05 * * ?")
    public void salvarDespesasDoDia() throws InterruptedException, ExecutionException {
        List<Politico> politicos = firestorePoliticoService.getPoliticos();
        politicos.forEach(p -> {
            int numeroMes = DataUtil.getNumeroMes();
            //TODO: retirar 2020 chapado
            String urlParaDespesasPolitico = URI_POLITICOS + p.getId() + "/despesas?ano=2020&mes=" + numeroMes
                    + "&ordem=ASC&ordenarPor=ano";

            String urlParaDespesasPoliticoMesPassado;
            if (numeroMes == 1) {
                //TODO: retirar 2019 chapado
                urlParaDespesasPoliticoMesPassado = URI_POLITICOS + p.getId()
                        + "/despesas?ano=2019&mes=12&ordem=ASC&ordenarPor=ano";
            } else {
                //TODO: retirar 2020 chapado
                urlParaDespesasPoliticoMesPassado = URI_POLITICOS + p.getId() + "/despesas?ano=2020&mes="
                        + (numeroMes - 1) + "&ordem=ASC&ordenarPor=ano";
            }

            List<Despesa> despesasDeHoje = this.restTemplate
                    .getForObject(urlParaDespesasPolitico, RetornoDespesas.class).getDados();

            List<Despesa> despesasMesPassado = this.restTemplate
                    .getForObject(urlParaDespesasPoliticoMesPassado, RetornoDespesas.class).getDados();

            despesasDeHoje.addAll(despesasMesPassado);

            despesasDeHoje.forEach(d -> {
                d.setIdPolitico(p.getId());
                d.setNomePolitico(p.getNomeEleitoral());
                d.setSiglaPartido(p.getSiglaPartido());
                d.setFotoPolitico(p.getUrlFoto());
                d.setEstadoPolitico(p.getSiglaUf());
                d.setUrlPartidoLogo(p.getUrlPartidoLogo());

                d.buildData();
            });

            firestoreService.salvarDespesas(
                    despesasDeHoje.stream().filter(d -> d.getDataDocumento() != null).collect(Collectors.toList()),
                    p.getId());
        });
    }

    public String criarDespesaMock() {

        Despesa despesa = new Despesa();
        despesa.setAno("2019");
        despesa.setCnpjCpfFornecedor("03482208000182");
        despesa.setCodDocumento("6821340");
        despesa.setDataDocumento("2019-05-06");
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

        return firestoreService.salvarDespesa(despesa);
    }
}