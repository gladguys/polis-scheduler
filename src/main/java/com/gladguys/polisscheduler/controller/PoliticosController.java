package com.gladguys.polisscheduler.controller;

import com.gladguys.polisscheduler.services.DespesasService;
import com.gladguys.polisscheduler.services.PoliticosService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/politicos")
public class PoliticosController {

	private final PoliticosService politicosService;
	private final DespesasService despesasService;

	public PoliticosController(PoliticosService politicosService, DespesasService despesasService) {
		this.politicosService = politicosService;
		this.despesasService = despesasService;
	}

	@GetMapping
	public ResponseEntity<String> getPoliticos() {
		try {
			politicosService.salvaPoliticos();
			return ResponseEntity.ok("Politicos salvos com sucesso!");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@GetMapping(value = "/despesas")
	public ResponseEntity<String> getDespesas() {
		try {
			despesasService.salvarDespesasDoDia();
			return ResponseEntity.ok("Despesas salvas com sucesso!");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}
}
