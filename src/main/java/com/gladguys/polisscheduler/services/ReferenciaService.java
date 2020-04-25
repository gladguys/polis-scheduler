package com.gladguys.polisscheduler.services;

import com.gladguys.polisscheduler.model.RetornoOrgaos;
import com.gladguys.polisscheduler.services.firestore.FirestoreReferenciaService;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ReferenciaService {

    private static final String URI_ORGAOS = "https://dadosabertos.camara.leg.br/api/v2/orgaos?ordem=ASC&ordenarPor=id";
    private final FirestoreReferenciaService firestoreReferenciaService;
    private final RestTemplate restTemplate;

    public ReferenciaService(FirestoreReferenciaService firestoreReferenciaService, RestTemplateBuilder restTemplateBuilder) {
        this.firestoreReferenciaService = firestoreReferenciaService;
        this.restTemplate = restTemplateBuilder.build();
    }

    public void salvarOrgaos() {
        RetornoOrgaos retornoOrgaos = restTemplate.getForObject(URI_ORGAOS, RetornoOrgaos.class);
        if (retornoOrgaos != null && retornoOrgaos.temOrgaos()) {
            firestoreReferenciaService.salvarOrgaos(retornoOrgaos.getDados());
        }
    }
}