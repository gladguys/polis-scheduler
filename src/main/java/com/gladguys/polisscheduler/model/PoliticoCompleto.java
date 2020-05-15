package com.gladguys.polisscheduler.model;

import lombok.Data;

import java.util.List;

@Data
public class PoliticoCompleto {

	private String id;
	private String nomeCivil;
	private UltimoStatusPolitico ultimoStatus;
	private String cpf;
	private String sexo;
	private String dataNascimento;
	private String escolaridade;

    public boolean politicoNaoExisteNoFirestore(List<String> politicosIds) {
		return this.id == null || !politicosIds.contains(this.id);
    }
}
