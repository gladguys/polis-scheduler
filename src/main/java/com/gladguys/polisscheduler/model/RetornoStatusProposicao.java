package com.gladguys.polisscheduler.model;

import lombok.Data;

@Data
public class RetornoStatusProposicao {

    private String dataHora;
    private String sequencia;
    private String descricaoTramitacao;
    private String descricaoSituacao;
    private String despacho;

}
