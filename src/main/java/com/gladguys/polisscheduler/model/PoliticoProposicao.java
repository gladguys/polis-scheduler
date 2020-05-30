package com.gladguys.polisscheduler.model;

import lombok.Data;

@Data
public class PoliticoProposicao {

    private String id;
    private String politico;
    private String proposicao;
    private String atualizacao;

    public boolean estaDesatualizado(Tramitacao ultimoTramite) {
        return this.atualizacao.compareTo(ultimoTramite.getDataHora()) < 0;
    }
}
