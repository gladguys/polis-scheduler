package com.gladguys.polisscheduler.controller;

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

	public PoliticosController(PoliticosService politicosService) {
		this.politicosService = politicosService;
	}

	@GetMapping
	public ResponseEntity<String> getPoliticos() {
		try {
			politicosService.getPoliticos();
			return ResponseEntity.ok("Politicos salvos com sucesso!");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}
}
