package com.gladguys.polisscheduler.model;

import lombok.Data;

@Data
public class Proposicao {

    private String id;
    private String descricaoTipo;
    private String numero;
    private String ementa;
    private String dataApresentacao;
    private String dataDocumento;
    private String sequencia;
    private String descricaoTramitacao;
    private String descricaoSituacao;
    private String despacho;
    private String nomePolitico;
    private String idPoliticoAutor;
    private String ementaDetalhada;
    private String keywords;
    private String tipoAtividade = "PROPOSICAO";
    private String siglaPartido;
    private String fotoPolitico;
    private String estadoPolitico;

}
