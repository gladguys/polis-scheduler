package com.gladguys.polisscheduler.controller;

import com.gladguys.polisscheduler.services.DespesasService;
import com.gladguys.polisscheduler.services.PoliticosService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

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
	public ResponseEntity<String> getDespesas(@RequestParam(value = "mes", required = false) Integer mes,
											  @RequestParam(value = "ano", required = false) Integer ano) {
		try {
			despesasService.salvarDespesasMesAtualEAnterior(mes, ano);
			return ResponseEntity.ok("Despesas salvas com sucesso!");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@GetMapping(value = "/despesa-dummy")
	public ResponseEntity<String> criarDespesaMock() {
		try {
			String idDespesaMock = despesasService.criarDespesaMock();
			return ResponseEntity.ok("Despesa Dummy com id "+ idDespesaMock +" criada com sucesso!");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@GetMapping(value = "/despesas/delete-all")
	public ResponseEntity<String> deletarDespesas() {
		try {
			this.despesasService.deletarTodasDespesas();
			return ResponseEntity.ok("todas desepsas deletadas com sucesso");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@GetMapping(value = "/ranking-despesas")
	public ResponseEntity<String> atualizaRankingDespesa() throws ExecutionException, InterruptedException {
		try {
			this.politicosService.atualizarRankingDespesas();
			return ResponseEntity.ok("ranking atualizado");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@GetMapping(value = "/total-pls")
	public ResponseEntity<String> atualizaTotalPLsPoliticos() throws ExecutionException, InterruptedException {
		try {
			politicosService.updateTotalizadorPLsPoliticos();
			return ResponseEntity.ok("ranking atualizado");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

}
