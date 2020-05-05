package com.gladguys.polisscheduler.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Data;

@Data
public class ProposicaoCompleto {

    private String id;
    private String descricaoTipo;
    private String numero;
    private String ementa;
    private String dataApresentacao;
    private String keywords;
    private String uriAutores;
    private String ementaDetalhada;

    public Proposicao build() {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        Date dataApresentacaoDate = null;
        Date dataHoraDate = null;
        
        try {
            dataApresentacaoDate = sdf.parse(this.dataApresentacao);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        Proposicao proposicao = new Proposicao();
        proposicao.setId(this.id);
        proposicao.setDescricaoTipo(this.descricaoTipo);
        proposicao.setNumero(this.numero);
        proposicao.setEmenta(this.ementa);
        proposicao.setDataApresentacao(new SimpleDateFormat("yyyy-MM-dd").format(dataApresentacaoDate).toString());
        proposicao.setEmentaDetalhada(this.ementaDetalhada);
        

        return proposicao;
    }
}
