package com.google.gwt.sample.notes.shared;

/**
 * Implementazione concreta dell'interfaccia User.
 * Questa classe rappresenta un utente del sistema con le sue credenziali.
 */
public class ConcreteUser implements User {
    private String email;
    private String password;

    public ConcreteUser() {}

    /**
     * Costruttore che inizializza l'utente con email e password
     */
    public ConcreteUser(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}