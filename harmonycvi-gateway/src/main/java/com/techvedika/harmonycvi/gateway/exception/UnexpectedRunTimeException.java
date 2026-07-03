package com.techvedika.harmonycvi.gateway.exception;

public class UnexpectedRunTimeException extends RuntimeException {

    public UnexpectedRunTimeException(String message) {
        super(message);
    }

    public UnexpectedRunTimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
