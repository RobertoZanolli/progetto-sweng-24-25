package com.google.gwt.sample.notes.server;

import com.google.gwt.sample.notes.shared.User;
import com.google.gwt.sample.notes.shared.ConcreteUser;

/**
 * Factory per la creazione e gestione degli utenti.
 * Implementa il pattern Singleton per garantire una singola istanza.
 */
public class UserFactory {

    private static final JsonParser parser = new GsonJsonParser();
    private static UserFactory instance;

    private UserFactory() {}

    /**
     * Restituisce l'istanza singleton della factory.
     */
    public static synchronized UserFactory getInstance() {
        if (instance == null) {
            instance = new UserFactory();
        }
        return instance;
    }

    /**
     * Crea un nuovo utente con email e password specificati.
     */
    public static synchronized User create(String email, String password) {
        return new ConcreteUser(email, password);
    }

    /**
     * Crea un utente a partire da una stringa JSON.
     */
    public static synchronized User fromJson(String json) {
        return parser.fromJson(json, ConcreteUser.class);
    }

    /**
     * Converte un utente in formato JSON.
     */
    public static synchronized String toJson(User user) {
        return parser.toJson(user);
    }
}
