package com.gladguys.polisscheduler.exceptions;

public class ApiCamaraDeputadosException extends RuntimeException {
    public ApiCamaraDeputadosException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
