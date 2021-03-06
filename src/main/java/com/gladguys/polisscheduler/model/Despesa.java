package com.gladguys.polisscheduler.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Data;

@Data
public class Despesa {

    private String id;
    private String ano;
    private TipoAtividade tipoAtividade = TipoAtividade.DESPESA;
    private String fotoPolitico;
    private String nomePolitico;
    private String idPolitico;
    private String siglaPartido;
    private String cnpjCpfFornecedor;
    private String codDocumento;
    private String codLote;
    private String codTipoDocumento;
    private String dataDocumento;
    private String dataAtualizacao;
    private String mes;
    private String nomeFornecedor;
    private String numDocumento;
    private String numRessarcimento;
    private String parcela;
    private String tipoDespesa;
    private String tipoDocumento;
    private String urlDocumento;
    private String valorDocumento;
    private String valorGlosa;
    private String valorLiquido;
    private String estadoPolitico;
    private String urlPartidoLogo;
    private boolean visualizado;
    private String dataPublicacao;

    public void
    buildData() {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dataDocumentoString = null;

        if (this.dataDocumento != null) {
            try {
                dataDocumentoString = sdf.parse(this.dataDocumento);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            this.dataAtualizacao = new SimpleDateFormat("yyyy-MM-dd").format(dataDocumentoString).toString();
        }
        this.dataPublicacao = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    public void montaDespesa(Politico p) {
        idPolitico = p.getId();
        nomePolitico = p.getNomeEleitoral();
        siglaPartido = p.getSiglaPartido();
        fotoPolitico = p.getUrlFoto();
        estadoPolitico = p.getSiglaUf();
        urlPartidoLogo = p.getUrlPartidoLogo();
        this.montaIdDespesa();
        this.buildData();
    }

    public void montaIdDespesa() {
        this.id =  this.dataDocumento.replace("-", "") + this.idPolitico
                + this.valorDocumento.replace(".", "") + this.codDocumento;
    }
}
