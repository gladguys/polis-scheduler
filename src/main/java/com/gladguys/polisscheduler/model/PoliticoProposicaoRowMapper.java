package com.gladguys.polisscheduler.model;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PoliticoProposicaoRowMapper implements RowMapper<PoliticoProposicao> {

    @Override
    public PoliticoProposicao mapRow(ResultSet rs, int rowNum) throws SQLException {

        PoliticoProposicao pp = new PoliticoProposicao();
        pp.setId(rs.getString("id"));
        pp.setPolitico(rs.getString("politico"));
        pp.setProposicao(rs.getString("proposicao"));
        pp.setAtualizacao(rs.getString("atualizacao"));

        return pp;

    }
}
