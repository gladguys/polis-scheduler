package com.gladguys.polisscheduler.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ComparativoRankingDespesas {
    private List<String> nomesPoliticoPrimeiro = new ArrayList<>();
    private String nomePoliticoUltimo;
    private List<String> fotosPoliticoPrimeiro = new ArrayList<>();
    private String fotoPoliticoUltimo;
    private List<String> partidosPoliticoPrimeiro = new ArrayList<>();
    private String partidoPoliticoUltimo;
    private List<String> estadosPoliticoPrimeiro = new ArrayList<>();
    private String estadoPoliticoUltimo;
    private List<String> logosPartidoPoliticoPrimeiro = new ArrayList<>();
    private String logoPartidoPoliticoUltimo;
    private List<Double> despesasPoliticoPrimeiro = new ArrayList<>();
    private Double despesaPoliticoUltimo;
    private Double despesaMedia;
    private String idPoliticoUltimo;
    private List<String> idPoliticosPrimeiro = new ArrayList<>();

    public void setDadosPoliticoPrimeiro(List<Politico> politicos) {
        politicos.forEach(p -> {
            idPoliticosPrimeiro.add(p.getId());
            nomesPoliticoPrimeiro.add(p.getNomeEleitoral());
            fotosPoliticoPrimeiro.add(p.getUrlFoto());
            partidosPoliticoPrimeiro.add(p.getSiglaPartido());
            estadosPoliticoPrimeiro.add(p.getSiglaUf());
            logosPartidoPoliticoPrimeiro.add(p.getUrlPartidoLogo());
            despesasPoliticoPrimeiro.add(p.getTotalDespesas());
        });
    }

    public void setDadosPoliticoUltimo(Politico politico) {
        idPoliticoUltimo = politico.getId();
        nomePoliticoUltimo = politico.getNomeEleitoral();
        fotoPoliticoUltimo = politico.getUrlFoto();
        partidoPoliticoUltimo = politico.getSiglaPartido();
        estadoPoliticoUltimo = politico.getSiglaUf();
        logoPartidoPoliticoUltimo = politico.getUrlPartidoLogo();
        despesaPoliticoUltimo = politico.getTotalDespesas();
    }
}
