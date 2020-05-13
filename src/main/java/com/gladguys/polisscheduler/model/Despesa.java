package com.gladguys.polisscheduler.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Data;

@Data
public class Despesa {

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

    public void buildData() {
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
    }
}
