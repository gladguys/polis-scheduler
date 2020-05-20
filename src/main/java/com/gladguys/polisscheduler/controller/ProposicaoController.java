package com.gladguys.polisscheduler.controller;

import com.gladguys.polisscheduler.services.ProposicaoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.QueryParam;

@RestController
@RequestMapping(value = "/proposicoes")
public class ProposicaoController {

	private final ProposicaoService proposicaoService;

	public ProposicaoController(ProposicaoService proposicaoService) {
		this.proposicaoService = proposicaoService;
	}

	@GetMapping
	public ResponseEntity<String> salvaProposicoes(@RequestParam(value = "data", required = false) String data) {
		try {
			proposicaoService.salvarProposicoes(data);
			return ResponseEntity.ok("Proposicoes salvas com sucesso!");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@GetMapping(value = "/tramites")
	public ResponseEntity<String> atualizaTramites(@RequestParam(value = "data", required = false) String data) {
		try {
			proposicaoService.atualizaTramitacoes(data);
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

	@GetMapping(value = "/delete-all")
	public ResponseEntity<String> deleteAll() {
		try {
			proposicaoService.deletarTodasProposicoes();
			return ResponseEntity.ok("proposicoes deletadas com sucesso");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@GetMapping(value = "/zerar-total-pl")
	public ResponseEntity<String> zerarTotalizadorPl() {
		try {
			proposicaoService.zerarTotalizadorProjetosLeiPoliticos();
			return ResponseEntity.ok("totalizador de projetos de lei zerados com sucesso");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

}
