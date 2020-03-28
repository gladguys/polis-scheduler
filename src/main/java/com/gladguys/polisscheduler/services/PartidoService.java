package com.gladguys.polisscheduler.services;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.gladguys.polisscheduler.model.Despesa;
import com.gladguys.polisscheduler.model.Partido;
import com.gladguys.polisscheduler.model.Politico;
import com.gladguys.polisscheduler.model.RetornoApiPartido;
import com.gladguys.polisscheduler.model.RetornoApiPartidos;
import com.gladguys.polisscheduler.model.RetornoDespesas;
import com.gladguys.polisscheduler.utils.DataUtil;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PartidoService {

    private static final String URI_PARTIDOS = "https://dadosabertos.camara.leg.br/api/v2/partidos";

    private final RestTemplate restTemplate;
    private final FirestoreService firestoreService;

    public PartidoService(RestTemplateBuilder restTemplateBuilder, FirestoreService firestoreService) {
        this.restTemplate = restTemplateBuilder.build();
        this.firestoreService = firestoreService;
    }

   public void atualizaPartidos() {
       List<Partido> partidos =  this.restTemplate.getForObject(URI_PARTIDOS, RetornoApiPartidos.class).dados;
       this.firestoreService.salvarPartidos(partidos);
   }

}