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
    private int sequencia;
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
    private boolean foiAtualizada;
    private String dataAtualizacao;
    private String urlPartidoLogo;

    public void configuraDadosPoliticoNaProposicao(PoliticoCompleto politicoRetorno) {
        this.setNomePolitico(politicoRetorno.getUltimoStatus().getNomeEleitoral());
        this.setIdPoliticoAutor(politicoRetorno.getId());
        this.setSiglaPartido(politicoRetorno.getUltimoStatus().getSiglaPartido());
        this.setFotoPolitico(politicoRetorno.getUltimoStatus().getUrlFoto());
        this.setEstadoPolitico(politicoRetorno.getUltimoStatus().getSiglaUf());
    }
}
