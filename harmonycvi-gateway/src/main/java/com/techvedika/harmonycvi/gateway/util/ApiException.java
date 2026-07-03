package com.techvedika.harmonycvi.gateway.util;

public class ApiException extends RuntimeException {
    private int status;
    private Object body;

    public ApiException(String message, int status, Object body) {
        super(message);
        this.status = status;
        this.body = body;
    }

    public int getStatus() {
        return status;
    }
    
    public int getStatusCode() {
    	return status;
    }

    public Object getBody() {
        return body;
    }
    
    public Object getResponseBody() {
    	return body;
    }
}
