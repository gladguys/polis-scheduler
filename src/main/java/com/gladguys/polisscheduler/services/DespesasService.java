package com.gladguys.polisscheduler.services;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.gladguys.polisscheduler.model.Despesa;
import com.gladguys.polisscheduler.model.RetornoDespesas;
import com.gladguys.polisscheduler.utils.DataUtil;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DespesasService {

    private static final String URI_POLITICOS = "https://dadosabertos.camara.leg.br/api/v2/deputados/";

    private final RestTemplate restTemplate;
    private final FirestoreService firestoreService;

    public DespesasService(RestTemplateBuilder restTemplateBuilder, FirestoreService firestoreService) {
        this.restTemplate = restTemplateBuilder.build();
        this.firestoreService = firestoreService;
    }

    public void salvarDespesasDoDia() throws InterruptedException, ExecutionException {
        List<String> ids = firestoreService.getPoliticosIds();
        ids.forEach(id -> {
            int numeroMes = DataUtil.getNumeroMes();
            String urlParaDespesasPolitico = URI_POLITICOS + id + "/despesas?ano=2020&mes=" + numeroMes +"&ordem=ASC&ordenarPor=ano";
            List<Despesa> despesasDoPolitico = this.restTemplate.getForObject(urlParaDespesasPolitico, RetornoDespesas.class).getDados();
            firestoreService.salvarDespesas(despesasDoPolitico, id);
        });
    }

}