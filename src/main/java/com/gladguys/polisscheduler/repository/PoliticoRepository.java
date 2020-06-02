package com.gladguys.polisscheduler.repository;

import com.gladguys.polisscheduler.model.Politico;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PoliticoRepository {

    final JdbcTemplate template;

    final String INSERT_QUERY = "insert into politicos (id) values (?)";

    public PoliticoRepository(JdbcTemplate template) {
        this.template = template;
    }

    public void salvarPolitico(String politicoId) {
        template.update(INSERT_QUERY, politicoId);
    }
}
