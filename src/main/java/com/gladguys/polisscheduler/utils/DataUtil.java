package com.gladguys.polisscheduler.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DataUtil {

    public static int getNumeroMes() {
        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.getMonthValue();
    }

    public static boolean ehHoje(String dataComoString) {
            String dataHojeComoString = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).toString();
            return dataComoString.equals(dataHojeComoString);
    }

    public static String getDataOntem() {
        return LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).toString();
    }

    public static int getNumeroAno() {
        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.getYear();
    }
}