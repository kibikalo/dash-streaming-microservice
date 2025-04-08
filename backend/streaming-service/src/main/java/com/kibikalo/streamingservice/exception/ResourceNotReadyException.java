package com.kibikalo.streamingservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ResourceNotReadyException extends RuntimeException {
    public ResourceNotReadyException(String message) {
        super(message);
    }
}