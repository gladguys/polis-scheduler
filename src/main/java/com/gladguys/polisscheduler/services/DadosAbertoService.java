package com.gladguys.polisscheduler.services;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

public class DadosAbertoService {

	private final RestTemplate restTemplate;

	public DadosAbertoService(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	public String getPostsPlainJSON() {
		String url = "https://dadosabertos.camara.leg.br/api/v2/deputados?ordem=ASC&ordenarPor=nome";
		return this.restTemplate.getForObject(url, String.class);
	}
}
