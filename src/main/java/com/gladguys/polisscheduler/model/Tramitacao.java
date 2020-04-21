package com.gladguys.polisscheduler.model;

import lombok.Data;

@Data
public class Tramitacao {

    private String dataHora; 
    private int sequencia;
    private String siglaOrgao;
    private String regime;
    private String descricaoTramitacao; 
    private String descricaoSituacao;
    private String despacho;
    private String ambito;
}
