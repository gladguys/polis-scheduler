package com.gladguys.polisscheduler.services;

import com.gladguys.polisscheduler.builder.PoliticoBuilder;
import com.gladguys.polisscheduler.model.Politico;
import com.gladguys.polisscheduler.model.PoliticoCompleto;
import com.gladguys.polisscheduler.model.PoliticoSimples;
import com.gladguys.polisscheduler.model.RetornoApiPoliticosCompleto;
import com.gladguys.polisscheduler.model.RetornoApiPoliticosSimples;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PoliticosService {

	private final RestTemplate restTemplate;

	public PoliticosService(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	public void getPoliticos() {
		String url = "https://dadosabertos.camara.leg.br/api/v2/deputados?ordem=ASC&ordenarPor=nome";
		List<PoliticoSimples> politicos =
				this.restTemplate.getForObject(url, RetornoApiPoliticosSimples.class).dados;

		politicos.forEach(ps -> {
			PoliticoCompleto pCompleto =
							this.restTemplate.getForObject(ps.getUri(), RetornoApiPoliticosCompleto.class).dados;
			Politico politico = PoliticoBuilder.build(pCompleto);

		});
	}
}
