package com.gladguys.polisscheduler.model;

import com.gladguys.polisscheduler.services.firestore.FirestorePoliticoService;
import lombok.Data;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Data
public class Proposicao {

    private String id;
    private String descricaoTipo;
    private String numero;
    private String ementa;
    private String dataApresentacao;
    private String dataDocumento;
    private String dataPublicacao;
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
    private boolean visualizado;
    private String uriAutores;
    private String urlInteiroTeor;
    private List<String> nomesAutores;

    public void configuraDadosPoliticoNaProposicao(Politico politico) {
        nomePolitico = politico.getNomeEleitoral();
        idPoliticoAutor = politico.getId();
        siglaPartido = politico.getSiglaPartido();
        fotoPolitico = politico.getUrlFoto();
        estadoPolitico = politico.getSiglaUf();
        urlPartidoLogo = politico.getUrlPartidoLogo();
    }

    public void atualizaDadosUltimaTramitacao(Tramitacao tramitacao) {
        this.descricaoTramitacao = tramitacao.getDescricaoTramitacao();
        this.despacho = tramitacao.getDespacho();
    }

    public boolean temTipoDescricaoValido() {
        if (descricaoTipo.equals("Projeto de Lei") ||
                descricaoTipo.equals("Indicação") ||
                descricaoTipo.startsWith("Requerimento") ||
                descricaoTipo.equals("Emenda de Plenário")) {
            return true;
        }
        return false;
    }

    public void montaObjetoProposicao(Politico politicoDaProposicao, Tramitacao tramitacaoMaisRecente) {
        this.configuraDadosPoliticoNaProposicao(politicoDaProposicao);
        this.atualizaDadosUltimaTramitacao(tramitacaoMaisRecente);
    }

    public void setAutores(List<PoliticoCompleto> politicosAutores) {
        this.nomesAutores = politicosAutores.stream().map(p -> p.getUltimoStatus().getNomeEleitoral()).collect(Collectors.toList());
    }
}
