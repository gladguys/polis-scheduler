package com.gladguys.polisscheduler.model;

import lombok.Data;

@Data
public class PoliticoCompleto {

	private String id;
	private String nomeCivil;
	private UltimoStatusPolitico ultimoStatus;
	private String cpf;
	private String sexo;
	private String dataNascimento;
	private String escolaridade;
}
