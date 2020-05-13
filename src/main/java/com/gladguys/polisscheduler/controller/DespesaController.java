package com.gladguys.polisscheduler.controller;

import com.gladguys.polisscheduler.services.DespesasService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/despesas")
public class DespesaController {

    final DespesasService despesasService;

    public DespesaController(DespesasService despesasService) {
        this.despesasService = despesasService;
    }

    @GetMapping("/{ano}/{mes}")
    public ResponseEntity<String> salvaTotalizadorDespesasPor(@PathVariable("ano") String ano, @PathVariable("mes") String mes) {
        try {
            despesasService.totalizadorDespesasPorAnoEmes(ano, mes);
            return ResponseEntity.ok("Despepsas do mes salvas com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
