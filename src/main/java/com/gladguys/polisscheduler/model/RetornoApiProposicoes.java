package com.gladguys.polisscheduler.model;

import java.util.List;

public class RetornoApiProposicoes {

	public List<RetornoApiSimples> dados;
	public List<Link> links;
	
	public boolean temMaisPaginasComConteudo() {
		for (Link link : links) {
			if (link.rel.equals("next")) return true;
		}
		return false;
	}
}
