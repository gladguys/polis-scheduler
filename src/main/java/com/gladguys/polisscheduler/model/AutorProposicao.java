package com.gladguys.polisscheduler.model;

import lombok.Data;

@Data
public class AutorProposicao extends RetornoApiSimples {

    private String nome;
    private int ordemAssinatura;
}
