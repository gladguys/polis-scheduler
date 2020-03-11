package com.gladguys.polisscheduler.model;

import lombok.Data;

@Data
public class Politico {
	private String id;
	private String nomeCivil;
	private String siglaPartido;
	private String siglaUf;
	private String urlFoto;
	private String email;
	private String nomeEleitoral;
	private String status;
	private String condicaoEleitoral;
	private String cpf;
	private String sexo;
	private String dataNascimento;
	private String escolaridade;

	public Politico() {
	}

	public Politico(String id, String nomeCivil, String siglaPartido, String siglaUf,
					String urlFoto, String email, String nomeEleitoral, String status,
					String condicaoEleitoral, String cpf, String sexo, String dataNascimento,
					String escolaridade) {
		this.id = id;
		this.nomeCivil = nomeCivil;
		this.siglaPartido = siglaPartido;
		this.siglaUf = siglaUf;
		this.urlFoto = urlFoto;
		this.email = email;
		this.nomeEleitoral = nomeEleitoral;
		this.status = status;
		this.condicaoEleitoral = condicaoEleitoral;
		this.cpf = cpf;
		this.sexo = sexo;
		this.dataNascimento = dataNascimento;
		this.escolaridade = escolaridade;
	}
}
