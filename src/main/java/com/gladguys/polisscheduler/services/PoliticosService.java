package com.gladguys.polisscheduler.services;

import com.gladguys.polisscheduler.builder.PoliticoBuilder;
import com.gladguys.polisscheduler.model.*;
import com.gladguys.polisscheduler.services.firestore.FirestorePartidoService;
import com.gladguys.polisscheduler.services.firestore.FirestorePoliticoService;

import com.gladguys.polisscheduler.services.firestore.FirestoreProposicaoService;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class PoliticosService {

	private static final String URI_POLITICOS = "https://dadosabertos.camara.leg.br/api/v2/deputados";

	private final RestTemplate restTemplate;
	private final FirestoreService firestoreService;
	private final FirestorePoliticoService firestorePoliticoService;
	private final FirestorePartidoService firestorePartidoService;
	private final FirestoreProposicaoService firestoreProposicaoService;


	public PoliticosService(RestTemplateBuilder restTemplateBuilder, FirestoreService firestoreService,
							FirestorePoliticoService firestorePoliticoService, FirestorePartidoService firestorePartidoService, FirestoreProposicaoService firestoreProposicaoService) {
		this.restTemplate = restTemplateBuilder.build();
		this.firestoreService = firestoreService;
		this.firestorePoliticoService = firestorePoliticoService;
		this.firestorePartidoService = firestorePartidoService;
		this.firestoreProposicaoService = firestoreProposicaoService;
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

		var partidoOpt = firestorePartidoService.getById(politico.getSiglaPartido());
		politico.setUrlPartidoLogo(partidoOpt.orElse(null).getLogo());

		firestorePoliticoService.addPolitico(politico);
	}

    public void atualizarRankingDespesas() throws ExecutionException, InterruptedException {
		List<Politico> politicos = this.firestorePoliticoService.getPoliticos();
		politicos.sort(Comparator.comparing(p -> p.getTotalDespesas()));

		for (int pos = politicos.size(); pos >= 1; pos--) {
			firestorePoliticoService.atualizarPosicaoRankingDespesaPolitico(politicos.get(pos-1).getId(), pos);
		}
	}

	public void updateTotalizadorPLsPoliticos() throws ExecutionException, InterruptedException {
		var politicos = firestorePoliticoService.getPoliticos();
		politicos.forEach(p -> {
			try {
				firestoreProposicaoService.updateTotalizadorPLsPolitico(p.getId());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
