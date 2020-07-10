package com.gladguys.polisscheduler.model;

import lombok.Data;

@Data
public class Usuario {

    private String id;
    private String fcmToken;
    private boolean isNotificationEnabled;
}
