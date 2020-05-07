package com.gladguys.polisscheduler.services;

import com.gladguys.polisscheduler.builder.PoliticoBuilder;
import com.gladguys.polisscheduler.model.Politico;
import com.gladguys.polisscheduler.model.PoliticoCompleto;
import com.gladguys.polisscheduler.model.PoliticoSimples;
import com.gladguys.polisscheduler.model.RetornoApiPoliticosCompleto;
import com.gladguys.polisscheduler.model.RetornoApiPoliticosSimples;
import com.gladguys.polisscheduler.services.firestore.FirestorePoliticoService;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class PoliticosService {

	private static final String URI_POLITICOS = "https://dadosabertos.camara.leg.br/api/v2/deputados";

	private final RestTemplate restTemplate;
	private final FirestoreService firestoreService;
	private final FirestorePoliticoService firestorePoliticoService;

	public PoliticosService(RestTemplateBuilder restTemplateBuilder, FirestoreService firestoreService,
			FirestorePoliticoService firestorePoliticoService) {
		this.restTemplate = restTemplateBuilder.build();
		this.firestoreService = firestoreService;
		this.firestorePoliticoService = firestorePoliticoService;
	}

	public void salvaPoliticos() {
		String url = URI_POLITICOS + "?ordem=ASC&ordenarPor=nome";
		List<PoliticoSimples> politicos = this.restTemplate.getForObject(url, RetornoApiPoliticosSimples.class).dados;
		politicos.forEach(this::salvaPolitico);
		this.firestoreService.updateHashCodeSyncPoliticos();
	}

	private void salvaPolitico(PoliticoSimples ps) {
		PoliticoCompleto pCompleto = this.restTemplate.getForObject(ps.getUri(),
				RetornoApiPoliticosCompleto.class).dados;
		Politico politico = PoliticoBuilder.build(pCompleto);
		firestorePoliticoService.addPolitico(politico);
	}
}
