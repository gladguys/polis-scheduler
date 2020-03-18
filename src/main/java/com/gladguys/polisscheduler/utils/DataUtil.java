package com.gladguys.polisscheduler.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class DataUtil {

    public static int getNumeroMes() {
        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.getMonthValue();
    }
}