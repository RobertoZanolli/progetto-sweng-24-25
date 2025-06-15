package com.google.gwt.sample.notes.server;

/**
 * Eccezione personalizzata per gestire gli errori dei services.
 */
public class ServiceException extends Exception {
    private final int statusCode;

    public ServiceException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
