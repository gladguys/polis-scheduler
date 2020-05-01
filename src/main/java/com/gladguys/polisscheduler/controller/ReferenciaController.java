package com.gladguys.polisscheduler.controller;

import com.gladguys.polisscheduler.services.ReferenciaService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/referencias")
public class ReferenciaController {

    private final ReferenciaService referenciaService;

    public ReferenciaController(ReferenciaService referenciaService) {
        this.referenciaService = referenciaService;
    }

    
    @GetMapping(value = "/orgaos")
    public ResponseEntity<String> atualizarOrgaos() {
       
            referenciaService.salvarOrgaos();
            return ResponseEntity.ok("Orgãos salvos com sucesso");
     
    }

}