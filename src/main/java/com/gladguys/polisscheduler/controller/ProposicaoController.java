package com.gladguys.polisscheduler.controller;

import com.gladguys.polisscheduler.services.ProposicaoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/proposicoes")
public class ProposicaoController {

	private final ProposicaoService proposicaoService;

	public ProposicaoController(ProposicaoService proposicaoService) {
		this.proposicaoService = proposicaoService;
	}

	@GetMapping(value = "/{data}")
	public ResponseEntity<String> salvaProposicoes(@PathVariable("data") String data) {
		try {
			proposicaoService.salvarProposicoes(data);
			return ResponseEntity.ok("Proposicoes salvas com sucesso!");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@GetMapping(value = "/new-dummy")
	public ResponseEntity<String> criarDummyProposicoes() {
		try {
			proposicaoService.criarDummyProposicao();
			return ResponseEntity.ok("proposicao dummy criada com sucesso!");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@GetMapping(value = "/deleteall")
	public ResponseEntity<String> deleteAll() {
		try {
			proposicaoService.deletarTodasProposicoes();
			return ResponseEntity.ok("proposicoes deletadas com sucesso");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

}
