package com.gladguys.polisscheduler.services;

import com.gladguys.polisscheduler.model.Politico;
import com.gladguys.polisscheduler.model.RetornoApi;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class PoliticosService {

	private final RestTemplate restTemplate;

	public PoliticosService(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	public void getPoliticos() {
		String url = "https://dadosabertos.camara.leg.br/api/v2/deputados?ordem=ASC&ordenarPor=nome";
		RetornoApi retorno = this.restTemplate.getForObject(url, RetornoApi.class);

	}
}
