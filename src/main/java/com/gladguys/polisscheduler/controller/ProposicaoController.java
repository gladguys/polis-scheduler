package com.gladguys.polisscheduler.controller;

import com.gladguys.polisscheduler.services.ProposicaoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/proposicoes")
public class ProposicaoController {

	private final ProposicaoService proposicaoService;

	public ProposicaoController(ProposicaoService proposicaoService) {
		this.proposicaoService = proposicaoService;
	}

	@GetMapping
	public ResponseEntity<String> salvaProposicoes() {
		try {
			System.out.println("deded");
			proposicaoService.salvarProposicoes();
			return ResponseEntity.ok("Partidos salvos com sucesso!");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}
}
