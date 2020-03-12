package com.gladguys.polisscheduler.builder;

import com.gladguys.polisscheduler.model.Politico;
import com.gladguys.polisscheduler.model.PoliticoCompleto;

public class PoliticoBuilder {

	public static Politico build(PoliticoCompleto politicoCompleto) {
		Politico politico =  new Politico();
		politico.setId(politicoCompleto.getId());
		politico.setEmail(politicoCompleto.getUltimoStatus().getEmail());
		politico.setSiglaPartido(politicoCompleto.getUltimoStatus().getSiglaPartido());
		politico.setUrlFoto(politicoCompleto.getUltimoStatus().getUrlFoto());
		politico.setCondicaoEleitoral(politicoCompleto.getUltimoStatus().getCondicaoEleitoral());
		politico.setDataNascimento(politicoCompleto.getDataNascimento());
		politico.setCpf(politicoCompleto.getCpf());
		politico.setSexo(politicoCompleto.getSexo());
		politico.setEscolaridade(politicoCompleto.getEscolaridade());
		politico.setStatus(politicoCompleto.getUltimoStatus().getStatus());
		politico.setNomeCivil(politicoCompleto.getNomeCivil());
		politico.setNomeEleitoral(politicoCompleto.getUltimoStatus().getNomeEleitoral());
		politico.setSiglaUf(politicoCompleto.getUltimoStatus().getSiglaUf());

		return politico;
	}
}
