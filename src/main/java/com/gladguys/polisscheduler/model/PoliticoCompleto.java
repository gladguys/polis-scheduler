package com.gladguys.polisscheduler.model;

public class PoliticoCompleto {

	private String id;
	private String nomeCivil;
	private UltimoStatusPolitico ultimoStatus;
	private String cpf;
	private String sexo;
	private String dataNascimento;
	private String escolaridade;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNomeCivil() {
		return nomeCivil;
	}

	public void setNomeCivil(String nomeCivil) {
		this.nomeCivil = nomeCivil;
	}

	public UltimoStatusPolitico getUltimoStatus() {
		return ultimoStatus;
	}

	public void setUltimoStatus(UltimoStatusPolitico ultimoStatus) {
		this.ultimoStatus = ultimoStatus;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getSexo() {
		return sexo;
	}

	public void setSexo(String sexo) {
		this.sexo = sexo;
	}

	public String getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(String dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	public String getEscolaridade() {
		return escolaridade;
	}

	public void setEscolaridade(String escolaridade) {
		this.escolaridade = escolaridade;
	}
}
