package com.gladguys.polisscheduler.repository;

import com.gladguys.polisscheduler.model.Despesa;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DespesasRepository {

    final JdbcTemplate template;

    final String INSERT_QUERY = "insert into politico_despesa (id, politico) values (?, ?)";
    final String CHECK_IF_EXISTS = "SELECT count(*) FROM politico_despesa WHERE id = ?";

    public DespesasRepository(JdbcTemplate template) {
        this.template = template;
    }

    public boolean exists(Despesa despesa) {
        int count = this.template.queryForObject(CHECK_IF_EXISTS, new Object[] { despesa.getId() }, Integer.class);
        return count > 0;
    }

    public int inserirDespesa(Despesa despesa) {
        if (despesa == null) return -1;
        return template.update(INSERT_QUERY, despesa.getId(),despesa.getIdPolitico());
    }
}
