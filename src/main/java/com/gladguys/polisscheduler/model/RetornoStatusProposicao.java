package com.gladguys.polisscheduler.model;

import lombok.Data;

@Data
public class RetornoStatusProposicao {

    private String dataHora;
    private int sequencia;
    private String descricaoTramitacao;
    private String descricaoSituacao;
    private String despacho;

}
