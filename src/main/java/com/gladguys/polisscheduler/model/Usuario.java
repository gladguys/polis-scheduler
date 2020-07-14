package com.gladguys.polisscheduler.model;

import lombok.Data;

import java.util.HashMap;

@Data
public class Usuario {

    private String id;
    private String fcmToken;
    private HashMap<String, Object> userConfigs;
}
