package com.gladguys.polisscheduler.services;

import java.util.List;

import com.gladguys.polisscheduler.model.Partido;
import com.gladguys.polisscheduler.model.RetornoApiPartidos;
import com.gladguys.polisscheduler.services.firestore.FirestorePartidoService;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PartidoService {

    private static final String URI_PARTIDOS = "https://dadosabertos.camara.leg.br/api/v2/partidos?itens=100";

    private final RestTemplate restTemplate;
    private final FirestorePartidoService firestorePartidoService;
    private final FirestoreService firestoreService;

    public PartidoService(RestTemplateBuilder restTemplateBuilder, FirestorePartidoService firestorePartidoService,
            FirestoreService firestoreService) {
        this.restTemplate = restTemplateBuilder.build();
        this.firestoreService = firestoreService;
        this.firestorePartidoService = firestorePartidoService;
    }

    public void atualizaPartidos() {
        List<Partido> partidos = this.restTemplate.getForObject(URI_PARTIDOS, RetornoApiPartidos.class).dados;
        this.firestorePartidoService.salvarPartidos(partidos);
        this.firestoreService.updateHashCodeSyncPartidos();
    }

}