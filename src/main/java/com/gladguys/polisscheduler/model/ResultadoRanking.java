package com.gladguys.polisscheduler.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ResultadoRanking {
    private List<String> nomesPoliticoPrimeiro;
    private String nomePoliticoUltimo;
    private List<String> fotosPoliticoPrimeiro;
    private String fotoPoliticoUltimo;
    private List<String> partidosPoliticoPrimeiro;
    private String partidoPoliticoUltimo;
    private List<String> estadosPoliticoPrimeiro;
    private String estadoPoliticoUltimo;
    private List<String> logosPartidoPoliticoPrimeiro;
    private String logoPartidoPoliticoUltimo;
    private List<Double> despesasPoliticoPrimeiro;
    private Double despesaPoliticoUltimo;
    private Double despesaMedia;

    public void setDadosPoliticoPrimeiro(List<Politico> politicos) {
        politicos.forEach(p -> {
            nomesPoliticoPrimeiro.add(p.getNomeEleitoral());
            fotosPoliticoPrimeiro.add(p.getUrlFoto());
            partidosPoliticoPrimeiro.add(p.getSiglaPartido());
            estadosPoliticoPrimeiro.add(p.getSiglaUf());
            logosPartidoPoliticoPrimeiro.add(p.getUrlPartidoLogo());
            despesasPoliticoPrimeiro.add(p.getTotalDespesas());
        });
    }

    public void setDadosPoliticoUltimo(Politico politico) {
        nomePoliticoUltimo = politico.getNomeEleitoral();
        fotoPoliticoUltimo = politico.getUrlFoto();
        partidoPoliticoUltimo = politico.getSiglaPartido();
        estadoPoliticoUltimo = politico.getSiglaUf();
        logoPartidoPoliticoUltimo = politico.getUrlPartidoLogo();
        despesaPoliticoUltimo = politico.getTotalDespesas();
    }

}
