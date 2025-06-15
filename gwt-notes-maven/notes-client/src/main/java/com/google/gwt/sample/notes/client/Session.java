package com.google.gwt.sample.notes.client;

/**
 * Gestisce la sessione dell'utente corrente
 */
public class Session {
    private static Session instance;
    private String userEmail;

    public Session() {
        this.userEmail = null;
    }

    public static synchronized Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void destroy() {
        this.userEmail=null;
    }
}