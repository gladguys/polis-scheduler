package com.gladguys.polisscheduler.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Partido {

    String id;
    String nome;
    String sigla;

    public Map parseToMap() {
        Map partidoMap = new HashMap();
        partidoMap.put("id", id);
        partidoMap.put("nome", nome);
        partidoMap.put("sigla", sigla);

        return partidoMap;
    }
}
