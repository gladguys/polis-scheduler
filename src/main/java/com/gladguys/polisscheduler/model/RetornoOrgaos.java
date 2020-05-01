package com.gladguys.polisscheduler.model;

import java.util.List;

import lombok.Data;


@Data
public class RetornoOrgaos {

    private List<Orgao> dados;

    public boolean temOrgaos() {
        return dados != null && dados.size() > 0;
    }
}