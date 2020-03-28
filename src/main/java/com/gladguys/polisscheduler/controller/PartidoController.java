package com.gladguys.polisscheduler.controller;

import com.gladguys.polisscheduler.services.PartidoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/partidos")
public class PartidoController {

	private final PartidoService partidoService;

	public PartidoController(PartidoService partidoService) {
		this.partidoService = partidoService;
	}

	@GetMapping
	public ResponseEntity<String> atualizaPartidos() {
		try {
			partidoService.atualizaPartidos();
			return ResponseEntity.ok("Partidos salvos com sucesso!");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}
}
