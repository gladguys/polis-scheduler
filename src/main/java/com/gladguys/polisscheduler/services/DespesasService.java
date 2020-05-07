package com.gladguys.polisscheduler.services;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.gladguys.polisscheduler.model.Despesa;
import com.gladguys.polisscheduler.model.Politico;
import com.gladguys.polisscheduler.model.RetornoDespesas;
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
            String urlParaDespesasPolitico = URI_POLITICOS + p.getId() + "/despesas?ano=2019&mes=" + 5
                    + "&ordem=ASC&ordenarPor=ano";

           /* String urlParaDespesasPoliticoMesPassado;
            if (numeroMes == 1) {
                //TODO: retirar 2019 chapado
                urlParaDespesasPoliticoMesPassado = URI_POLITICOS + p.getId()
                        + "/despesas?ano=2019&mes=12&ordem=ASC&ordenarPor=ano";
            } else {
                //TODO: retirar 2020 chapado
                urlParaDespesasPoliticoMesPassado = URI_POLITICOS + p.getId() + "/despesas?ano=2020&mes="
                        + (numeroMes - 1) + "&ordem=ASC&ordenarPor=ano";
            }*/

            List<Despesa> despesasDeHoje = this.restTemplate
                    .getForObject(urlParaDespesasPolitico, RetornoDespesas.class).getDados();

           /* List<Despesa> despesasMesPassado = this.restTemplate
                    .getForObject(urlParaDespesasPoliticoMesPassado, RetornoDespesas.class).getDados();*/

            //despesasDeHoje.addAll(despesasMesPassado);

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
                    despesasDeHoje.stream().filter(d -> d.getDataDocumento() != null && (d.getDataDocumento().equals("2019-05-20") || d.getDataDocumento().equals("2019-05-21") || d.getDataDocumento().equals("2019-05-22") )).collect(Collectors.toList()),
                    p.getId());
        });
    }

}