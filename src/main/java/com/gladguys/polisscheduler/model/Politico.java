package com.gladguys.polisscheduler.model;

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

	public Politico () {}

	public Politico(String id, String nomeCivil, String siglaPartido, String siglaUf,
					String urlFoto, String email, String nomeEleitoral, String status,
					String condicaoEleitoral, String cpf, String sexo, String dataNascimento, String escolaridade) {
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

	public String getSiglaPartido() {
		return siglaPartido;
	}

	public void setSiglaPartido(String siglaPartido) {
		this.siglaPartido = siglaPartido;
	}

	public String getSiglaUf() {
		return siglaUf;
	}

	public void setSiglaUf(String siglaUf) {
		this.siglaUf = siglaUf;
	}

	public String getUrlFoto() {
		return urlFoto;
	}

	public void setUrlFoto(String urlFoto) {
		this.urlFoto = urlFoto;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNomeEleitoral() {
		return nomeEleitoral;
	}

	public void setNomeEleitoral(String nomeEleitoral) {
		this.nomeEleitoral = nomeEleitoral;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCondicaoEleitoral() {
		return condicaoEleitoral;
	}

	public void setCondicaoEleitoral(String condicaoEleitoral) {
		this.condicaoEleitoral = condicaoEleitoral;
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
