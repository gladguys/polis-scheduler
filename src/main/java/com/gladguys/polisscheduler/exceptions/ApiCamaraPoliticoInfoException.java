package com.gladguys.polisscheduler.exceptions;

import org.springframework.web.client.RestClientException;

public class ApiCamaraPoliticoInfoException extends Throwable {
    public ApiCamaraPoliticoInfoException(String errorMessage, Throwable err) {
        super(errorMessage,err);
    }
}
