package com.techvedika.harmonycvi.gateway.exception;

import org.springframework.http.HttpStatus;

public class GlobalApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;
	private final HttpStatus status;
    private final Object body;

    public GlobalApiException(HttpStatus status, Object body) {
        super(status.getReasonPhrase());
        this.status = status;
        this.body   = body;
    }

    public HttpStatus getStatus(){ return status; }
    public Object getBody(){ return body;}
    public HttpStatus getStatusCode() {
    	return status;
    }
    public Object getResponseBody() {
    	return body;
    }
}